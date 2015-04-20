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
			newAction.run(action);
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
