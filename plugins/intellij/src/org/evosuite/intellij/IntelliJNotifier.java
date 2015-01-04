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
    public void attachProcess(Process process) {
        OSProcessHandler handler = new OSProcessHandler(process, null);
        console.attachToProcess(handler);
        handler.startNotify();
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
