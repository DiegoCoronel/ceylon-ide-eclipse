package com.redhat.ceylon.eclipse.code.refactor;

import static com.redhat.ceylon.compiler.typechecker.tree.Util.formatPath;
import static com.redhat.ceylon.eclipse.core.builder.CeylonBuilder.getProjectTypeChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.CopyParticipant;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import com.redhat.ceylon.compiler.typechecker.context.PhasedUnit;
import com.redhat.ceylon.compiler.typechecker.tree.Tree.ImportPath;
import com.redhat.ceylon.compiler.typechecker.tree.Visitor;
import com.redhat.ceylon.eclipse.core.vfs.IFileVirtualFile;

public class CopyFileRefactoringParticipant extends CopyParticipant {

    private IFile file;

    @Override
    protected boolean initialize(Object element) {
        file= (IFile) element;
        return file.getFileExtension().equals("ceylon");
    }
    
    @Override
    public String getName() {
        return "Copy file participant for Ceylon source";
    }

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm,
            CheckConditionsContext context) throws OperationCanceledException {
        return new RefactoringStatus();
    }

    public Change createChange(IProgressMonitor pm) throws CoreException {
        try {
        Change change = new Change() {
            IFile newFile;
            @Override
            public Change perform(IProgressMonitor pm) throws CoreException {
                IFolder dest = (IFolder) getArguments().getDestination();
                final String newName = dest.getProjectRelativePath()
                        .removeFirstSegments(1).toPortableString()
                        .replace('/', '.');
                newFile = dest.getFile(file.getName());
                String relFilePath = file.getProjectRelativePath()
                        .removeFirstSegments(1).toPortableString();
                String relPath = file.getProjectRelativePath()
                        .removeFirstSegments(1).removeLastSegments(1)
                        .toPortableString();
                final String oldName = relPath.replace('/', '.');
                final IProject project = file.getProject();
                
                final HashMap<IFile,Change> changes= new HashMap<IFile,Change>();
                PhasedUnit phasedUnit = getProjectTypeChecker(project)
                        .getPhasedUnitFromRelativePath(relFilePath);
                final List<ReplaceEdit> edits = new ArrayList<ReplaceEdit>();
                if (phasedUnit.getPackage().getNameAsString().startsWith(oldName)) {
                    phasedUnit.getCompilationUnit().visit(new Visitor() {
                        @Override
                        public void visit(ImportPath that) {
                            super.visit(that);
                            if (formatPath(that.getIdentifiers()).equals(oldName)) {
                                edits.add(new ReplaceEdit(that.getStartIndex(), oldName.length(), newName));
                            }
                        }
                    });
                    if (!edits.isEmpty()) {
                        try {
                            IFile file = ((IFileVirtualFile) phasedUnit.getUnitFile()).getFile();
                            TextFileChange change= new TextFileChange(file.getName(), newFile);
                            change.setEdit(new MultiTextEdit());
                            changes.put(file, change);
                            for (ReplaceEdit edit: edits) {
                                change.addEdit(edit);
                            }
                        }       
                        catch (Exception e) { 
                            e.printStackTrace(); 
                        }
                    }
                }
                
                if (changes.isEmpty())
                    return null;
                
                CompositeChange result= new CompositeChange("Ceylon source changes");
                for (Change change: changes.values()) {
                    result.add(change);
                }
                result.perform(pm);
                return null;
            }

            @Override
            public String getName() {
                return "Copy Ceylon Package";
            }

            @Override
            public void initializeValidationData(IProgressMonitor pm) {}

            @Override
            public RefactoringStatus isValid(IProgressMonitor pm)
                    throws CoreException, OperationCanceledException {
                return new RefactoringStatus();
            }

            @Override
            public Object getModifiedElement() {
                return newFile;
            }
        };
        change.setEnabled(true);
        return change;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }
//
//    private IFile getMovedFile(final String newName, IFile file) {
//        String oldPath = file.getParent().getElementName().replace('.', '/');
//        String newPath = newName.replace('.', '/');
//        String replaced = file.getProjectRelativePath().toString()
//                .replace(oldPath, newPath);
//        return file.getProject().getFile(replaced);
//    }
}
