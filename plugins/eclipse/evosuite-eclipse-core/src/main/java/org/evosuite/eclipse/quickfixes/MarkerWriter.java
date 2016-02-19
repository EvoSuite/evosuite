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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.evosuite.result.BranchInfo;
import org.evosuite.result.Failure;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.eclipse.Activator;

public class MarkerWriter {
	private final IResource res;
	private final TestGenerationResult tgr;
	private final ClassMethodVisitor testM;
	private final ClassMethodVisitor raw;

	private MarkerWriter(IResource res, TestGenerationResult tgr) {
		this.res = res;
		this.tgr = tgr;
		testM = new ClassMethodVisitor();
		raw = new ClassMethodVisitor();
	}

	public static void clearMarkers(IResource res) throws CoreException {
		if (res.exists()) {
			res.deleteMarkers("EvoSuiteQuickFixes.exceptionmarker", true, 1);
			res.deleteMarkers("EvoSuiteQuickFixes.notcoveredmarker", true, 1);
			res.deleteMarkers("EvoSuiteQuickFixes.uncoveredlinemarker", true, 1);
			res.deleteMarkers("EvoSuiteQuickFixes.lineremovedmarker", true, 1);
		} else
			System.out.println(MessageFormat.format("Resource {} does not exist.", res).toString());
	}

	public void writeMarkers() {
		System.out.println(tgr);
		// ServerStatistics.getInstance().getX(); (getCoverage();)
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setSource(tgr.getTestSuiteCode().toCharArray());

		CompilationUnit compTest = (CompilationUnit) parser.createAST(null);
		IJavaElement jEle = JavaCore.create(res);

		if (jEle instanceof ICompilationUnit) {

			final ICompilationUnit icomp = (ICompilationUnit) jEle;
			// System.out.println(icomp);
			BufferedReader br;
			char[] varcontent = null;
			try {
				br = new BufferedReader(new FileReader(res.getLocation()
						.toFile()));
				int size = 0;
				while (br.read() != -1) {
					size++;
				}
				// String content = "";
				// String line = br.readLine();
				// content += line;
				// while ((line = br.readLine()) != null){
				// content += line;
				// }
				// parser.setSource(content.toCharArray());
				br = new BufferedReader(new FileReader(res.getLocation()
						.toFile()));
				varcontent = new char[size];
				br.read(varcontent, 0, size);
				parser.setSource(varcontent);
			} catch (FileNotFoundException e2) {
				e2.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			final char[] content = varcontent;
			final CompilationUnit compClass = (CompilationUnit) parser
					.createAST(null);
			compTest.accept(testM);
			compClass.accept(raw);
			IJavaElement element = JavaCore.create(res);
			IJavaElement packageElement = element.getParent();

			String packageName = packageElement.getElementName();

			final String className = (!packageName.isEmpty() ? packageName + "." : "")
			        + res.getName().replace(".java", "").replace(File.separator, ".");

			final char[] classContent = varcontent;
			if (tgr != null) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						ArrayList<Integer> lines = new ArrayList<Integer>();
						Set<Integer> actualLines = new HashSet<Integer>();
						actualLines.addAll(tgr.getUncoveredLines());
						actualLines.addAll(tgr.getCoveredLines());
						int contentPosition = 0;
						int line = 1;
						while (contentPosition != -2 && contentPosition != -1) {
							if (!actualLines.contains(line)) {
								lines.add(line);
							}
							contentPosition = compClass.getPosition(line, 0);
							line++;
						}

						IPreferenceStore store = Activator.getDefault()
								.getPreferenceStore();
						if (store.getBoolean("removed")) {
							for (Integer i : lines) {
								// this line couldn't be reached in the test!
								IJavaElement currentElement = null;
								int position = compClass.getPosition(i, 0);
								int maxPosition = compClass.getPosition(i + 1,
										0);
								if (position == -1 || position == -2) {
									continue;
								}
								while (position < classContent.length
										&& Character
												.isWhitespace(classContent[position])) {
									// System.out.println(classContent);
									position++;
								}
								if (position > maxPosition) {
									continue;
								}
								while (maxPosition < classContent.length
										&& Character
												.isWhitespace(classContent[maxPosition])) {
									// System.out.println(classContent);
									maxPosition++;
								}
								try {
									currentElement = icomp
											.getElementAt(position + 1);
								} catch (JavaModelException e1) {
									e1.printStackTrace();
								}
								if (isMethodDeclaration(i, currentElement,
										content, compClass, icomp)) {
									continue;
								}
								IJavaElement nextElement = null;
								int nextPosition = compClass.getPosition(i + 1,
										0);
								if (nextPosition != -1 || nextPosition != -2) {
									try {
										nextElement = icomp
												.getElementAt(nextPosition);
										if (nextElement != currentElement) {
											continue;
										}
									} catch (JavaModelException e) {
										e.printStackTrace();
									}
								}
								if (position > maxPosition) {
									continue;
								}
								while (maxPosition < classContent.length
										&& Character
												.isWhitespace(classContent[maxPosition])) {
									// System.out.println(classContent);
									maxPosition++;
								}
								try {
									currentElement = icomp
											.getElementAt(position + 1);
								} catch (JavaModelException e1) {
									e1.printStackTrace();
								}
								if (content[position] == '/'
										&& content[position + 1] == '/') {
									continue;
								}
								if (content[position] == '}') {
									continue;
								}

								if (getMethod(currentElement) == null) {
									continue;
								}
								boolean marker = shouldWriteMarkers(currentElement);
								if (marker) {
									try {
										IMarker m = res
												.createMarker("EvoSuiteQuickFixes.lineremovedmarker");
										m.setAttribute(IMarker.MESSAGE,
												"This line appears to be removed by the Java Compiler.");
										m.setAttribute(IMarker.LINE_NUMBER, i);
										m.setAttribute(IMarker.PRIORITY,
												IMarker.PRIORITY_HIGH);
										m.setAttribute(IMarker.SEVERITY,
												IMarker.SEVERITY_WARNING);
										m.setAttribute(IMarker.LOCATION,
												res.getName());
										m.setAttribute(IMarker.CHAR_START,
												position);
										m.setAttribute(IMarker.CHAR_END,
												compClass.getPosition(i + 1, 0));

									} catch (CoreException e) {
										e.printStackTrace();
									}
								}
							}
						}

						if (store.getBoolean("uncovered")) {
							for (Integer i : tgr.getUncoveredLines()) {
								// this line couldn't be reached in the test!

								IJavaElement currentElement = null;
								int position = compClass.getPosition(i, 0);
								if (position == -1) {
									continue;
								}
								while (position < classContent.length
										&& Character
												.isWhitespace(classContent[position])) {
									// System.out.println(classContent);
									position++;
								}
								try {
									currentElement = icomp
											.getElementAt(position + 1);
								} catch (JavaModelException e1) {
									e1.printStackTrace();
								}
								boolean marker = shouldWriteMarkers(currentElement);
								if (marker) {
									try {
										IMarker m = res
												.createMarker("EvoSuiteQuickFixes.uncoveredlinemarker");
										m.setAttribute(IMarker.LINE_NUMBER, i);
										m.setAttribute(IMarker.PRIORITY,
												IMarker.PRIORITY_HIGH);
										m.setAttribute(IMarker.SEVERITY,
												IMarker.SEVERITY_WARNING);
										m.setAttribute(IMarker.LOCATION,
												res.getName());
										m.setAttribute(IMarker.CHAR_START,
												position);
										m.setAttribute(IMarker.CHAR_END,
												compClass.getPosition(i + 1, 0));

									} catch (CoreException e) {
										e.printStackTrace();
									}
								}
							}
						}

						for (BranchInfo bi : tgr.getUncoveredBranches()) {
							int j = bi.getLineNo();
							IJavaElement currentElement = null;
							int position = compClass.getPosition(j, 0);
							while (position < classContent.length
									&& Character
											.isWhitespace(classContent[position])) {
								// System.out.println(classContent);
								position++;
							}
							try {
								currentElement = icomp
										.getElementAt(position + 1);
							} catch (JavaModelException e1) {
								e1.printStackTrace();
							}
							boolean marker = shouldWriteMarkers(currentElement);
							if (marker) {
								try {
									IMarker m = res
											.createMarker("EvoSuiteQuickFixes.notcoveredmarker");
									m.setAttribute(
											IMarker.MESSAGE,
											"This branch (starting line "
													+ j
													+ ") could not be covered by EvoSuite.");
									m.setAttribute(IMarker.LINE_NUMBER, j);
									m.setAttribute(IMarker.PRIORITY,
											IMarker.PRIORITY_HIGH);
									m.setAttribute(IMarker.SEVERITY,
											IMarker.SEVERITY_WARNING);
									m.setAttribute(IMarker.LOCATION,
											res.getName());
									m = res.createMarker("EvoSuiteQuickFixes.uncoveredlinemarker");
									// m.setAttribute(IMarker.LINE_NUMBER, j);
									m.setAttribute(IMarker.PRIORITY,
											IMarker.PRIORITY_HIGH);
									m.setAttribute(IMarker.SEVERITY,
											IMarker.SEVERITY_WARNING);
									m.setAttribute(IMarker.LOCATION,
											res.getName());
									m.setAttribute(IMarker.CHAR_START, position);
									m.setAttribute(IMarker.CHAR_END,
											compClass.getPosition(j + 1, 0) - 1);

									m.setAttribute(IMarker.CHAR_START, position);
									m.setAttribute(IMarker.CHAR_END,
											compClass.getPosition(j + 1, 0) - 1);

								} catch (CoreException e) {
									e.printStackTrace();
								}
							}
						}

						for (MethodDeclaration method : testM.getMethods()) {
							String test = method.getName()
									.getFullyQualifiedName();
							Set<Failure> failures = tgr
									.getContractViolations(test);
							if (failures != null && failures.size() != 0) {
								// uncaught Exception!
								try {
									for (Failure f : failures) {
										if (f != null) {
											int lineNumber = 1;
											String message = "";
											for (int i = 0; i < f
													.getStackTrace().length; i++) {
												if (f.getStackTrace()[i]
														.getClassName().equals(
																className)) {
													boolean found = false;

													for (MethodDeclaration method2 : raw
															.getMethods()) {
														String s = method2
																.getName()
																.getFullyQualifiedName();
														if (s.equals(f
																.getStackTrace()[i]
																.getMethodName())) {
															found = true;
															break;
														}
													}
													if (found) {
														message = f
																.getStackTrace()[i]
																.toString();
														lineNumber = f
																.getStackTrace()[i]
																.getLineNumber();
														break;
													}
												}
											}
											IJavaElement currentElement = null;
											int position = compClass
													.getPosition(lineNumber, 0);
											while (position < classContent.length
													&& Character
															.isWhitespace(classContent[position])) {
												// System.out.println(classContent);
												position++;
											}
											try {
												currentElement = icomp
														.getElementAt(position + 1);
											} catch (JavaModelException e1) {
												e1.printStackTrace();
											}
											boolean marker = shouldWriteMarkers(currentElement);
											if (marker) {
												IMarker m = res
														.createMarker("EvoSuiteQuickFixes.exceptionmarker");
												m.setAttribute(IMarker.MESSAGE,
														f.getExceptionName()
																+ " detected "
																+ message);
												m.setAttribute(
														IMarker.LINE_NUMBER,
														lineNumber);
												m.setAttribute(
														IMarker.PRIORITY,
														IMarker.PRIORITY_HIGH);
												m.setAttribute(
														IMarker.SEVERITY,
														IMarker.SEVERITY_WARNING);
												m.setAttribute(
														IMarker.LOCATION,
														res.getName());
												while (position < classContent.length
														&& Character
																.isWhitespace(classContent[position])) {
													// System.out.println(classContent);
													position++;
												}
												m.setAttribute(
														IMarker.CHAR_START,
														position);
												m.setAttribute(
														IMarker.CHAR_END,
														compClass.getPosition(
																lineNumber + 1,
																0) - 1);
											}
										}
									}
								} catch (CoreException e) {
									e.printStackTrace();
								}
							}

						}

					}

				});
			}
		}
	}

	public IJavaElement getMethod(IJavaElement e) {
		IJavaElement method = e;
		while (method != null && method.getElementType() != IJavaElement.METHOD) {
			method = method.getParent();
		}
		return method;

	}

	private boolean isMethodDeclaration(int lineNumber,
			IJavaElement currentElement, char[] content, CompilationUnit cu,
			ICompilationUnit icu) {
		IJavaElement previousElement = null;
		int prevPosition = cu.getPosition(lineNumber - 1, 0);
		int limit = cu.getPosition(lineNumber, 0);
		while (Character.isWhitespace(content[limit])) {
			// System.out.println(classContent);
			limit++;
		}
		limit++;
		if (prevPosition != -1 && prevPosition != -2) {
			try {
				previousElement = icu.getElementAt(prevPosition + 1);
				if (previousElement != currentElement) {
					return true;
				} else {
					for (int i = prevPosition; i < limit; i++) {
						if (content[i] == '{') {
							return false;
						}
					}
					return isMethodDeclaration(lineNumber - 1, currentElement,
							content, cu, icu);
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private boolean shouldWriteMarkers(IJavaElement currentElement) {
		IJavaElement parent = currentElement;
		while (parent != null) {

			if (parent instanceof IAnnotatable) {
				IAnnotatable p = (IAnnotatable) parent;
				try {
					for (IAnnotation a : p.getAnnotations()) {
						if (a.getElementName().equalsIgnoreCase("EvoIgnore")) {
							return false;
						}
					}
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}
			parent = parent.getParent();
		}
		return true;
	}

	public static void write(IResource res, TestGenerationResult tgr) {
		MarkerWriter mr = new MarkerWriter(res, tgr);
		mr.writeMarkers();
	}

}
