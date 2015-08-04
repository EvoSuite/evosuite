package org.evosuite.eclipse.popup.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
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
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

@SuppressWarnings("restriction")
public class ExtendSuiteEditorAction extends ExtendSuiteAction {

//	@Override
//	public void selectionChanged(IAction action, ISelection selection) {
//		
//	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		String SUT = "";		
		IResource target = null;
		
		System.out.println("Current selection of type "+selection.getClass().getName()+": "+selection);
		if(selection instanceof TreeSelection) {
			TreeSelection treeSelection = (TreeSelection) selection;
			IAdaptable firstElement = (IAdaptable) treeSelection.getFirstElement();

			// Relies on an internal API, bad juju
			if (firstElement instanceof org.eclipse.jdt.internal.core.CompilationUnit) {
				try {
					org.eclipse.jdt.internal.core.CompilationUnit compilationUnit = (org.eclipse.jdt.internal.core.CompilationUnit) firstElement;									
					String packageName = "";
					if (compilationUnit.getPackageDeclarations().length > 0) {
						System.out.println("Package: "
								+ compilationUnit.getPackageDeclarations()[0].getElementName());
						packageName = compilationUnit.getPackageDeclarations()[0].getElementName();
					}
					String targetSuite = compilationUnit.getElementName().replace(".java", "");
					if (!packageName.isEmpty())
						targetSuite = packageName + "." + targetSuite;
					System.out.println("Selected class: " + targetSuite);
					SUT = targetSuite;
					target = compilationUnit.getResource();
				} catch (JavaModelException e) {
					
				}
			}
		}
		else if (activeEditor instanceof JavaEditor) {
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

				IWorkspaceRoot wroot = ResourcesPlugin.getWorkspace().getRoot();
				target = wroot.findMember(root.getPath());
			} catch (JavaModelException e) {

			}
		}
		if (!SUT.isEmpty() && target != null) {
			IProject proj = target.getProject();
			fixJUnitClassPath(JavaCore.create(proj));
			generateTests(target);
		}

		return null;
	}

}
