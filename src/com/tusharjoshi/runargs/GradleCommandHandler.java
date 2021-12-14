/*
 The MIT License (MIT)

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

import java.lang.reflect.Method;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author Kevin Kofler
 */
public class GradleCommandHandler extends CommandHandler {
    private static final String CMD_LINE_ARGS_GRADLE_ARG = "-PcmdLineArgs=";
    private static final String RUN_CLASS_NAME_GRADLE_ARG = "-PrunClassName=";
    private static final String RUN_ARGS_GRADLE_ARG = "-PrunArgs=";
    private static final String SELECTED_CLASS_PLACEHOLDER
            = "${selectedClass}";

    private void actionImpl(String applicationArgs, Project project,
            String resourceName, String command) throws IllegalStateException {
        try {
            Class<? extends Project> projectClass = project.getClass();
            ClassLoader classLoader = projectClass.getClassLoader();
            Method getActiveMapping = classLoader.loadClass(
                    "org.netbeans.modules.gradle.actions.ActionToTaskUtils")
                    .getMethod("getActiveMapping", String.class, Project.class,
                            Lookup.class);
            Lookup lookup = project.getLookup();
            Object activeMapping = getActiveMapping.invoke(null, command,
                    project, lookup);
            Class<?> customActionMappingClass = classLoader.loadClass(
                    "org.netbeans.modules.gradle.customizer.CustomActionMapping"
            );
            Class<?> actionMappingClass
                    = customActionMappingClass.getInterfaces()[0];
            Object customActionMapping = customActionMappingClass
                    .getConstructor(actionMappingClass, String.class)
                    .newInstance(activeMapping, null);
            String name = (String) customActionMappingClass
                    .getMethod("getName").invoke(customActionMapping);
            String args = (String) customActionMappingClass
                    .getMethod("getArgs").invoke(customActionMapping);
            // Yes, applicationArgs must be escaped as _1_ parameter.
            String newArgs = args + ' '
                    + (args.contains(RUN_CLASS_NAME_GRADLE_ARG)
                    ? RUN_ARGS_GRADLE_ARG : CMD_LINE_ARGS_GRADLE_ARG)
                    + BaseUtilities.escapeParameters(
                            new String[]{applicationArgs});
            if (resourceName != null) {
                newArgs = newArgs.replace(SELECTED_CLASS_PLACEHOLDER,
                        resourceName);
            }
            customActionMappingClass.getMethod("setArgs", String.class)
                    .invoke(customActionMapping, newArgs);
            Action customAction = (Action) classLoader.loadClass(
                    "org.netbeans.modules.gradle.ActionProviderImpl")
                    .getMethod("createCustomGradleAction", Project.class,
                            String.class, actionMappingClass, Lookup.class,
                            boolean.class).invoke(null, project, name,
                            customActionMapping, lookup, false);
            customAction.actionPerformed(null);
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
                command + Constants.SINGLE_SUFFIX);
    }
}
