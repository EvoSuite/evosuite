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

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Entry point for the IntelliJ plugin for when projects are opened/closed
 *
 * <p/>
 * Created by arcuri on 9/9/14.
 */
public class ProjectRegistration implements ProjectComponent { //implements ApplicationComponent {

    private final Project project;

    private ConsoleViewImpl console;

    public ProjectRegistration(Project project){
        this.project = project;
    }


    // Returns the component name (any unique string value).
    @NotNull
    public String getComponentName() {
        return "EvoSuite Plugin";
    }


    // If you register the ProjectRegistration class in the <application-components> section of
// the plugin.xml file, this method is called on IDEA start-up.
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {
        EvoParameters.getInstance().save(project);
    }

    @Override
    public void projectOpened() {

        EvoParameters.getInstance().load(project);

        ActionManager am = ActionManager.getInstance();

        //create the tool window, which will appear in the bottom when an EvoSuite run is started
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.registerToolWindow("EvoSuite", false, ToolWindowAnchor.BOTTOM);
        toolWindow.setTitle("EvoSuite Console Output");
        toolWindow.setType(ToolWindowType.DOCKED, null);


        //create a console panel
        console = (ConsoleViewImpl) TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        JComponent consolePanel = console.getComponent();


        IntelliJNotifier notifier = IntelliJNotifier.registerNotifier(project,"EvoSuite Plugin", console);

        //create left-toolbar with stop button
        DefaultActionGroup buttonGroup = new DefaultActionGroup();
        buttonGroup.add(new StopEvoAction(notifier));
        ActionToolbar viewToolbar = am.createActionToolbar("EvoSuite.ConsoleToolbar", buttonGroup, false);
        JComponent toolBarPanel = viewToolbar.getComponent();


        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(toolBarPanel, BorderLayout.WEST);
        panel.add(consolePanel,BorderLayout.CENTER);


        //Content content = contentFactory.createContent(consolePanel, "", false);
        Content content = contentFactory.createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);

    }

    @Override
    public void projectClosed() {
        EvoParameters.getInstance().save(project);
        if(console!=null){
            console.dispose();
        }
    }

}