package org.evosuite.intellij;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.evosuite.intellij.util.AsyncGUINotifier;
import org.evosuite.intellij.util.EvoSuiteExecutor;

/**
 * Created by arcuri on 10/15/14.
 */
public class StopEvoAction extends AnAction {

    private final AsyncGUINotifier notifier;

    public StopEvoAction(AsyncGUINotifier notifier){
        super("Stop EvoSuite");
        getTemplatePresentation().setIcon(AllIcons.Actions.CloseNew);
        getTemplatePresentation().setHoveredIcon(AllIcons.Actions.CloseNewHovered);
        this.notifier = notifier;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        EvoSuiteExecutor.getInstance().stopRun();
        notifier.printOnConsole("\n\n\nEvoSuite run has been cancelled\n");
    }
}
