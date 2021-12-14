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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;

/**
 *
 * @author Kevin Kofler
 */
public class GradlePluginCommandHandler extends CommandHandler {
    private static final String CMD_LINE_ARGS_PLACEHOLDER = "${cmd-line-args}";
    private static final String SELECTED_CLASS_PLACEHOLDER
            = "${selected-class}";

    private void replacePlaceholder(ArrayList<String> args, String placeholder,
            String replacement) {
        int nArguments = args.size();
        for (int i = 0; i < nArguments; i++) {
            String arg = args.get(i);
            if (arg.contains(placeholder)) {
                args.set(i, arg.replace(placeholder, replacement));
            }
        }
    }

    private void actionImpl(String applicationArgs, Project project,
            String resourceName, String command) throws IllegalStateException {
        try {
            Class<? extends Project> projectClass = project.getClass();
            Object configProvider = projectClass.getMethod("getConfigProvider")
                    .invoke(project);
            Object mergedCommandQuery = projectClass
                    .getMethod("getMergedCommandQuery").invoke(project);
            Object gradleCommandExecutor = projectClass
                    .getMethod("getGradleCommandExecutor").invoke(project);
            Object activeConfiguration = configProvider.getClass().getMethod(
                    "getActiveConfiguration").invoke(configProvider);
            Method getProfileDef = activeConfiguration.getClass()
                    .getMethod("getProfileDef");
            Object profileDef = getProfileDef.invoke(activeConfiguration);
            Class<?> profileDefClass = getProfileDef.getReturnType();
            Class<?> mergedCommandQueryClass = mergedCommandQuery.getClass();
            Method tryGetDefaultGradleCommand = mergedCommandQueryClass
                    .getMethod("tryGetDefaultGradleCommand", profileDefClass,
                            String.class);
            Object gradleCommand = tryGetDefaultGradleCommand
                    .invoke(mergedCommandQuery, profileDef, command);
            Class<?> gradleCommandClass
                    = tryGetDefaultGradleCommand.getReturnType();
            Class<?>[] nestedClasses = gradleCommandClass.getDeclaredClasses();
            Class<?> builderClass = null;
            for (Class<?> nestedClass : nestedClasses) {
                if ("Builder".equals(nestedClass.getSimpleName())) {
                    builderClass = nestedClass;
                    break;
                }
            }
            Object builder = builderClass.getConstructor(gradleCommandClass)
                    .newInstance(gradleCommand);
            List<String> arguments = (List<String>) gradleCommandClass
                    .getMethod("getArguments").invoke(gradleCommand);
            ArrayList<String> newArguments = new ArrayList<String>(arguments);
            replacePlaceholder(newArguments, CMD_LINE_ARGS_PLACEHOLDER,
                    applicationArgs);
            if (resourceName != null) {
                replacePlaceholder(newArguments, SELECTED_CLASS_PLACEHOLDER,
                        resourceName);
            }
            builderClass.getMethod("setArguments", List.class).invoke(builder,
                    newArguments);
            Object modifiedGradleCommand = builderClass.getMethod("create")
                    .invoke(builder);
            Method tryGetCommandDefs = mergedCommandQueryClass.getMethod(
                    "tryGetCommandDefs", profileDefClass, String.class);
            Object commandDefs = tryGetCommandDefs
                    .invoke(mergedCommandQuery, profileDef, command);
            Class<?> commandDefsClass = tryGetCommandDefs.getReturnType();
            if (modifiedGradleCommand != null && commandDefs != null) {
                gradleCommandExecutor.getClass().getMethod("executeCommand",
                        gradleCommandClass, commandDefsClass).invoke(
                                gradleCommandExecutor, modifiedGradleCommand,
                                commandDefs);
            }
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
