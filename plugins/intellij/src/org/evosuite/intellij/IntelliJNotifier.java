package org.evosuite.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.evosuite.intellij.util.AsyncGUINotifier;

/**
 * Created by arcuri on 10/2/14.
 */
public class IntelliJNotifier implements AsyncGUINotifier{

    private final String title;
    private final Project project;

    public IntelliJNotifier(Project project, String title){
        this.project = project;
        this.title = title;
    }

    @Override
    public void success(String message) {
        Messages.showMessageDialog(project,message, title, Messages.getInformationIcon());
    }

    @Override
    public void failed(String message) {
        Messages.showMessageDialog(project,message, title, Messages.getWarningIcon());
    }
}
