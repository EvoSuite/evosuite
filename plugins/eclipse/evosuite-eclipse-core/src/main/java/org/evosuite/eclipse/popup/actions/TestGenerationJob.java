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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.ui.actions.OrganizeImportsAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.classpath.ResourceList;
import org.evosuite.eclipse.Activator;
import org.evosuite.eclipse.properties.EvoSuitePreferencePage;
import org.evosuite.eclipse.properties.EvoSuitePropertyPage;
import org.evosuite.eclipse.quickfixes.MarkerWriter;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.rmi.MasterServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.rmi.service.ClientStateInformation;
import org.evosuite.rmi.service.MasterNodeLocal;

/**
 * @author Gordon Fraser, Thomas White, Jose Miguel Rojas
 * 
 */
public class TestGenerationJob extends Job {

	protected final String targetClass;
	protected final String suiteClass;

	protected final IResource target;

	protected final Shell shell;

	protected boolean running = false;
	protected boolean stopped = false;
	protected boolean writeAllMarkers = true;
	protected ClientStateInformation lastState = null;

	protected String lastTest = "";
	protected static final double SEED_CHANCE = 0.2;
	protected final String ENCODING = "UTF-8";
	protected String classPath;	
	protected final Map<String, Double> coverage = null;

	public TestGenerationJob(Shell shell, final IResource target, String targetClass) {
		this(shell, target, targetClass, null);
	}

	public IResource getTarget() {
		return target;
	}
	
	public TestGenerationJob(Shell shell, final IResource target, String targetClassName, String suiteClassName) {
		super("EvoSuite Test Generation: " + targetClassName);
		this.targetClass = targetClassName;
		if (suiteClassName == null || suiteClassName.isEmpty()) {
			String tmp = targetClassName.replace('.', File.separatorChar);
			suiteClassName = new String(tmp + Properties.JUNIT_SUFFIX + ".java");
		}
		this.suiteClass = suiteClassName;
		this.target = target;
		this.shell = shell;
		IJavaProject jProject = JavaCore.create(target.getProject());
		try {
			classPath = target.getWorkspace().getRoot().findMember(jProject.getOutputLocation()).getLocation().toOSString();
		} catch (JavaModelException e) {
			e.printStackTrace();
			classPath = "";
		}
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		Boolean disabled = System.getProperty("evosuite.disable") != null; //  && System.getProperty("evosuite.disable").equals("1")
		if ( disabled ) {
			System.out.println("TestGenerationJob: The EvoSuite plugin is disabled :(");
			return Status.OK_STATUS;
		}

		final String suiteFileName = getSuiteFileName(suiteClass);
		final IFile fileSuite = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(suiteFileName));
		if ( fileSuite != null && fileSuite.exists()) {
			// Open test suite in editor
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					IWorkbenchWindow iw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IWorkbenchPage page = iw.getActivePage();
					try {
						IDE.openEditor(page, fileSuite, true);
					} catch (PartInitException e1) {
						System.out.println("Could not open test suite");
						e1.printStackTrace();
					}
				}
			});
			
			// Generated tests should be checked by tester?
			Boolean checkMarkers = System.getProperty("evosuite.markers.enforce") != null;
			if ( checkMarkers ) {

				String fileContents = readFileToString(suiteFileName);

				ASTParser parser = ASTParser.newParser(AST.JLS8);
				parser.setKind(ASTParser.K_COMPILATION_UNIT);
				parser.setStatementsRecovery(true);

				@SuppressWarnings("unchecked")
				Map<String, String> COMPILER_OPTIONS = new HashMap<String, String>(JavaCore.getOptions());
				COMPILER_OPTIONS.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
				COMPILER_OPTIONS.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_7);
				COMPILER_OPTIONS.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);

				parser.setUnitName(suiteClass);
				String[] encodings = { ENCODING };
				String[] classpaths = { classPath };
				String[] sources = { new File(suiteFileName).getParent() };
				parser.setEnvironment(classpaths, sources, encodings, true);
				parser.setSource(fileContents.toCharArray());

				CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
				final List<String> uncheckedMethods = new ArrayList<String>();
				compilationUnit.accept(new ASTVisitor() {
					@Override
					public boolean visit(MemberValuePair node) {
						if (node.getName().toString().equals("checked") && ! ((BooleanLiteral)node.getValue()).booleanValue()) { 
							NormalAnnotation ann = (NormalAnnotation) node.getParent();
							MethodDeclaration method = (MethodDeclaration)ann.getParent();
							uncheckedMethods.add(method.getName().toString());
							return false;
						}
						return true;
					}
				});
				if (uncheckedMethods.size() > 0) {
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							MessageDialog dialog = new MessageDialog(
									shell,
									"JUnit test suite contains unit tests that need to be checked",
									null, // image
									"The JUnit test suite "
									+ suiteClass
									+ " contains test cases that need to be checked:\n"
									+ uncheckedMethods.toString(),
									MessageDialog.OK, new String[] { "Ok" }, 0);
							dialog.open();
						}					
					});
					return Status.OK_STATUS;
				}
			} else
				System.out.println("Not checking markers.");
		} else {
			System.out.println("File " + suiteFileName + " does not exist");
			// TODO: Dialog
//			Display.getDefault().syncExec(new Runnable() {
//				@Override
//				public void run() {
//					MessageDialog dialog = new MessageDialog(
//							shell,
//							"Error during test generation",
//							null, // image
//							"EvoSuite failed to generate tests for class"
//							+ suiteClass,
//							MessageDialog.OK, new String[] { "Ok" }, 0);
//					dialog.open();
//				}					
//			});
//			return Status.CANCEL_STATUS;
		}
		
		setThread(new Thread());
		running = true;
		
		clearMarkersTarget();

		String oldTgr = getOldTestGenerationResult();
		lastTest = oldTgr;
		
		ArrayList<TestGenerationResult> results = runEvoSuite(monitor);
		writeMarkersTarget(results);
		//uncomment after experiment
		if (writeAllMarkers)
			writeMarkersTestSuite();
		
		try {
			target.getProject().refreshLocal(IProject.DEPTH_INFINITE, null);
//			if ("true".equals(target.getProject().getPersistentProperty(
//					EvoSuitePropertyPage.REPORT_PROP_KEY))) {
//				syncWithUi(target);
//			};
			
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						final IFile generatedSuite = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(suiteFileName));
						ICompilationUnit cu=JavaCore.createCompilationUnitFrom(generatedSuite);
						IWorkbenchWindow iw = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
						IWorkbenchPage page = iw.getActivePage();
						IEditorPart part = IDE.openEditor(page, generatedSuite, true);
						if ( Activator.organizeImports() ) {
							OrganizeImportsAction a=new OrganizeImportsAction(part.getSite());
							a.run(cu);
							cu.commitWorkingCopy(true, null);
							cu.save(null, true);
						}
					} catch (PartInitException e1) {
						System.out.println("Could not open test suite to organize imports");
						e1.printStackTrace();
					} catch (JavaModelException e) {
						System.out.println("Something went wrong while saving test suite after organizing imports");
						e.printStackTrace();
					};
				}});
		} catch (CoreException e) {
			System.out.println("Dear me");
			e.printStackTrace();
		}
		Activator.CURRENT_WRITING_FILE = null;
		running = false;
		monitor.done();
		done(ASYNC_FINISH);
		Activator.FILE_QUEUE.update();
		return Status.OK_STATUS;
	}

	protected ArrayList<TestGenerationResult> runEvoSuite(final IProgressMonitor monitor) {
		monitor.beginTask("EvoSuite test suite generation", 100);
		ArrayList<TestGenerationResult> tgrs = new ArrayList<TestGenerationResult>();
		try {
			List<String> commands = createCommand();
			commands.addAll(getAdditionalParameters());
			String[] command = new String[commands.size()];
			commands.toArray(command);
			System.out.println("* EvoSuite command: " + Arrays.asList(command));

			setupRMI();
			Thread progressMonitor = new Thread() {

				@Override
				public void run() {

					int percent = 0;
					int last = 0;
					String subTask = "";
					//try {
					while (percent != -1 && !isInterrupted()) {
						MasterNodeLocal masterNode = MasterServices
								.getInstance().getMasterNode();
						if (masterNode != null) {
							Collection<ClientStateInformation> currentStates = MasterServices
									.getInstance().getMasterNode()
									.getCurrentStateInformation();
							if (currentStates.size() == 1) {
								ClientStateInformation currentState = currentStates
										.iterator().next();
								lastState = currentState;

								percent = currentState.getOverallProgress();
								if (percent >= 100
										&& currentState.getState() == ClientState.NOT_STARTED)
									continue;

								String currentTask = currentState
										.getState().getDescription();
//								+ " ["
//								+ currentState.getState()
//								.getOverallProgress()
//								+ "%]";
								if (percent > last || !subTask.equals(currentTask)) {
									subTask = currentTask;
									monitor.worked(percent - last);
									monitor.subTask(subTask);
									last = percent;
								}
							}
						}
						try {
							sleep(250); // TODO - should use observer pattern
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
							System.out.println("* Shut down progress monitor");
						}
					//} catch (Exception e) {
					//	System.err.println(this.getClass().getCanonicalName() + ": Exception while reading output of client process " + e);
					//	System.err.println(e.getStackTrace().toString());
					//}
					}
				}
			};

			progressMonitor.start();

			tgrs = launchProcess(command);

			progressMonitor.interrupt();

			try {
				target.getProject().refreshLocal(IProject.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			System.out.println("Job returned normally");
			monitor.done();

			/*
			GenerationResult result = new GenerationResult(shell, SWT.DIALOG_TRIM
			        | SWT.APPLICATION_MODAL);
			result.open();
			return Status.OK_STATUS;
			*/
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
		
		return tgrs;
	}

	private String buildProjectCP() throws JavaModelException {
		IJavaProject jProject = JavaCore.create(target.getProject());
		IClasspathEntry[] oldEntries = jProject.getRawClasspath();
		String classPath = "";
		boolean first = true;

		for (int i = 0; i < oldEntries.length; i++) {
			IClasspathEntry curr = oldEntries[i];
			System.out.println("Current entry: " + curr.getPath());

			if (curr.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				IPath path = curr.getPath();
				if (path.toFile().getName().startsWith("evosuite")) {
					System.out.println("Skipping evosuite.jar");
					continue;
				}
				if (!first)
					classPath += File.pathSeparator;
				else
					first = false;

				if (path.toFile().exists()) {
					classPath += path.toOSString();
					System.out.println("Adding CPE_LIBRARY to classpath: "
							+ path.toOSString());
				} else {
					classPath += target.getWorkspace().getRoot()
							.getLocation().toOSString()
							+ path.toOSString();
					System.out.println("Adding CPE_LIBRARY to classpath: "
							+ target.getWorkspace().getRoot().getLocation()
									.toOSString() + path.toOSString());
				}
			} else if (curr.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				if (curr.isExported()) {
					if (curr.toString().equals(
							"org.eclipse.jdt.launching.JRE_CONTAINER")) {
						System.out.println("Found JRE container");
					} else if (curr.toString().startsWith(
							"org.eclipse.jdt.junit.JUNIT_CONTAINER")) {
						System.out.println("Found JUnit container");
					} else {
						System.out.println("Found unknown container: "
								+ curr);
					}
				} else {
					System.out.println("Container not exported: " + curr);
				}
			} else if (curr.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
				// Add binary dirs of this project to classpath
				System.out.println("Don't handle CPE_PROJECT yet");
			} else if (curr.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
				System.out.println("Path: " + curr.getPath());
				System.out.println("Resolved Path: "
						+ JavaCore.getResolvedVariablePath(curr.getPath()));
				if (!first)
					classPath += File.pathSeparator;
				else
					first = false;

				classPath += JavaCore.getResolvedVariablePath(curr
						.getPath());
			} else if (curr.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				System.out.println("Don't handle CPE_SOURCE yet");
			} else {
				System.out.println("CP type: " + curr.getEntryKind());
			}
		}
		ResourceList.resetAllCaches();
		if (!first)
			classPath += File.pathSeparator;

		classPath += target.getWorkspace().getRoot()
				.findMember(jProject.getOutputLocation()).getLocation()
				.toOSString();
		return classPath;
	}

	@SuppressWarnings("unused")
	private boolean isFinished(Process process) {
		try {
			process.exitValue();
			return true;
		} catch (IllegalThreadStateException ex) {
			return false;
		}
	}

	private ArrayList<TestGenerationResult> launchProcess(String[] evoSuiteOptions) throws IOException {
		EvoSuite evosuite = new EvoSuite();

		Vector<String> javaCmd = new Vector<String>();
		// javaCmd.add("java");
		// javaCmd.add("-jar");
		// javaCmd.add(TestGenerationAction.getEvoSuiteJar());
		Collections.addAll(javaCmd, evoSuiteOptions);

		String[] command = javaCmd.toArray(new String[] {});
		@SuppressWarnings("unchecked")
		List<List<TestGenerationResult>> results = (List<List<TestGenerationResult>>) evosuite.parseCommandLine(command);
		ArrayList<TestGenerationResult> tgrs = new ArrayList<TestGenerationResult>();
		System.out.println("Results: "+results.size());
		for(List<TestGenerationResult> list : results) {
			for(TestGenerationResult result : list) {
				tgrs.add(result);
				System.out.println("Covered lines: " + result.getCoveredLines());
			}
		}
		return tgrs;
//		ProcessBuilder builder = new ProcessBuilder(command);
//		builder.directory(new File(baseDir));
//		builder.redirectErrorStream(true);
//
//		Process process = builder.start();
//		InputStream stdout = process.getInputStream();
//		do {
//			readInputStream("EvoSuite process output - ", stdout);
//		} while (!isFinished(process));

	}

	@SuppressWarnings("unused")
	private void readInputStream(String prefix, InputStream in) throws IOException {
		InputStreamReader is = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(is);
		String read = br.readLine();
		while (read != null) {
			System.out.println(prefix + read);
			read = br.readLine();
		}
	}

	protected void syncWithUi(final IResource target) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				GenerationResult result = new GenerationResult(shell,
						SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, target
								.getProject().getLocation(), target);
				result.open();
				// MessageDialog.openInformation(shell, "Your Popup ", "Your job has finished.");
			}
		});

	}

	@SuppressWarnings("unused")
	private void showFile(IPath targetFile) {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		try {
			IDE.openEditor(page, ResourcesPlugin.getWorkspace().getRoot()
					.getFileForLocation(targetFile));
		} catch (PartInitException e2) {
			//Put your exception handler here if you wish to.
			System.out.println("Error: " + e2);
		}
	}


	protected List<String> createCommand() throws CoreException {
		List<String> commands = new ArrayList<String>();
		
		String classPath = buildProjectCP();

		String baseDir = target.getProject().getLocation().toOSString();
		Properties.TARGET_CLASS = targetClass;

		int time = Activator.getDefault().getPreferenceStore().getInt("runtime");
		commands.addAll(Arrays.asList(new String[] {
				"-generateSuite",
				"-class", targetClass,
				"-evosuiteCP", TestGenerationAction.getEvoSuiteJar(), 
				"-projectCP", classPath, 
				"-base_dir", baseDir, 
				"-Dshow_progress=false", 
				"-Dstopping_condition=TimeDelta", 
				"-Dtest_comments=false", // "true"
				"-Dsearch_budget=" + time, 
				"-Dassertion_timeout=" + time, 
				"-Dpure_inspectors=true", 
				"-Dnew_statistics=false"
				// "-Dsandbox_mode=IO",
				// "-Djava.rmi.server.codebase=file:///Remote/evosuite-0.1-SNAPSHOT-jar-minimal.jar"
				}));
		
		if ( System.getProperty("evosuite.experiment") != null ) {
			commands.add("-Declipse_plugin=true");
		}
		
		String budget = target.getProject().getPersistentProperty(
				EvoSuitePropertyPage.TIME_PROP_KEY);
		if (budget == null) {
			commands.add("-Dsearch_budget=20");
		} else {
			commands.add("-Dsearch_budget=" + budget);
		}
		
		String globalBudget = target.getProject().getPersistentProperty(
				EvoSuitePropertyPage.GLOBAL_TIME_PROP_KEY);
		if (globalBudget == null) {
			commands.add("-Dglobal_timeout=60");
		} else {
			commands.add("-Dglobal_timeout=" + globalBudget);
		}

		;
		if ("false".equals(target.getProject().getPersistentProperty(
				EvoSuitePropertyPage.ASSERTION_PROP_KEY))) {
			commands.add("-Dassertions=false");
		} else {
			commands.add("-Dassertions=true");				
		}
		
		if ("false".equals(target.getProject().getPersistentProperty(
				EvoSuitePropertyPage.MINIMIZE_TESTS_PROP_KEY))) {
			commands.add("-Dminimize=false");
		} else {
			commands.add("-Dminimize=true");
		}
		if (!"false".equals(target.getProject().getPersistentProperty(
				EvoSuitePropertyPage.MINIMIZE_VALUES_PROP_KEY))) {
			commands.add("-Dminimize_values=true");
		} else {
			commands.add("-Dminimize_values=false");
		}
		// if (!"true".equals(target.getProject().getPersistentProperty(EvosuitePropertyPage.RUNNER_PROP_KEY))) {
		//			//	commands.add("-Djunit_runner=false");
		//	commands.add("-Dreplace_calls=false");
		//} else {
		//	commands.add("-Djunit_runner=true");
		if ("true".equals(target.getProject().getPersistentProperty(
				EvoSuitePropertyPage.DETERMINISTIC_PROP_KEY))) {
			commands.add("-Dreplace_calls=true");
			commands.add("-Dreplace_system_in=true");
			commands.add("-Dreset_static_fields=true");
			commands.add("-Dvirtual_fs=true");
		} else {
			commands.add("-Dreplace_calls=false");
			commands.add("-Dreplace_system_in=false");
			commands.add("-Dreset_static_fields=false");
			commands.add("-Dvirtual_fs=false");
		}
		//	}

//		if (!"true".equals(target.getProject().getPersistentProperty(
//				EvoSuitePropertyPage.REPORT_PROP_KEY))) {
//			//commands.add("-Dstatistics_backend=none");
//		} else {
//			if ("true".equals(target.getProject().getPersistentProperty(
//					EvoSuitePropertyPage.PLOT_PROP_KEY))) {
//				commands.add("-Dplot=true");
//			}
//		}
		if ("false".equals(target.getProject().getPersistentProperty(
				EvoSuitePropertyPage.SANDBOX_PROP_KEY))) {
			commands.add("-Dsandbox=false");
		}
		if ("false".equals(target.getProject().getPersistentProperty(
				EvoSuitePropertyPage.SCAFFOLDING_PROP_KEY))) {
			commands.add("-Dtest_scaffolding=false");
			commands.add("-Dno_runtime_dependency=true");
		}
		if ("true".equals(target.getProject().getPersistentProperty(
				EvoSuitePropertyPage.CONTRACTS_PROP_KEY))) {
			commands.add("-Dcheck_contracts=true");
		}
		if ("true".equals(target.getProject().getPersistentProperty(
				EvoSuitePropertyPage.ERROR_BRANCHES_PROP_KEY))) {
			commands.add("-Derror_branches=true");
		}
		if ("true".equals(target.getProject().getPersistentProperty(
				EvoSuitePropertyPage.DSE_PROP_KEY)) || 
			"true".equals(target.getProject().getPersistentProperty(
				EvoSuitePropertyPage.LS_PROP_KEY))) {
			commands.add("-Dlocal_search_rate=10");
			commands.add("-Dlocal_search_probability=1.0");
			commands.add("-Dlocal_search_adaptation_rate=1.0");
			commands.add("-Dlocal_search_selective=true");
			// commands.add("-Dlocal_search_selective_primitives=false");
			//commands.add("-Dlocal_search_dse=suite");
			commands.add("-Dlocal_search_budget_type=time");
			commands.add("-Dlocal_search_budget=15");

		}
		if ("true".equals(target.getProject().getPersistentProperty(
				EvoSuitePropertyPage.DSE_PROP_KEY))) {
//			commands.add("-Dlocal_search_rate=1");
//			commands.add("-Dlocal_search_probability=1.0");
//			commands.add("-Dlocal_search_adaptation_rate=1.0");
//			commands.add("-Dlocal_search_selective=false");
//			commands.add("-Dlocal_search_selective_primitives=false");
			commands.add("-Dlocal_search_dse=suite");
			// commands.add("-Dlocal_search_budget_type=time");
			// commands.add("-Dlocal_search_budget=15");
		}
//		if ("true".equals(target.getProject().getPersistentProperty(
//				EvosuitePropertyPage.LS_PROP_KEY))) {
//			commands.add("-Dlocal_search_rate=1");
//			commands.add("-Dlocal_search_probability=1.0");
//			commands.add("-Dlocal_search_adaptation_rate=1.0");
//			commands.add("-Dlocal_search_selective=false");
//			commands.add("-Dlocal_search_selective_primitives=false");
//			commands.add("-Dlocal_search_dse=suite");
//			commands.add("-Dlocal_search_budget_type=time");
//			commands.add("-Dlocal_search_budget=15");
//		}
		String suffix = target.getProject().getPersistentProperty(
				EvoSuitePropertyPage.TEST_SUFFIX_PROP_KEY);
		if(suffix != null) {
			commands.add("-Djunit_suffix="+suffix);
		}
		/*
		String seed = target.getProject().getPersistentProperty(
				EvosuitePropertyPage.SEED_PROP_KEY);
		if (seed != null) {
			commands.add("-seed");
			commands.add(seed);
		}
		*/
		String criterion = target.getProject().getPersistentProperty(
				EvoSuitePropertyPage.CRITERIA_PROP_KEY);
		if (criterion != null) {
			commands.add("-criterion");
			commands.add(criterion);
		}
		
		if (Activator.getDefault().getPreferenceStore().getBoolean(EvoSuitePreferencePage.TEST_COMMENTS)) {
			commands.add("-Dtest_comments=true");
		}
		return commands;
	}


	private void createIFolder(IFolder folder) throws CoreException {
		if (!folder.exists()) {
			createIFolder((IFolder) folder.getParent());
			folder.create(IResource.NONE, true, null);
		}
	}

	private void saveTestGenerationResult(TestGenerationResult result) {
		if (target.getProject() != null) {
			int lastDot = targetClass.lastIndexOf(".");
			String filePackage = "";
			if (lastDot >= 1) {
				filePackage = targetClass.substring(0, lastDot);
			}
			IProject project = target.getProject();
			IFolder folder;
			if (filePackage.length() == 0) {
				folder = project.getFolder("evosuite-tests/data");
			} else {
				folder = project.getFolder("evosuite-tests/data/"
						+ filePackage.replace('.', '/'));
			}
			if (!folder.exists()) {
				try {
					createIFolder(folder);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			IFile file = folder.getFile(target.getName() + ".gadata");
			if (file.exists()) {
				try {
					file.delete(true, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			try {
				while (!file.getParent().exists()) {
					IContainer icont = file.getParent();
					while (!icont.getParent().exists()) {
						if (icont.getParent() == null) {
							icont = (IContainer) ResourcesPlugin.getWorkspace();
						}
					}
					IFolder f = project.getFolder(icont
							.getProjectRelativePath());
					f.create(IResource.NONE, true, null);
				}
				file.create(new ByteArrayInputStream("".getBytes()), true, null);
				FileOutputStream fos = new FileOutputStream(file.getLocation()
						.toOSString());
				ObjectOutputStream oo = new ObjectOutputStream(fos);

				oo.writeObject(result);
				
				// close stream
				oo.close();
				// file.create(s, IResource.NONE, null);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	protected String getOldTestGenerationResult() {
		if (target.getProject() != null) {
			IProject project = target.getProject();
			int lastDot = targetClass.lastIndexOf(".");
			String filePackage = "";
			if (lastDot >= 1) {
				filePackage = targetClass.substring(0, lastDot);
			}
			IFolder folder;
			if (filePackage.length() == 0) {
				folder = project.getFolder("evosuite-tests/data");
			} else {
				folder = project.getFolder("evosuite-tests/data/"
						+ filePackage.replace('.', '/'));
			}
			IFile file = folder.getFile(target.getName() + ".gadata");
			return file.getLocation().toOSString();

		}
		return "";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#canceling()
	 */
	@Override
	protected void canceling() {
		System.out.println("Trying to cancel job");
		if (MasterServices.getInstance() != null
				&& MasterServices.getInstance().getMasterNode() != null) {
			MasterServices.getInstance().getMasterNode().cancelAllClients();
		}
		super.canceling();
	}

	/**
	 * Getting RMI to work in Eclipse is tricky. These hacks are necessary,
	 * otherwise it won't work
	 */
	protected void setupRMI() {
		// RMI only runs if there is a security manager
		if (System.getSecurityManager() == null) {
			// As there is no security manager, we can just put a dumb security
			// manager that allows everything here, just to make RMI happy
			System.setSecurityManager(new DumbSecurityManager());
		}
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
	}

	protected void clearMarkersTarget() {
		try {
			MarkerWriter.clearMarkers(target);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	protected void writeMarkersTarget(ArrayList<TestGenerationResult> results) {
		clearMarkersTarget();
		if (Activator.markersEnabled()) {
			System.out.println("**********  Writing markers in target class " + targetClass);
			for (TestGenerationResult result : results) {
				// any lines covered?
				if (result.getCoveredLines().size() > 0) {
					if (!stopped) {
						MarkerWriter.write(target, result);
					}
					saveTestGenerationResult(result);
				}
			}
		} else
			System.out.println("**********  Markers are disabled");
	}

	protected String getSuiteFileName(String suiteClass) {
		String tmp = suiteClass.replace('.', File.separatorChar);
		String testClassFileName = new String(target.getProject().getLocation() + "/evosuite-tests/" + tmp + ".java");
		return testClassFileName;
	}

	protected void writeMarkersTestSuite() {
		if (Activator.markersEnabled()) {
			System.out.println("**********  Writing markers in test suite" + suiteClass);

			String testClassFileName = getSuiteFileName(suiteClass);

			final IFile fileTestClass = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(testClassFileName));

			String fileContents = readFileToString(testClassFileName);
			if (fileContents.isEmpty()) {
				System.out.println("Not writing markers in test suite " + testClassFileName + " (not found)");
				return;
			}

			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setStatementsRecovery(true);

			@SuppressWarnings("unchecked")
			Map<String, String> COMPILER_OPTIONS = new HashMap<String, String>(JavaCore.getOptions());
			COMPILER_OPTIONS.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
			COMPILER_OPTIONS.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_7);
			COMPILER_OPTIONS.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);

			parser.setUnitName(suiteClass);
			String[] encodings = { ENCODING };
			String[] classpaths = { classPath };
			String[] sources = { new File(testClassFileName).getParent() };
			parser.setEnvironment(classpaths, sources, encodings, true);
			parser.setSource(fileContents.toCharArray());

			CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
			MethodExtractingVisitor visitor = new MethodExtractingVisitor();
			compilationUnit.accept(visitor);
			List<MethodDeclaration> methods = visitor.getMethods();
		
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					IWorkbenchWindow iw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IWorkbenchPage page = iw.getActivePage();
					try {
						IDE.openEditor(page, fileTestClass, true);
					} catch (PartInitException e1) {
						System.out.println("Could not open test suite");
						e1.printStackTrace();
					}
				}
			});

			for (MethodDeclaration m : methods) {
				int lineNumber = compilationUnit.getLineNumber(m.getStartPosition());
				try {
					IMarker mark = fileTestClass.createMarker("EvoSuiteQuickFixes.newtestmarker");
					mark.setAttribute(IMarker.MESSAGE, "This test case needs to be verified.");
					mark.setAttribute(IMarker.LINE_NUMBER, lineNumber);
					mark.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
					mark.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
					mark.setAttribute(IMarker.LOCATION, fileTestClass.getName());
					mark.setAttribute(IMarker.CHAR_START, m.getStartPosition());
					mark.setAttribute(IMarker.CHAR_END, m.getStartPosition() + 1);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		} else
			System.out.println("**********  Markers are disabled");
	}
	
	public static String readFileToString(String fileName) {
		StringBuilder content = new StringBuilder();
		try {
			Reader reader = new InputStreamReader(
					new FileInputStream(fileName), "utf-8");
			BufferedReader in = new BufferedReader(reader);
			try {
				String str;
				while ((str = in.readLine()) != null) {
					content.append(str); content.append("\n");
				}
			} finally {
				in.close();
			}
		} catch (FileNotFoundException fnfe) {
			System.out.println("File not found " + fileName);
			return "";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return content.toString();
	}

	public boolean isRunning() {
		return running;
	}

	public List<String> getAdditionalParameters() {
		List<String> parameters = new ArrayList<String>();
		parameters.add("-Dtest_dir=" + target.getProject().getLocation() + "/evosuite-tests");
		return parameters;
	}
}
