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
package org.evosuite.eclipse.popup.actions;

import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.evosuite.eclipse.quickfixes.MarkerWriter;

public class ClearMarkersAction implements IObjectActionDelegate {

	HashSet<IResource> currentSelection = new HashSet<IResource>();

	protected Shell shell;
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {
		Boolean disabled = System.getProperty("evosuite.markers.clear.disable") != null;
		if (disabled) {
			MessageDialog.openInformation(shell, "Evosuite", "Feature not available.");
			return;
		}
		/*
		for (IResource res : currentSelection) {
			try {
				MarkerWriter.clearMarkers(res);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}*/
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		currentSelection.clear();

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;

			for (Object o : sel.toList()) {
				if (o instanceof IJavaElement) {
					IJavaElement jEl = (IJavaElement) o;
					try {
						IResource jRes = jEl.getCorrespondingResource();
						if (jRes != null) {
							jRes.accept(new IResourceVisitor() {
								@Override
								public boolean visit(IResource resource)
								        throws CoreException {
									if ("java".equals(resource.getFileExtension()))
										currentSelection.add(resource);
									return true;
								}
							});
						}
					} catch (JavaModelException e) {
						System.err.println("Error while traversing resources!" + e);
					} catch (CoreException e) {
						System.err.println("Error while traversing resources!" + e);
					}
				}
			}
		}
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}
}
