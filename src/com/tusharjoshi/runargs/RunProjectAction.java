/*
 The MIT License (MIT)

 Copyright (c) 2014 Tushar Joshi
 Copyright (c) 2020 DAGOPT Optimization Technologies GmbH

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
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

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
    "CTL_RunProjectAction=Run with Arguments"})
public class RunProjectAction extends ProjectAction 
implements ContextAwareAction {   

    @Override
    public Action createContextAwareInstance(Lookup lkp) {
        return new RunProjectAction(lkp, Constants.COMMAND_RUN_NAME, "D-S-R");
    }
    
    public RunProjectAction() {
        super(Utilities.actionsGlobalContext(), Constants.COMMAND_RUN_NAME, "D-S-R");
    }
    
    public RunProjectAction(final Lookup lkp, String commandName, String accKey) {
        super(lkp,commandName, accKey);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        Project project = getProject();
        CommandHandler commandHandler
                = CommandHandler.createCommandHandler(project);
        if (commandHandler != null) {
            commandHandler.runProject(project);
        }
    } 
}
