package org.evosuite.intellij;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ApplicationComponent;
import org.evosuite.intellij.util.MavenExecutor;
import org.jetbrains.annotations.NotNull;

/**
 * Entry point for the IntelliJ plugin
 *
 * Created by arcuri on 9/9/14.
 */
public class PluginRegistration implements ApplicationComponent {
    // Returns the component name (any unique string value).
    @NotNull
    public String getComponentName() {
        return "EvoSuite Plugin";
    }


    // If you register the PluginRegistration class in the <application-components> section of
// the plugin.xml file, this method is called on IDEA start-up.
    public void initComponent() {
        ActionManager am = ActionManager.getInstance();

        EvoAction evo = new EvoAction();

        // Gets an instance of the WindowMenu action group.
        //DefaultActionGroup windowM = (DefaultActionGroup) am.getAction("WindowMenu");
        //this in the file editor, not the left-pane file selection
        //DefaultActionGroup editorM = (DefaultActionGroup) am.getAction("EditorPopupMenu");

        DefaultActionGroup pvM = (DefaultActionGroup) am.getAction("ProjectViewPopupMenu");
        pvM.addSeparator();
        pvM.add(evo);

        if(MavenExecutor.getInstance().isAlreadyRunning()){
            //TODO disable evo
        }
    }

    // Disposes system resources.
    public void disposeComponent() {
    }
}