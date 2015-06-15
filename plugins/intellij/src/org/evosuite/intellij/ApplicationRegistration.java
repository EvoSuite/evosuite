package org.evosuite.intellij;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

/**
 *  Entry point for the IntelliJ plugin for when IntelliJ starts
 *
 * Created by arcuri on 11/1/14.
 */
public class ApplicationRegistration implements ApplicationComponent {
    @Override
    public void initComponent() {
        EvoAction evo = new EvoAction();

        // Gets an instance of the WindowMenu action group.
        //DefaultActionGroup windowM = (DefaultActionGroup) am.getAction("WindowMenu");
        //this in the file editor, not the left-pane file selection
        //DefaultActionGroup editorM = (DefaultActionGroup) am.getAction("EditorPopupMenu");

        ActionManager am = ActionManager.getInstance();

        DefaultActionGroup pvM = (DefaultActionGroup) am.getAction("ProjectViewPopupMenu");
        pvM.addSeparator();
        pvM.add(evo);

        DefaultActionGroup epM = (DefaultActionGroup) am.getAction("EditorPopupMenu");
        epM.addSeparator();
        epM.add(evo);
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "EvoSuite Plugin";
    }
}
