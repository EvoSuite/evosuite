package org.evosuite.eclipse.quickfixes;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.evosuite.eclipse.popup.actions.GenerateTestsEditorAction;
import org.evosuite.eclipse.Activator;

public class FileQueueAction implements IObjectActionDelegate {

	@Override
	public void run(IAction action) {
		if (Activator.isEnabled()) {
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
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
								&& res.getFileExtension().equalsIgnoreCase(
										"java")
								&& !res.getName().endsWith(
										Activator.JUNIT_IDENTIFIER)) {
							Activator.FILE_QUEUE.addFile(res);
							Activator.FILE_QUEUE.update();
							Activator.getDefault().resetRoamingJob(res);

						}
					}
				}
			}
		} else {
			GenerateTestsEditorAction newAction = new GenerateTestsEditorAction();
			newAction.run(action);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setActivePart(IAction arg0, IWorkbenchPart arg1) {
		// TODO Auto-generated method stub

	}

}
