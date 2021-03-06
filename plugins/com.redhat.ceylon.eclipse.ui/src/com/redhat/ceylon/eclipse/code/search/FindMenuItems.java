package com.redhat.ceylon.eclipse.code.search;


import static com.redhat.ceylon.eclipse.code.editor.DynamicMenuItem.collapseMenuItems;
import static com.redhat.ceylon.eclipse.code.editor.Util.getCurrentEditor;
import static com.redhat.ceylon.eclipse.ui.CeylonPlugin.PLUGIN_ID;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.CompoundContributionItem;

import com.redhat.ceylon.eclipse.code.editor.CeylonEditor;
import com.redhat.ceylon.eclipse.code.editor.DynamicMenuItem;

public class FindMenuItems extends CompoundContributionItem {
    
    public FindMenuItems() {}
    
    public FindMenuItems(String id) {
        super(id);
    }
    
    @Override
    public IContributionItem[] getContributionItems() {
        IContributionItem[] items = getItems(getCurrentEditor());
        if (collapseMenuItems(getParent())) {
            MenuManager submenu = new MenuManager("Find");
            submenu.setActionDefinitionId(CeylonEditor.FIND_MENU_ID);
            for (IContributionItem item: items) {
                submenu.add(item);
            }
            return new IContributionItem[] { submenu };
        }
        else {
            return items;
        }
    }

    private IContributionItem[] getItems(IEditorPart editor) {
        return new IContributionItem[] {
                //new Separator(),
                new DynamicMenuItem(PLUGIN_ID + ".action.findReferences", "&Find References",
                        editor==null ? false : new FindReferencesAction(editor).isEnabled(), 
                        		AbstractFindAction.REFS),
                new DynamicMenuItem(PLUGIN_ID + ".action.findAssignments", "Find &Assignments",
                		editor==null ? false : new FindAssignmentsAction(editor).isEnabled(), 
                				AbstractFindAction.REFS),
                new DynamicMenuItem(PLUGIN_ID + ".action.findRefinements", "Find &Refinements",
                		editor==null ? false : new FindRefinementsAction(editor).isEnabled(), 
                				AbstractFindAction.DECS),
                new DynamicMenuItem(PLUGIN_ID + ".action.findSubtypes", "Find &Subtypes",
                		editor==null ? false : new FindSubtypesAction(editor).isEnabled(), 
                				AbstractFindAction.DECS)
                //new Separator()
            };
    }

}
