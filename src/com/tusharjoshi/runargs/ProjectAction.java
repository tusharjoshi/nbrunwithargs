/*
 The MIT License (MIT)

 Copyright (c) 2014 Tushar Joshi
 Copyright (c) 2020-2021 DAGOPT Optimization Technologies GmbH

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

import javax.swing.AbstractAction;
import org.netbeans.api.project.Project;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tushar Joshi
 */
public abstract class ProjectAction extends AbstractAction {   

    private Project project;
    
    private Lookup.Result<Project> result;
    
    private Lookup lkp;
    
    private final LookupListener listener;
    
    public ProjectAction(String commandName, String accKey) {
        this(Utilities.actionsGlobalContext(), commandName, accKey);
    }

    public ProjectAction(final Lookup lkp, String commandName, String accKey) {

        this.listener = new ProjectLookupListener(commandName);
        
        this.lkp = lkp;        
        this.result = lkp.lookupResult(Project.class);
        this.result.addLookupListener(
                WeakListeners.create(LookupListener.class, listener, this.result));
        
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);        
        putValue(ACCELERATOR_KEY, Utilities.stringToKey(accKey));

        lookupChanged(commandName);
    }

    public Project getProject() {
        return project;
    }
    
    private class ProjectLookupListener implements LookupListener {
        
        private final String commandName;
        
        public ProjectLookupListener(final String commandName) {
            this.commandName = commandName;
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            lookupChanged(commandName);
        }
        
    }

    private void lookupChanged(String commandName) {
        project = CommandHandler.findProject( lkp);
        
        String projectName = "";
        boolean enableMenu = false;
        
        if( null != project ) {
            switch( project.getClass().getName() ) {
                case Constants.J2SEPROJECT:
                case Constants.MAVENPROJECT:
                case Constants.GRADLEPLUGINPROJECT:
                case Constants.GRADLEPROJECT:
                    projectName = CommandHandler.getProjectName(project);
                    enableMenu = true;
                    break;
                default:
                    break;
            }
        }
            
        putValue(NAME, Bundle.MSG_INPUT_TITLE(projectName, 
                commandName));
        setEnabled(enableMenu);
    }    
}
