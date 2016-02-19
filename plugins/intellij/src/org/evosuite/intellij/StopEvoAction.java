/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
        //notifier.printOnConsole("\n\n\nEvoSuite run has been cancelled\n"); //done in the Task
    }
}
