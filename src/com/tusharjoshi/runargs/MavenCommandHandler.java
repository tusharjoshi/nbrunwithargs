/*
 The MIT License (MIT)

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

import java.lang.reflect.Method;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;

/**
 *
 * @author Kevin Kofler
 */
public class MavenCommandHandler extends CommandHandler {
    private static final String EXEC_ARGS = "exec.args";
    private static final String PACKAGE_CLASS_NAME = "packageClassName";
    private static final String MAIN_SUFFIX = ".main";

    private void actionImpl(String applicationArgs, Project project,
            String resourceName, String command) throws IllegalStateException {
        try {
            Class<? extends Project> projectClass = project.getClass();
            ClassLoader classLoader = projectClass.getClassLoader();
            Method createRunConfig = classLoader.loadClass(
                    "org.netbeans.modules.maven.execute.ActionToGoalUtils")
                    .getMethod("createRunConfig", String.class, projectClass,
                            Lookup.class);
            Object runConfig = createRunConfig.invoke(null, command, project,
                    project.getLookup());
            Class<?> runConfigClass = createRunConfig.getReturnType();
            Class<?> runUtilsClass = classLoader.loadClass(
                    "org.netbeans.modules.maven.api.execute.RunUtils");
            Object newRunConfig = runUtilsClass.getMethod("cloneRunConfig",
                    runConfigClass).invoke(null, runConfig);
            Map<? extends String,? extends String> properties =
                    (Map<? extends String,? extends String>) runConfigClass
                            .getMethod("getProperties").invoke(newRunConfig);
            String execArgs = properties.get(EXEC_ARGS);
            System.out.println(execArgs);
            Method setProperty = runConfigClass.getMethod("setProperty",
                    String.class, String.class);
            setProperty.invoke(newRunConfig, EXEC_ARGS,
                            execArgs + ' ' + applicationArgs);
            if (resourceName != null) {
                setProperty.invoke(newRunConfig, PACKAGE_CLASS_NAME,
                        resourceName);
            }
            runUtilsClass.getMethod("run", runConfigClass)
                    .invoke(null, newRunConfig);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    protected void projectActionImpl(String applicationArgs, Project project,
            String command) {
        actionImpl(applicationArgs, project, null, command);
    }

    @Override
    protected void fileActionImpl(String applicationArgs, Project project,
            String resourceName, String command) {
        actionImpl(applicationArgs, project, resourceName,
                command + Constants.SINGLE_SUFFIX + MAIN_SUFFIX);
    }
}
