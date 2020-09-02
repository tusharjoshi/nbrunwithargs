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

import java.io.IOException;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Tushar Joshi
 */
public class AntCommandHandler extends CommandHandler { 
    private static final String APPLICATION_ARGS = "application.args"; // NOI18N
    private static final String BUILD_XML = "build.xml"; // NOI18N

    @Override
    protected void projectActionImpl(String applicationArgs, Project project,
            String command) {
        Properties properties = new Properties();
        /*
         Ant script in the build-impl.xml file uses
         application.args property to pass any arguments.
         */
        properties.put(APPLICATION_ARGS, applicationArgs);

        FileObject projectDirectory = project.getProjectDirectory();
        FileObject fileObject = projectDirectory.getFileObject(BUILD_XML);

        try {
            ActionUtils.runTarget(fileObject, new String[]{command}, properties);
        } catch (IOException | IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected void fileActionImpl(String applicationArgs, Project project,
            String resourceName, String command) {
        Properties properties = new Properties();
        /*
         Ant script in the build-impl.xml file uses
         application.args property to pass any arguments.
         */
        properties.put(APPLICATION_ARGS, applicationArgs);
        properties.put("main.class", resourceName);

        FileObject projectDirectory = project.getProjectDirectory();
        FileObject fileObject = projectDirectory.getFileObject(BUILD_XML);

        try {
            ActionUtils.runTarget(fileObject, new String[]{command}, properties);
        } catch (IOException | IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
