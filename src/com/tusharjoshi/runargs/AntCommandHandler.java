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

import java.io.IOException;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Tushar Joshi
 */
@NbBundle.Messages({
    "MSG_INPUT_TEXT=Enter command line arguments:",
    "# {0} - Project name",
    "MSG_INPUT_TITLE=Run{0} with Arguments"
})
public class AntCommandHandler {

    private static final String APPLICATION_ARGS = "application.args"; // NOI18N
    private static final String COMMAND_RUN = "run"; // NOI18N
    private static final String BUILD_XML = "build.xml"; // NOI18N 
    
    public void runProject(Project project) {
        PropertyHandler propertiesHandler
                = PropertyHandler.createPrivatePropertiesHandler(project);

        String inputText;
        inputText = propertiesHandler.getProperty(APPLICATION_ARGS);

        NotifyDescriptor.InputLine inputLine
                = new NotifyDescriptor.InputLine(Bundle.MSG_INPUT_TEXT(),
                        Bundle.MSG_INPUT_TITLE(getProjectName(project)));
        inputLine.setInputText(inputText);
        Object resultOption = DialogDisplayer.getDefault().notify(
                inputLine);
        if (NotifyDescriptor.OK_OPTION != resultOption) {
            return;
        }

        inputText = inputLine.getInputText();

        Properties properties = new Properties();
        /*
         Ant script in the build-impl.xml file uses
         application.args property to pass any arguments.
         */
        properties.put(APPLICATION_ARGS, inputText);

        propertiesHandler.setProperty(APPLICATION_ARGS, inputText);

        FileObject projectDirectory = project.getProjectDirectory();
        FileObject fileObject = projectDirectory.getFileObject(BUILD_XML);

        try {
            ActionUtils.runTarget(fileObject, new String[]{COMMAND_RUN}, properties);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @NbBundle.Messages({
        "# {0} - Class name",
        "LBL_No_Main_Classs_Found=Class \"{0}\" does not have a main method."
    })
    public void runFile(DataObject dataObject) {
        
        Project project = findProject(dataObject);
        if( null == project ) {
            return;
        }
        
        String resourceName;
        resourceName = getFullClassName(dataObject.getPrimaryFile());
        
        if( SourceUtils.getMainClasses(dataObject.getPrimaryFile()).isEmpty() ) {            
            NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.LBL_No_Main_Classs_Found(resourceName), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        
        PropertyHandler privateProperties
                = PropertyHandler.createPrivatePropertiesHandler(project);
        
        PropertyHandler projectProperties
                = PropertyHandler.createProjectPropertiesHandler(project);

        String inputText;
        inputText = privateProperties.getProperty(APPLICATION_ARGS);

        NotifyDescriptor.InputLine inputLine
                = new NotifyDescriptor.InputLine(Bundle.MSG_INPUT_TEXT(),
                        Bundle.MSG_INPUT_TITLE(" " + resourceName));
        inputLine.setInputText(inputText);
        Object resultOption = DialogDisplayer.getDefault().notify(
                inputLine);
        if (NotifyDescriptor.OK_OPTION != resultOption) {
            return;
        }

        inputText = inputLine.getInputText();

        Properties properties = new Properties();
        /*
         Ant script in the build-impl.xml file uses
         application.args property to pass any arguments.
         */
        properties.put(APPLICATION_ARGS, inputText);

        privateProperties.setProperty(APPLICATION_ARGS, inputText);

        FileObject projectDirectory = project.getProjectDirectory();
        FileObject fileObject = projectDirectory.getFileObject(BUILD_XML);
        
        String oldMainClass = projectProperties.getProperty("main.class");
        projectProperties.setProperty("main.class", resourceName);

        try {
            ExecutorTask task = ActionUtils.runTarget(fileObject, new String[]{COMMAND_RUN}, properties);
            task.waitFinished();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            projectProperties.setProperty("main.class", oldMainClass);
        }
    }

    private String getFullClassName(FileObject fileObject) {
        ClassPath classPath = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
        String name = null;
        if( null != classPath ) {
            name = classPath.getResourceName(fileObject);
            name = name.replace("/", ".");
            if( name.endsWith(".java")){
                name = name.substring(0, name.length() - 5);
            }
        }
        return name;
    }
    
    public static Project findProject(Lookup lkp) {
        
        DataObject dataObject = lkp.lookup(DataObject.class);
        if( null != dataObject ) {
            return findProject(dataObject);
        }
                
        return null;
    }
    
    public static Project findProject(DataObject dataObject) {
        Project p = null;
        if( null != dataObject ) {
            p = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
        }
                
        return p;
    }
    
    public static String getProjectName(Project project) {
        ProjectInformation projectInfo = ProjectUtils.getInformation(project);
        return " (" + projectInfo.getDisplayName() + ")";
    }
    
}
