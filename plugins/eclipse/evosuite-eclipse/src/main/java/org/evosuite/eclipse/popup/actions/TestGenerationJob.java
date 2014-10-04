/**
 * 
 */
package org.evosuite.eclipse.popup.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.classpath.ResourceList;
import org.evosuite.eclipse.properties.EvosuitePropertyPage;
import org.evosuite.rmi.MasterServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.rmi.service.ClientStateInformation;
import org.evosuite.rmi.service.MasterNodeLocal;

/**
 * @author Gordon Fraser
 * 
 */
public class TestGenerationJob extends Job {

	protected final String targetClass;

	protected final IResource target;

	protected final Shell shell;

	protected ClientStateInformation lastState = null;

	protected final Map<String, Double> coverage = null;

	public TestGenerationJob(Shell shell, final IResource target,
			String targetClass) {
		super("EvoSuite Test Generation: " + targetClass);
		this.targetClass = targetClass;
		this.target = target;
		this.shell = shell;
	}

	public List<String> getAdditionalParameters() {
		return new ArrayList<String>();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.jobs.Job#canceling()
	 */
	@Override
	protected void canceling() {
		System.out.println("Trying to cancel job");
		MasterServices.getInstance().getMasterNode().cancelAllClients();
		super.canceling();
	}

	/**
	 * Getting RMI to work in Eclipse is tricky. These hacks are necessary,
	 * otherwise it won't work
	 */
	private void setupRMI() {
		// RMI only runs if there is a security manager
		if (System.getSecurityManager() == null) {
			// As there is no security manager, we can just put a dumb security
			// manager that allows everything here, just to make RMI happy
			System.setSecurityManager(new DumbSecurityManager());
		}
		Thread.currentThread().setContextClassLoader(
				this.getClass().getClassLoader());
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {

		monitor.beginTask("EvoSuite test suite generation", 100);

		try {
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
			ResourceList.resetCache();
			if (!first)
				classPath += File.pathSeparator;

			classPath += target.getWorkspace().getRoot()
					.findMember(jProject.getOutputLocation()).getLocation()
					.toOSString();

			String baseDir = target.getProject().getLocation().toOSString();
			Properties.TARGET_CLASS = targetClass;
			List<String> commands = new ArrayList<String>();
			commands.addAll(Arrays.asList(new String[] {
					"-generateSuite",
					"-class",
					targetClass,
					"-Dtest_dir=" + target.getProject().getLocation()
							+ "/evosuite-tests", "-evosuiteCP",
					TestGenerationAction.getEvoSuiteJar(), "-projectCP",
					classPath, "-base_dir", baseDir, "-Dshow_progress=false",
					"-Dstopping_condition=MaxTime", "-Dtest_comments=false",
					"-Dpure_inspectors=true", "-Dnew_statistics=false" })); //  "-Dsandbox_mode=IO", -Djava.rmi.server.codebase=file:///Users/gordon/Documents/Workspace/evosuite-eclipse/lib/evosuite-0.1-SNAPSHOT-jar-minimal.jar
			commands.addAll(getAdditionalParameters());
			String cp = "";
			first = true;

			for (int i = 0; i < oldEntries.length; i++) {
				IClasspathEntry curr = oldEntries[i];

				// Check if JUnit is already in the build path
				if (curr.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					IPath path = curr.getPath();
					if (!first)
						cp += File.pathSeparator;
					else
						first = false;
					cp += target.getWorkspace().getRoot().getLocation()
							.toOSString()
							+ path.toOSString();
				}
			}

			String budget = target.getProject().getPersistentProperty(
					EvosuitePropertyPage.TIME_PROP_KEY);
			if (budget == null) {
				commands.add("-Dsearch_budget=120");
			} else {
				commands.add("-Dsearch_budget=" + budget);
			}
			;
			if ("false".equals(target.getProject().getPersistentProperty(
					EvosuitePropertyPage.ASSERTION_PROP_KEY))) {
				commands.add("-Dassertions=false");
			} else {
				commands.add("-Dassertions=true");				
			}
			
			if ("false".equals(target.getProject().getPersistentProperty(
					EvosuitePropertyPage.MINIMIZE_TESTS_PROP_KEY))) {
				commands.add("-Dminimize=false");
			} else {
				commands.add("-Dminimize=true");
			}
			if (!"false".equals(target.getProject().getPersistentProperty(
					EvosuitePropertyPage.MINIMIZE_VALUES_PROP_KEY))) {
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
					EvosuitePropertyPage.DETERMINISTIC_PROP_KEY))) {
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

			if (!"true".equals(target.getProject().getPersistentProperty(
					EvosuitePropertyPage.REPORT_PROP_KEY))) {
				commands.add("-Dhtml=false");
				//commands.add("-Dstatistics_backend=none");
			} else {
				if ("true".equals(target.getProject().getPersistentProperty(
						EvosuitePropertyPage.PLOT_PROP_KEY))) {
					commands.add("-Dplot=true");
				}
			}
			if ("false".equals(target.getProject().getPersistentProperty(
					EvosuitePropertyPage.SANDBOX_PROP_KEY))) {
				commands.add("-Dsandbox=false");
			}
			if ("false".equals(target.getProject().getPersistentProperty(
					EvosuitePropertyPage.SCAFFOLDING_PROP_KEY))) {
				commands.add("-Dscaffolding=false");
				commands.add("-Dno_runtime_dependency=true");
			}
			if ("true".equals(target.getProject().getPersistentProperty(
					EvosuitePropertyPage.CONTRACTS_PROP_KEY))) {
				commands.add("-Dcheck_contracts=true");
			}
			if ("true".equals(target.getProject().getPersistentProperty(
					EvosuitePropertyPage.ERROR_BRANCHES_PROP_KEY))) {
				commands.add("-Derror_branches=true");
			}
			if ("true".equals(target.getProject().getPersistentProperty(
					EvosuitePropertyPage.DSE_PROP_KEY)) || 
				"true".equals(target.getProject().getPersistentProperty(
					EvosuitePropertyPage.LS_PROP_KEY))) {
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
					EvosuitePropertyPage.DSE_PROP_KEY))) {
//				commands.add("-Dlocal_search_rate=1");
//				commands.add("-Dlocal_search_probability=1.0");
//				commands.add("-Dlocal_search_adaptation_rate=1.0");
//				commands.add("-Dlocal_search_selective=false");
//				commands.add("-Dlocal_search_selective_primitives=false");
				commands.add("-Dlocal_search_dse=suite");
				// commands.add("-Dlocal_search_budget_type=time");
				// commands.add("-Dlocal_search_budget=15");
			}
//			if ("true".equals(target.getProject().getPersistentProperty(
//					EvosuitePropertyPage.LS_PROP_KEY))) {
//				commands.add("-Dlocal_search_rate=1");
//				commands.add("-Dlocal_search_probability=1.0");
//				commands.add("-Dlocal_search_adaptation_rate=1.0");
//				commands.add("-Dlocal_search_selective=false");
//				commands.add("-Dlocal_search_selective_primitives=false");
//				commands.add("-Dlocal_search_dse=suite");
//				commands.add("-Dlocal_search_budget_type=time");
//				commands.add("-Dlocal_search_budget=15");
//			}
			String suffix = target.getProject().getPersistentProperty(
					EvosuitePropertyPage.TEST_SUFFIX_PROP_KEY);
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
					EvosuitePropertyPage.CRITERION_PROP_KEY);
			if (criterion != null) {
				commands.add("-criterion");
				commands.add(criterion);
			}
			String[] command = new String[commands.size()];
			commands.toArray(command);
			System.out.println(Arrays.asList(command));

			setupRMI();
			Thread progressMonitor = new Thread() {

				@Override
				public void run() {
					try {
						int percent = 0;
						int last = 0;
						String subTask = "";

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
									if (percent > last
											|| !subTask.equals(currentTask)) {
										subTask = currentTask;
										monitor.worked(percent - last);
										monitor.subTask(subTask);
										last = percent;
									}
								}
							}
							sleep(250); // TODO - should use observer pattern
						}

					} catch (Exception e) {

						System.err
								.println("Exception while reading output of client process "
										+ e);
					}

				}

			};

			progressMonitor.start();
			launchProcess(baseDir, command);

			//			System.out.println("Results: "+results.size());
			// for(TestGenerationResult result : results) {
			//	System.out.println("Covered lines: "+result.getCoveredLines());
			// }
			progressMonitor.interrupt();

			try {
				target.getProject().refreshLocal(IProject.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("" + coverage);
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
		try {
			target.getProject().refreshLocal(IProject.DEPTH_INFINITE, null);
			if ("true".equals(target.getProject().getPersistentProperty(
					EvosuitePropertyPage.REPORT_PROP_KEY))) {
				syncWithUi(target);
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			System.out.println("Dear me");
			e.printStackTrace();
		}

		return Status.OK_STATUS;
	}

	private boolean isFinished(Process process) {
		try {
			process.exitValue();
			return true;
		} catch (IllegalThreadStateException ex) {
			return false;
		}
	}

	private void launchProcess(String baseDir, String[] evoSuiteOptions) throws IOException {
		EvoSuite evosuite = new EvoSuite();
		//		evosuite.parseCommandLine(command);

		Vector<String> javaCmd = new Vector<String>();
		// javaCmd.add("java");
		// javaCmd.add("-jar");
		// javaCmd.add(TestGenerationAction.getEvoSuiteJar());
		Collections.addAll(javaCmd, evoSuiteOptions);

		String[] command = javaCmd.toArray(new String[] {});
		evosuite.parseCommandLine(command);
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

	private void readInputStream(String prefix, InputStream in) throws IOException {
		InputStreamReader is = new InputStreamReader(in);
		BufferedReader br = new BufferedReader(is);
		String read = br.readLine();
		while (read != null) {
			System.out.println(prefix + read);
			read = br.readLine();
		}
	}

	private void syncWithUi(final IResource target) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				GenerationResult result = new GenerationResult(shell,
						SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, target
								.getProject().getLocation(), target);
				result.open();
				//MessageDialog.openInformation(shell, "Your Popup ",
				//                              "Your job has finished.");
			}
		});

	}

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
}
