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
package org.evosuite.eclipse.quickfixes;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.evosuite.eclipse.popup.actions.GenerateTestsEditorAction;
import org.evosuite.eclipse.Activator;

public class FileQueueAction implements IObjectActionDelegate {

	protected Shell shell;
	
	@Override
	public void run(IAction action) {
		Boolean disabled = System.getProperty("evosuite.disable") != null; //  && System.getProperty("evosuite.disable").equals("1")
		if ( disabled ) {
			MessageDialog.openInformation(shell, "Sorry!", "The EvoSuite plugin is disabled :(");
			return;
		}
		System.out.println("[FileQueueAction] EvoSuite Plugin is enabled");
		if (Activator.markersEnabled()) {
			System.out.println("[FileQueueAction] Markers are enabled");
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				IStructuredSelection select = (IStructuredSelection) window
						.getSelectionService().getSelection();
				for (Object o : select.toArray()) {
					System.out.println(o.getClass());
					if (o instanceof ICompilationUnit) {
						ICompilationUnit icomp = (ICompilationUnit) o;
						o = icomp.getResource();
					}
					if (o instanceof IResource) {
						IResource res = (IResource) o;
						if (res.getType() == IResource.FILE
								&& res.getFileExtension().equalsIgnoreCase("java")
								&& !res.getName().endsWith(Activator.JUNIT_IDENTIFIER)
								&& !res.getName().endsWith(Activator.SCAFFOLDING_IDENTIFIER)) {
							Activator.FILE_QUEUE.addFile(res);
							Activator.FILE_QUEUE.update();
							Activator.getDefault().resetRoamingJob(res);
						}
					}
				}
			}
		} else {
			System.out.println("[FileQueueAction] Markers are disabled");
			GenerateTestsEditorAction newAction = new GenerateTestsEditorAction();
			// TODO: Fix
			// newAction.run(action);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

}
