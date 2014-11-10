/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.eclipse.popup.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.evosuite.Properties;
import org.evosuite.Properties.Algorithm;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.Properties.Strategy;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.classpath.ClassPathHacker;
import org.evosuite.eclipse.properties.EvoSuitePropertyPage;
// import org.evosuite.junit.JUnitTestReader;
import org.evosuite.eclipse.replace.TestCaseReplacer;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.testcase.ExecutionTracer;
import org.evosuite.testcase.RandomLengthTestFactory;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;

@SuppressWarnings("restriction")
public class ReplaceTestAction implements IObjectActionDelegate {

	private Shell shell;

	/**
	 * Constructor for Action1.
	 */
	public ReplaceTestAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	private static String SUT = "";

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	@Override
	public void run(IAction action) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart activeEditor = page.getActiveEditor();

		if (activeEditor instanceof JavaEditor) {
			ITypeRoot root = EditorUtility.getEditorInputJavaElement(activeEditor, false);
			ITextSelection sel = (ITextSelection) ((JavaEditor) activeEditor).getSelectionProvider().getSelection();
			int offset = sel.getOffset();
			IJavaElement element;

			try {
				element = root.getElementAt(offset);
				if (element.getElementType() == IJavaElement.METHOD) {
					IMethod method = (IMethod) element;
					IJavaElement pDeclaration = element.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
					IPackageFragment pFragment = (IPackageFragment) pDeclaration;
					System.out.println("Package: "
					        + pFragment.getCompilationUnits()[0].getPackageDeclarations()[0].getElementName());
					String packageName = pFragment.getCompilationUnits()[0].getPackageDeclarations()[0].getElementName();
					String targetSuite = element.getParent().getElementName();
					if (!packageName.isEmpty())
						targetSuite = packageName + "." + targetSuite;

					int position = targetSuite.lastIndexOf('.');
					final String targetClass;
					if (position == -1) {
						targetClass = targetSuite.replaceFirst("Test", "");
					} else {
						System.out.println("Substring 1: "
						        + targetSuite.substring(position + 1, position + 5));
						System.out.println("Substring 2: "
						        + targetSuite.substring(targetSuite.length() - 4));
						System.out.println("Substring 3: "
						        + targetSuite.substring(0, targetSuite.length() - 4));
						if (targetSuite.substring(position + 1, position + 5).equals("Test")) {
							targetClass = targetSuite.substring(0, position + 1)
							        + targetSuite.substring(position + 5);
						} else if (targetSuite.substring(targetSuite.length() - 4).equals("Test")) {
							targetClass = targetSuite.substring(0,
							                                    targetSuite.length() - 4);
						} else {
							//System.out.println(PlatformUI.getWorkbench());
							//System.out.println(PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActiveWorkbenchWindow());
							final List<String> classNames = new ArrayList<String>();
							final String prefix = targetSuite.substring(0, position);

							try {
								for (IPackageFragment fragment : root.getJavaProject().getPackageFragments()) {
									if (fragment.getKind() == IPackageFragmentRoot.K_SOURCE) {
										for (ICompilationUnit unit : fragment.getCompilationUnits()) {
											for (IPackageDeclaration packageDeclaration : unit.getPackageDeclarations()) {
												if (packageDeclaration.getElementName().startsWith(prefix)) {
													classNames.add(packageDeclaration.getElementName()
													        + "."
													        + unit.getElementName().replace(".java",
													                                        ""));
												}
											}
										}
									}
								}
							} catch (JavaModelException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

							PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
								                                                @Override
								                                                public void run() {
									                                                Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

									                                                ElementListSelectionDialog dialog = new ElementListSelectionDialog(
									                                                        activeShell,
									                                                        new LabelProvider());
									                                                dialog.setTitle("String Selection");
									                                                dialog.setMessage("Select a String (* = any string, ? = any char):");
									                                                dialog.setElements(classNames.toArray());
									                                                dialog.open();
									                                                if (dialog.getFirstResult() != null) {
										                                                SUT = dialog.getFirstResult().toString();
									                                                }

									                                                dialog.close();
								                                                }
							                                                });
							targetClass = SUT;
						}
					}
					if (targetClass == null || targetClass.equals("")) {
						PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
							@Override
							public void run() {

								Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
								MessageDialog.openError(activeShell,
								                        "Evosuite",
								                        "Cannot measure coverage because the class under test is unknown - please specify class or adhere to naming convention.");

							}
						});
						return;
					}
					IWorkspaceRoot wroot = ResourcesPlugin.getWorkspace().getRoot();
					IResource res = wroot.findMember(root.getPath());

					IJavaProject jProject = root.getJavaProject();
					IClasspathEntry[] oldEntries = jProject.getRawClasspath();
					List<String> cPath = new ArrayList<String>();
					String classPathText = "";

					// for(IClasspathEntry curr : jProject.getResolvedClasspath(true)) {
					for (int i = 0; i < oldEntries.length; i++) {
						IClasspathEntry curr = oldEntries[i];
						if (curr.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
							IPath path = curr.getPath();
							System.out.println(curr);
							cPath.add(wroot.findMember(path).getLocation().toOSString());
							classPathText += wroot.findMember(path).getLocation().toOSString();
						} else if (curr.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
							System.out.println("Container: " + curr);

							//JavaCore.getClasspathContainer(curr.getPath(), jProject);
							System.out.println(wroot.findMember(JavaCore.getResolvedClasspathEntry(curr).getPath()));
						}
						//						        + path.toOSString();
					}
					classPathText = wroot.findMember(jProject.getOutputLocation()).getLocation().toOSString()
					        + classPathText;
					cPath.add(wroot.findMember(jProject.getOutputLocation()).getLocation().toOSString());
					try {
						ClassPathHacker.addFile(wroot.findMember(jProject.getOutputLocation()).getLocation().toOSString());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("CP: " + cPath);
					System.out.println("Target class: " + targetClass);
					String[] classPath = new String[cPath.size()];
					cPath.toArray(classPath);
					Properties.CP = classPathText;
					Properties.TARGET_CLASS = targetClass;
					Properties.getInstance();
					Properties.resetTargetClass();
					Properties.STOPPING_CONDITION = StoppingCondition.MAXTIME;
					try {
						Properties.SEARCH_BUDGET = Integer.parseInt(jProject.getProject().getPersistentProperty(EvoSuitePropertyPage.REPLACEMENT_TIME_PROP_KEY));
					} catch (Throwable e) {
						Properties.SEARCH_BUDGET = 20;
					}

					try {

						DependencyAnalysis.analyze(Properties.TARGET_CLASS,
						                           Arrays.asList(Properties.CP.split(":")));
					} catch (Throwable t) {
						System.out.println("Error: " + t);
						return;
					}
					ExecutionTracer.enableTraceCalls();
					System.out.println("Setting Properties.CP = " + Properties.CP);

					TestCaseReplacer replacer = new TestCaseReplacer();
					TestCase replacement = replacer.replaceTest(targetClass,
					                                            res.getLocation().toOSString(),
					                                            element.getElementName(),
					                                            classPath);

					MessageDialog dialog = new MessageDialog(shell,
					        "Replacement test generated", null,
					        "The following replacement test was found:\n\n"
					                + replacement.toCode()
					                + "\n\nDo you want to replace the old test?",
					        MessageDialog.QUESTION_WITH_CANCEL, new String[] { "Replace",
					                "Cancel" }, 0);
					int returnCode = dialog.open();
					if (returnCode != 0)
						return;

					IDocumentProvider prov = ((JavaEditor) activeEditor).getDocumentProvider();
					IDocument doc = prov.getDocument(((JavaEditor) activeEditor).getEditorInput());
					ISourceRange range = method.getSourceRange();
					doc.replace(range.getOffset(), range.getLength(),
					            testToString(replacement, element.getElementName()));
				}

			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * Given a test, create a GA and look for a replacement test
	 * 
	 * @param test
	 */
	public TestCase replaceTest(String targetClass, List<TestCase> otherTests,
	        TestCase test) {
		// Various environmental setup necessary for EvoSuite
		Properties.ALGORITHM = Algorithm.STEADYSTATEGA;
		Properties.STRATEGY = Strategy.ONEBRANCH;
		ExecutionTracer.enableTraceCalls();

		// Run for 10 generations - adapt as necessary
		Properties.STOPPING_CONDITION = StoppingCondition.MAXGENERATIONS;
		Properties.SEARCH_BUDGET = 100;

		GeneticAlgorithm<TestChromosome> ga = TestSuiteGenerator.getGeneticAlgorithm(new RandomLengthTestFactory());

		// Set up fitness function for the parsed test case
		/*
		DifferenceFitnessFunction fitness = new DifferenceFitnessFunction(test,
		        otherTests, TestSuiteGenerator.getFitnessFactory());
		ga.setFitnessFunction(fitness);

		// Perform calculation
		ga.generateSolution();

		// The best individual at the end of the search contains our candidate solution
		TestChromosome testChromosome = (TestChromosome) ga.getBestIndividual();
		TestCaseMinimizer minimizer = new TestCaseMinimizer(fitness.getOriginalGoals());
		minimizer.minimize(testChromosome);

		System.out.println("Best individual has fitness: " + testChromosome.getFitness());
		return testChromosome.getTestCase();
		*/

		return null;
	}

	/**
	 * Try to parse test case from a file, then start replacement
	 * 
	 * @param fileName
	 *            Name of the file containing the unit tests
	 * 
	 * @param testName
	 *            Method name of the test=
	 */
	public TestCase replaceTest(String targetClass, String fileName, String testName) {
		Map<String, TestCase> tests = getTestCases(fileName);
		TestCase target = tests.get(testName);
		System.out.println("Found target test: " + target.toCode());
		tests.remove(testName); //remove "test5" method name
		return replaceTest(targetClass, new ArrayList<TestCase>(tests.values()), target);
	}

	/**
	 * Parse all the tests in the given file
	 * 
	 * @param fileName
	 * @return
	 */
	private Map<String, TestCase> getTestCases(String fileName) {
		//JUnitTestReader parser = new JUnitTestReader(
		 //       System.getProperty("java.class.path").split(":"), new String[0]);
		Map<String, TestCase> tests = new HashMap<String, TestCase>();
		// TODO:
		// tests.putAll(parser.readTests(fileName));
		if (tests.isEmpty()) {
			System.err.println("Found no parsable test cases in file " + fileName);
			System.exit(1);
		}
		return tests;
	}

	protected String testToString(TestCase test, String name) {

		StringBuilder builder = new StringBuilder();
		builder.append("\n");
		builder.append("  // Replacement test");
		builder.append("\n");
		builder.append("  @Test\n  public void " + name + "() ");
		builder.append(" {\n");
		String testString = test.toCode();
		for (String line : testString.split("\\r?\\n")) {
			builder.append("      ");
			builder.append(line);
			// builder.append(";\n");
			builder.append("\n");
		}
		builder.append("   }\n");

		return builder.toString();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
