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
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Build",
        id = "com.tusharjoshi.runargs.DebugFileAction"
)
@ActionRegistration(
        displayName = "#CTL_DebugFileAction"
)
@ActionReference(path = "Loaders/text/x-java/Actions", position = 920)
@Messages("CTL_DebugFileAction=Debug File with Arguments")
public final class DebugFileAction implements ActionListener {
    
    private final DataObject dataObject;

    public DebugFileAction(DataObject dataObject) {
        this.dataObject = dataObject;        
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        
        new AntCommandHandler().debugFile(dataObject);
        
    }
}
