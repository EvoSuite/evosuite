package org.evosuite.eclipse.popup.actions;

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;

public class GenerateTestsAction extends TestGenerationAction {

	HashSet<IResource> currentSelection = new HashSet<IResource>();

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {

		if (currentSelection.isEmpty()) {
			MessageDialog.openError(shell, "EvoSuite",
			                        "Unable to generate test cases for selection: Cannot find .java files.");
		} else if (currentSelection.size() > 1) {
			MessageDialog.openError(shell, "EvoSuite", "Please only select one class at a time.");
		} else {

			for (IResource res : currentSelection) {
				IProject proj = res.getProject();
				fixJUnitClassPath(JavaCore.create(proj));
				generateTests(res);
			}
		}
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
}
