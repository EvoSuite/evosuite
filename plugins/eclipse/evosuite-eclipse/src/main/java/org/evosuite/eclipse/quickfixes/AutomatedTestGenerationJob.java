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
package org.evosuite.eclipse.quickfixes;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.result.TestGenerationResult;
import org.evosuite.rmi.MasterServices;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.rmi.service.ClientStateInformation;
import org.evosuite.rmi.service.MasterNodeLocal;
import org.evosuite.eclipse.Activator;
import org.evosuite.eclipse.popup.actions.DumbSecurityManager;
import org.evosuite.eclipse.popup.actions.GenerationResult;
import org.evosuite.eclipse.popup.actions.TestGenerationAction;
/**
 * @author Thomas White, extended from Gordon Fraiser's
 *         org.evosuite.eclipse.popup.actions.TestGenerationJob.java
 * 
 */
public class AutomatedTestGenerationJob extends Job {

	private String targetClass = "";

	private IResource target;

	private final Shell shell;

	private boolean running = false;
	private final EvoSuite evosuite = new EvoSuite();
	private String lastTest = "";
	private static final double SEED_CHANCE = 0.2;
	private boolean stopped = false;

	public AutomatedTestGenerationJob(Shell shell, final IResource target, String targetClass) {
		super("EvoSuite Test Generation: " + targetClass);
		this.targetClass = targetClass;
		this.target = target;
		this.shell = shell;
		// setPriority(Job.DECORATE);
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
	private void setupRMI() {
		// RMI only runs if there is a security manager
		if (System.getSecurityManager() == null) {
			// As there is no security manager, we can just put a dumb security
			// manager that allows everything here, just to make RMI happy
			System.setSecurityManager(new DumbSecurityManager());
		}
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		setThread(new Thread());
		running = true;
		try {
			MarkerWriter.clearMarkers(target);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String oldTgr = getOldTestGenerationResult();
		lastTest = oldTgr;
		ArrayList<TestGenerationResult> results = runEvoSuite(monitor);
		try {
			MarkerWriter.clearMarkers(target);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (TestGenerationResult result : results) {
			// any lines covered?
			if (result.getCoveredLines().size() > 0) {
				if (!stopped) {
					MarkerWriter.write(target, result);
				}
				saveTestGenerationResult(result);
			}
		}
		try {
			target.getProject().refreshLocal(IProject.DEPTH_INFINITE, null);
			//syncWithUi(target);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		Activator.CURRENT_WRITING_FILE = null;
		running = false;
		monitor.done();
		done(ASYNC_FINISH);
		Activator.FILE_QUEUE.update();
		return Status.OK_STATUS;
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
						+ filePackage.replace(".", "/"));
			}
			if (!folder.exists()) {
				try {
					createIFolder(folder);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			IFile file = folder.getFile(target.getName() + ".gadata");
			if (file.exists()) {
				try {
					file.delete(true, null);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private String getOldTestGenerationResult() {
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
						+ filePackage.replace(".", "/"));
			}
			IFile file = folder.getFile(target.getName() + ".gadata");
			return file.getLocation().toOSString();

		}
		return "";
	}

	private ArrayList<TestGenerationResult> runEvoSuite(
			final IProgressMonitor monitor) {
		ArrayList<TestGenerationResult> tgrs = new ArrayList<TestGenerationResult>();

		monitor.beginTask("EvoSuite test suite generation", 100);

		try {
			IJavaProject jProject = JavaCore.create(target.getProject());
			IClasspathEntry[] oldEntries = jProject.getRawClasspath();
			String classPath = target.getWorkspace().getRoot()
					.findMember(jProject.getOutputLocation()).getLocation()
					.toOSString();

			for (int i = 0; i < oldEntries.length; i++) {
				IClasspathEntry curr = oldEntries[i];
				System.out.println("Current entry: " + curr.getPath());

				if (curr.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					IPath path = curr.getPath();
					classPath += File.pathSeparatorChar;

					if (path.toFile().exists()) {
						classPath += path.toOSString();
					} else {
						classPath += target.getWorkspace().getRoot()
								.getLocation().toOSString()
								+ path.toOSString();
					}
				} else if (curr.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
					classPath += File.pathSeparatorChar;

					classPath += JavaCore.getResolvedVariablePath(curr
							.getPath());
				}
			}
			String baseDir = target.getProject().getLocation().toOSString();
			Properties.TARGET_CLASS = targetClass;
			List<String> commands = new ArrayList<String>();
			int time = Activator.getDefault().getPreferenceStore().getInt("runtime");
			if (time <= 0){
				time = 30;
			}
			commands.addAll(Arrays.asList(new String[] {
					"-generateSuite",
					"-class",
					targetClass,
					"-Dtest_dir=" + target.getProject().getLocation() + "/evosuite-tests", 
					"-evosuiteCP", TestGenerationAction.getEvoSuiteJar(), 
					"-projectCP", classPath, 
					"-base_dir", baseDir, 
					"-Dshow_progress=false", 
					"-Dstopping_condition=MaxTime", 
					"-Dtest_comments=true", 
					"-Dseed_file=" + lastTest,
					"-Dtest_factory=SEED_BEST_AND_RANDOM_INDIVIDUAL",
					"-Dseed_probability=" + SEED_CHANCE, 
					"-Dsearch_budget=" + time, 
					"-Dserialize_ga=true",
					"-Dconsider_main_methods=true", 
					"-Dnew_statistics=true",
					"-Dassertion_timeout=" + time, 
					"-mem", "2500" }));

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

								percent = currentState.getOverallProgress();
								if (percent >= 100
										&& currentState.getState() == ClientState.NOT_STARTED)
									continue;

								String currentTask = currentState
										.getState().getDescription()
										+ " ["
										+ currentState.getState()
										.getOverallProgress()
										+ "%]";
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
			Object object = evosuite.parseCommandLine(command);
			if (object instanceof List) {
				for (Object o : (List<List<TestGenerationResult>>) object) {
					if (o instanceof List) {
						for (TestGenerationResult r : (List<TestGenerationResult>) o) {
							tgrs.add((TestGenerationResult) r);
						}
					} else
						tgrs.add((TestGenerationResult) o);
				}
			} else if (object instanceof TestGenerationResult) {
				tgrs.add((TestGenerationResult) object);
			}
			progressMonitor.interrupt();

			try {
				target.getProject().refreshLocal(IProject.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
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

	@SuppressWarnings("unused")
	private void syncWithUi(final IResource target) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				GenerationResult result = new GenerationResult(shell,
						SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL, target
								.getProject().getLocation(), target);
				result.open();
				// MessageDialog.openInformation(shell, "Your Popup ",
				// "Your job has finished.");
			}
		});
	}

	public boolean isRunning() {
		return running;
	}
}
