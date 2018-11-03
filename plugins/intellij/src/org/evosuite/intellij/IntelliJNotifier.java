/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.intellij;

import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.evosuite.intellij.util.AsyncGUINotifier;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by arcuri on 10/2/14.
 */
public class IntelliJNotifier implements AsyncGUINotifier {

    private static final Map<Project, IntelliJNotifier> map = new LinkedHashMap<Project, IntelliJNotifier>();

    private final String title;
    private final Project project;
    private final ConsoleViewImpl console;

    private volatile OSProcessHandler processHandler;

    public IntelliJNotifier(Project project, String title, ConsoleViewImpl console) {
        this.project = project;
        this.title = title;
        this.console = console;
    }

    public static IntelliJNotifier getNotifier(Project p){
        return map.get(p);
    }

    public static IntelliJNotifier registerNotifier(Project project, String title, ConsoleViewImpl console){
        IntelliJNotifier n = new IntelliJNotifier(project,title,console);
        map.put(project,n);
        return n;
    }

    @Override
    public void success(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                Messages.showMessageDialog(project, message, title, Messages.getInformationIcon());
            }
        });
    }

    @Override
    public void failed(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                Messages.showMessageDialog(project, message, title, Messages.getWarningIcon());
            }
        });
    }

    @Override
    public void detachLastProcess(){
        if(processHandler != null){
            processHandler.destroyProcess();
            processHandler = null;
        }
    }

    @Override
    public void attachProcess(Process process) {
        if(processHandler != null){
            detachLastProcess();
        }
        processHandler = new OSProcessHandler(process, null);
        console.attachToProcess(processHandler);
        processHandler.startNotify();
    }

    @Override
    public void printOnConsole(String message) {
        console.print(message, ConsoleViewContentType.NORMAL_OUTPUT );
        //console.flushDeferredText();
    }

    @Override
    public void clearConsole() {
        console.clear();
    }
}
