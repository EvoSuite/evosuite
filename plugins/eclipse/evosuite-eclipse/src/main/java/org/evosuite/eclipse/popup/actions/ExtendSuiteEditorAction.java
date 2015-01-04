package org.evosuite.eclipse.popup.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class ExtendSuiteEditorAction extends ExtendSuiteAction {

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		
	}

	@Override
	public void run(IAction action) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart activeEditor = page.getActiveEditor();
		System.out.println("JUHU!");
		String SUT = "";
		if (activeEditor instanceof JavaEditor) {
			ITypeRoot root = EditorUtility.getEditorInputJavaElement(activeEditor, false);
			ITextSelection sel = (ITextSelection) ((JavaEditor) activeEditor).getSelectionProvider().getSelection();
			int offset = sel.getOffset();
			IJavaElement element;

			try {
				element = root.getElementAt(offset);
				if (element.getElementType() == IJavaElement.METHOD) {
					IJavaElement pDeclaration = element.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
					IPackageFragment pFragment = (IPackageFragment) pDeclaration;
					String packageName = "";
					if (pFragment.getCompilationUnits()[0].getPackageDeclarations().length > 0) {
						System.out.println("Package: "
						        + pFragment.getCompilationUnits()[0].getPackageDeclarations()[0].getElementName());
						packageName = pFragment.getCompilationUnits()[0].getPackageDeclarations()[0].getElementName();
					}
					String targetSuite = element.getParent().getElementName();
					if (!packageName.isEmpty())
						targetSuite = packageName + "." + targetSuite;
					System.out.println("Selected class: " + targetSuite);
					SUT = targetSuite;
				} else if (element.getElementType() == IJavaElement.TYPE) {
					IType type = ((IType) element);
					System.out.println("Selected class: " + type.getFullyQualifiedName());
					SUT = type.getFullyQualifiedName();
				}

				if (!SUT.isEmpty()) {
					IWorkspaceRoot wroot = ResourcesPlugin.getWorkspace().getRoot();
					IResource res = wroot.findMember(root.getPath());
					IProject proj = res.getProject();
					fixJUnitClassPath(JavaCore.create(proj));
					generateTests(res);
				}
			} catch (JavaModelException e) {

			}
		}
	}

}
