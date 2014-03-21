/*
 The MIT License (MIT)

 Copyright (c) 2014 Tushar Joshi

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package com.tusharjoshi.runargs;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tushar Joshi
 */
@ActionID(
        category = "Build",
        id = "com.tusharjoshi.runargs.RunProjectAction"
)
@ActionRegistration(displayName = "#CTL_RunProjectAction", lazy = false)
@ActionReferences({
    @ActionReference(path = "Menu/BuildProject", position = 0),
    @ActionReference(path = "Projects/Actions", position = 0),    
    @ActionReference(path = "Shortcuts", name = "D-S-R")
})
@NbBundle.Messages({
    "# {0} - project name",
    "CTL_RunProjectAction=Run{0} with Arguments"})
public class RunProjectAction extends AbstractAction 
implements ContextAwareAction, LookupListener {   

    private Project project;
    
    private Lookup.Result<Project> result;
    
    private Lookup lkp;

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new RunProjectAction(lkp);
    }
    
    public RunProjectAction() {
        this(Utilities.actionsGlobalContext());
    }

    public RunProjectAction(final Lookup lkp) {

        this.lkp = lkp;        
        this.result = lkp.lookupResult(Project.class);
        this.result.addLookupListener(
                WeakListeners.create(LookupListener.class, this, this.result));
        
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);        
        putValue(ACCELERATOR_KEY, Utilities.stringToKey("D-S-R"));

        resultChanged(null);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        
        if( null == project ) 
            return;
        
        new AntCommandHandler().runProject(project);
    }

    @Override
    public final void resultChanged(LookupEvent le) {
        project = AntCommandHandler.findProject( lkp);
        AntBuildExtender extender = null;
        String projectName = "";
        
        if( null != project ) {
                
            extender
                = project.getLookup().lookup(AntBuildExtender.class);
            
            if( null != extender ) { 
                projectName = AntCommandHandler.getProjectName(project);
            }
                   
        } 
            
        putValue(NAME, Bundle.CTL_RunProjectAction(projectName));
        setEnabled(null != extender);
    }    
}
