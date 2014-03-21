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
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;

/**
 *
 * @author tushar_joshi
 */
public class PropertyHandler {

    private final Project project;
    private final FileObject propsFO;
    private EditableProperties props;

    public static PropertyHandler createPrivatePropertiesHandler(Project project) {
        return new PropertyHandler(project, AntProjectHelper.PRIVATE_PROPERTIES_PATH);
    }

    public static PropertyHandler createProjectPropertiesHandler(Project project) {
        return new PropertyHandler(project, AntProjectHelper.PROJECT_PROPERTIES_PATH);
    }

    PropertyHandler(Project project, String propertyFilePath) {

        this.project = project;
        this.propsFO = project.getProjectDirectory()
                .getFileObject(propertyFilePath);
        if (this.propsFO != null) {
            this.props = new EditableProperties(false);
        }

    }

    public String getProperty(String propertyName) {

        String value = null;

        if (this.propsFO != null) {
            InputStream propsIS = null;
            try {
                propsIS = this.propsFO.getInputStream();
                this.props.load(propsIS);
                value = this.props.get(propertyName);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                try {
                    if (null != propsIS) {
                        propsIS.close();
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        }

        return value;
    }

    public void setProperty(String propertyName, String inputText) {
        if (this.propsFO != null && null != this.props) {
            this.props.put(propertyName, inputText);

            OutputStream outputStream = null;
            try {
                outputStream = this.propsFO.getOutputStream();
                this.props.store(outputStream);

            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (null != outputStream) {
                    try {
                        outputStream.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }

        }
    }

}
