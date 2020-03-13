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

import java.util.Collection;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Tushar Joshi
 */
@NbBundle.Messages({
    "MSG_INPUT_TEXT=Enter command line arguments:",
    "# {0} - Project name",
    "# {1} - Command",
    "MSG_INPUT_TITLE={1}{0} with Arguments"
})
public abstract class CommandHandler {
    public static CommandHandler createCommandHandler(Project project) {
        switch( project.getClass().getName() ) {
            case Constants.J2SEPROJECT:
                return new AntCommandHandler();
            case Constants.MAVENPROJECT:
                return new MavenCommandHandler();
            case Constants.GRADLEPROJECT:
                return new GradleCommandHandler();
            default:
                return null;
        }
    }

    public static CommandHandler createCommandHandler(DataObject dataObject) {
        return createCommandHandler(findProject(dataObject));
    }

    public void runProject(Project project) {
        projectAction(project, Constants.COMMAND_RUN);
    }

    public void debugProject(Project project) {
        projectAction(project, Constants.COMMAND_DEBUG);
    }

    private String getCommandName(@NonNull final String command) {
        switch (command) {
            case Constants.COMMAND_RUN:
                return Constants.COMMAND_RUN_NAME;
            case Constants.COMMAND_DEBUG:
                return Constants.COMMAND_DEBUG_NAME;
            default:
                return "";
        }
    }


    public void projectAction(@NullAllowed final Project project,
            @NonNull final String command) {

        if (null == project) {
            return;
        }

        PropertyHandler propertiesHandler
                = PropertyHandler.createPrivatePropertiesHandler(project);

        String inputText;
        inputText = propertiesHandler.getProperty(Constants.APPLICATION_ARGS);

        NotifyDescriptor.InputLine inputLine
                = new NotifyDescriptor.InputLine(Bundle.MSG_INPUT_TEXT(),
                        Bundle.MSG_INPUT_TITLE(getProjectName(project), getCommandName(command)));
        inputLine.setInputText(inputText);
        Object resultOption = DialogDisplayer.getDefault().notify(
                inputLine);
        if (NotifyDescriptor.OK_OPTION != resultOption) {
            return;
        }

        inputText = inputLine.getInputText();

        propertiesHandler.setProperty(Constants.APPLICATION_ARGS, inputText);

        projectActionImpl(inputText, project, command);
    }

    protected abstract void projectActionImpl(String applicationArgs,
            Project project, String command);

    public void runFile(DataObject dataObject) {
        fileAction(dataObject, Constants.COMMAND_RUN);
    }

    public void debugFile(DataObject dataObject) {
        fileAction(dataObject, Constants.COMMAND_DEBUG);
    }

    @NbBundle.Messages(value = {
        "# {0} - Class name",
        "LBL_No_Main_Classs_Found=Class \"{0}\" does not have a main method."
    })
    public void fileAction(@NullAllowed final DataObject dataObject,
            @NonNull final String command) {

        if (null == dataObject) {
            return;
        }

        Project project = findProject(dataObject);
        if (null == project) {
            return;
        }

        String resourceName;
        resourceName = getFullClassName(dataObject.getPrimaryFile());

        if (SourceUtils.getMainClasses(dataObject.getPrimaryFile()).isEmpty()) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.LBL_No_Main_Classs_Found(resourceName), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }

        PropertyHandler privateProperties
                = PropertyHandler.createPrivatePropertiesHandler(project);

        String inputText;
        inputText = privateProperties.getProperty(Constants.APPLICATION_ARGS);

        NotifyDescriptor.InputLine inputLine
                = new NotifyDescriptor.InputLine(Bundle.MSG_INPUT_TEXT(),
                        Bundle.MSG_INPUT_TITLE(" " + resourceName, getCommandName(command)));
        inputLine.setInputText(inputText);
        Object resultOption = DialogDisplayer.getDefault().notify(
                inputLine);
        if (NotifyDescriptor.OK_OPTION != resultOption) {
            return;
        }

        inputText = inputLine.getInputText();

        privateProperties.setProperty(Constants.APPLICATION_ARGS, inputText);

        fileActionImpl(inputText, project, resourceName, command);
    }

    protected abstract void fileActionImpl(String applicationArgs,
            Project project, String resourceName, String command);

    private String getFullClassName(FileObject fileObject) {
        ClassPath classPath = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
        String name = null;
        if (null != classPath) {
            name = classPath.getResourceName(fileObject);
            name = name.replace("/", ".");
            if (name.endsWith(".java")) {
                name = name.substring(0, name.length() - 5);
            }
        }
        return name;
    }

    public static Project findProject(Lookup lkp) {
        printLookupObjects(lkp);
        DataObject dataObject = lkp.lookup(DataObject.class);
        if (null != dataObject) {
            return findProject(dataObject);
        }

        return null;
    }

    protected static void printLookupObjects(Lookup lkp) {
        Collection<? extends Object> allObjects = lkp.lookupAll(Object.class);
        for (Object obj : allObjects) {
            System.out.println("" + obj.getClass().getName());
        }
    }

    public static Project findProject(DataObject dataObject) {
        Project p = null;
        if (null != dataObject) {
            p = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
        }

        return p;
    }

    public static String getProjectName(Project project) {
        ProjectInformation projectInfo = ProjectUtils.getInformation(project);
        return " (" + projectInfo.getDisplayName() + ")";
    }
}
