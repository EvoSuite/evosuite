/*
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

package org.evosuite.eclipse.popup.actions;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.evosuite.Properties;

/**
 * @author Gordon Fraser
 */
public class GenerationResult extends Dialog {

    protected Object result;
    protected Shell shlEvosuiteResult;
    protected IPath location;
    protected IResource targetResource;

    /**
     * Create the dialog.
     *
     * @param parent
     * @param style
     */
    public GenerationResult(Shell parent, int style, IPath location,
                            final IResource target) {
        super(parent, style);
        this.location = location;
        this.targetResource = target;
        setText("EvoSuite Result");
    }

    /**
     * Open the dialog.
     *
     * @return the result
     */
    public Object open() {
        createContents();
        shlEvosuiteResult.open();
        shlEvosuiteResult.layout();
        Display display = getParent().getDisplay();
        while (!shlEvosuiteResult.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        shlEvosuiteResult = new Shell(getParent(), SWT.SHELL_TRIM);
        shlEvosuiteResult.setMinimumSize(new Point(320, 160));
        shlEvosuiteResult.setSize(600, 611);
        shlEvosuiteResult.setText("EvoSuite Result");

        Browser browser = new Browser(shlEvosuiteResult, SWT.NONE);
        browser.setBounds(0, 0, 600, 559);

        System.out.println("Directory: " + location.toOSString() + File.separator
                + "evosuite-report/html");
        File dir = new File(location.toOSString() + File.separator + "evosuite-report"
                + File.separator + "html");
        String target = null;
        int max = -1;
        for (File report : dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".html")
                        && filename.startsWith("report-" + Properties.TARGET_CLASS);
            }
        })) {
            String filename = report.getName();
            String[] parts = filename.split("-");
            if (parts.length == 3) {

                int x = Integer.parseInt(parts[2].replace(".html", ""));
                if (x > max)
                    max = x;
            }
        }
        if (max >= 0) {
            target = location + "/evosuite-report/html/report-" + Properties.TARGET_CLASS
                    + "-" + max + ".html";
        } else {
            target = location + "/evosuite-report/report-generation.html";
        }
        System.out.println("Opening " + target);
        browser.setUrl(target);
        browser.update();

        Button btnNewButton = new Button(shlEvosuiteResult, SWT.NONE);
        btnNewButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                showFile(targetResource.getLocation());
                shlEvosuiteResult.close();
            }
        });
        btnNewButton.setBounds(10, 561, 110, 28);
        btnNewButton.setText("Edit " + targetResource.getName());

        Button btnNewButton_1 = new Button(shlEvosuiteResult, SWT.NONE);
        btnNewButton_1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                // Open evosuite-tests/.../Test<Target>
                shlEvosuiteResult.close();
                IWorkbench wb = PlatformUI.getWorkbench();
                IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
                IWorkbenchPage page = win.getActivePage();
                try {
                    String subPath = Properties.TARGET_CLASS.replace(".", File.separator);
                    int pos = subPath.lastIndexOf(File.separator);
                    if (pos >= 0) {
                        IPath targetFile = location.append(File.separator + "evosuite-tests" + File.separator
                                + subPath + Properties.JUNIT_SUFFIX
                                // + subPath.substring(0, pos) + "/Test"
                                // + subPath.substring(pos + 1)
                                + ".java");
                        System.out.println("target: " + targetFile);

                        IDE.openEditor(page,
                                ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(targetFile));
                    } else {
                        IPath targetFile = location.append(File.separator + "evosuite-tests" + File.separator
                                + subPath + Properties.JUNIT_SUFFIX
                                //IPath targetFile = location.append("/evosuite-tests/" + "/Test"
                                //        + subPath
                                + ".java");
                        System.out.println("target: " + targetFile);
                        IDE.openEditor(page,
                                ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(targetFile));
                    }
                } catch (PartInitException e2) {
                    //Put your exception handler here if you wish to.
                    System.out.println("Error: " + e2);
                }
            }
        });
        btnNewButton_1.setBounds(118, 561, 94, 28);
        btnNewButton_1.setText("Edit Tests");

        Button btnNewButton2 = new Button(shlEvosuiteResult, SWT.NONE);
        btnNewButton2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shlEvosuiteResult.close();
            }
        });
        btnNewButton2.setBounds(228, 561, 110, 28);
        btnNewButton2.setText("Close");

    }

    private void showFile(IPath targetFile) {
        IWorkbench wb = PlatformUI.getWorkbench();
        IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        IWorkbenchPage page = win.getActivePage();
        try {
            IDE.openEditor(page,
                    ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(targetFile));
        } catch (PartInitException e2) {
            //Put your exception handler here if you wish to.
            System.out.println("Error: " + e2);
        }
    }
}
