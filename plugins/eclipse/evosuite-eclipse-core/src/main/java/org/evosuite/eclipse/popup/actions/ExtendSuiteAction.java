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
import java.util.HashSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.evosuite.junit.DetermineSUT;
import org.evosuite.junit.DetermineSUT.NoJUnitClassException;

@SuppressWarnings("restriction")
public abstract class ExtendSuiteAction extends TestGenerationAction {

    HashSet<IResource> currentSelection = new HashSet<IResource>();

//	@Override
//	public void selectionChanged(IAction action, ISelection selection) {
//		currentSelection.clear();
//
//		if (selection instanceof IStructuredSelection) {
//			IStructuredSelection sel = (IStructuredSelection) selection;
//
//			for (Object o : sel.toList()) {
//				if (o instanceof IJavaElement) {
//					IJavaElement jEl = (IJavaElement) o;
//					try {
//						IResource jRes = jEl.getCorrespondingResource();
//						if (jRes != null) {
//							jRes.accept(new IResourceVisitor() {
//								@Override
//								public boolean visit(IResource resource)
//								        throws CoreException {
//									if ("java".equals(resource.getFileExtension()))
//										currentSelection.add(resource);
//									return true;
//								}
//							});
//						}
//					} catch (JavaModelException e) {
//						System.err.println("Error while traversing resources!" + e);
//					} catch (CoreException e) {
//						System.err.println("Error while traversing resources!" + e);
//					}
//				}
//			}
//		}
//	}
//
//	@Override
//	public void run(IAction action) {
//		if (currentSelection.isEmpty()) {
//			MessageDialog.openError(shell, "Evosuite",
//			                        "Unable to generate test cases for selection: Cannot find .java files.");
//		} else if (currentSelection.size() > 1) {
//			MessageDialog.openError(shell, "Evosuite",
//			                        "Please only select one class at a time.");
//		} else {
//
//			for (IResource res : currentSelection) {
//				IProject proj = res.getProject();
//				fixJUnitClassPath(JavaCore.create(proj));
//				generateTests(res);
//			}
//		}
//	}

    /**
     * Add a new test generation job to the job queue
     *
     * @param target
     */
    @Override
    protected void addTestJob(final IResource target) {
        IJavaElement element = JavaCore.create(target);
        IJavaElement packageElement = element.getParent();

        String packageName = packageElement.getElementName();

        final String suiteClass = (!packageName.equals("") ? packageName + "." : "")
                + target.getName().replace(".java", "").replace(File.separator, ".");
        System.out.println("Building new job for " + suiteClass);
        DetermineSUT det = new DetermineSUT();
        IJavaProject jProject = JavaCore.create(target.getProject());
        try {
            String classPath = target.getWorkspace().getRoot().findMember(jProject.getOutputLocation()).getLocation().toOSString();
            String SUT = det.getSUTName(suiteClass, classPath);

            // choose
            SelectionDialog typeDialog = JavaUI.createTypeDialog(shell,
                    new ProgressMonitorDialog(
                            shell),
                    target.getProject(),
                    IJavaElementSearchConstants.CONSIDER_CLASSES,
                    false);
            Object[] sutDefault = new Object[1];
            sutDefault[0] = SUT;
            typeDialog.setInitialSelections(sutDefault);
            typeDialog.setTitle("Please select the class under test");
            typeDialog.open();

            // Type selected by the user
            Object[] result = typeDialog.getResult();
            if (result.length > 0) {
                SourceType sourceType = (SourceType) result[0];
                SUT = sourceType.getFullyQualifiedName();
            } else {
                return;
            }

            Job job = new TestExtensionJob(shell, target, SUT, suiteClass);
            job.setPriority(Job.SHORT);
            IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
            ISchedulingRule rule = ruleFactory.createRule(target.getProject());

            //IFolder folder = proj.getFolder(ResourceUtil.EVOSUITE_FILES);
            job.setRule(rule);
            job.setUser(true);
            job.schedule(); // start as soon as possible

        } catch (JavaModelException e) {
            e.printStackTrace();
        } catch (NoJUnitClassException e) {
            MessageDialog.openError(shell, "Evosuite", "Cannot find JUnit tests in " + suiteClass);
        }

    }

}
