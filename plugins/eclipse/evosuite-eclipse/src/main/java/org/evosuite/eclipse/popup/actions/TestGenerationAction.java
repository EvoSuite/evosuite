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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.junit.JUnitCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.evosuite.Properties;
import org.evosuite.eclipse.Activator;
import org.osgi.framework.Bundle;

/**
 * @author Gordon Fraser
 * 
 */
public abstract class TestGenerationAction implements IObjectActionDelegate {

	private static String EVOSUITE_CP = null;

	protected Shell shell;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public abstract void run(IAction action);

	/**
	 * Main action: check if evosuite-tests exists, and add test generation job
	 * 
	 * @param target
	 */
	public void generateTests(IResource target) {
		String disabled = System.getProperty("disable.evosuite");
		if ( disabled != null && disabled.equals("1")) {			
			MessageDialog.openInformation(shell, "Sorry!", "The EvoSuite Plugin is disabled :(");
			return;
		}
		System.out.println("EvoSuite Plugin is enabled");
		System.out.println("[TestGenerationAction] Generating tests for " + target.toString());
		IFolder folder = target.getProject().getFolder("evosuite-tests");
		if (!folder.exists()) {
			// Create evosuite-tests directory and add as source folder
			try {
				folder.create(false, true, null);
				IJavaProject jProject = JavaCore.create(target.getProject());

				IPackageFragmentRoot root = jProject.getPackageFragmentRoot(folder);
				IClasspathEntry[] oldEntries = jProject.getRawClasspath();
				IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
				System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
				newEntries[oldEntries.length] = JavaCore.newSourceEntry(root.getPath());
				jProject.setRawClasspath(newEntries, null);

			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		addTestJob(target);
	}

	/**
	 * Add a new test generation job to the job queue
	 * 
	 * @param target
	 */
	protected void addTestJob(final IResource target) {
		IJavaElement element = JavaCore.create(target);
		if (element == null) {
			return;
		}
		IJavaElement packageElement = element.getParent();

		String packageName = packageElement.getElementName();

		final String targetClass = (!packageName.isEmpty() ? packageName + "." : "")
		        + target.getName().replace(".java", "").replace(File.separator, ".");
		System.out.println("* Scheduling new automated job for " + targetClass);
		final String targetClassWithoutPackage = target.getName().replace(".java", "");

		String tmp = targetClass.replace('.', File.separatorChar);

		String targetPath = target.getProject().getLocation() + "/evosuite-tests/" + tmp +
		        //+ tmp.substring(0, tmp.lastIndexOf(File.separator) + 1) 
		        //+ tmp.substring(tmp.lastIndexOf(File.separator) + 1) + 
				Properties.JUNIT_SUFFIX + ".java";
		System.out.println("Checking for " + targetPath);
		File testSuite = new File(targetPath);
		if (testSuite.exists()) {

			MessageDialog dialog = new MessageDialog(
			        shell,
			        "Existing test suite found",
			        null, // image
			        "A test suite for class \""
			                + targetClass
			                + "\" already exists. EvoSuite will overwrite this test suite. Do you really want to proceed?",
			        MessageDialog.QUESTION_WITH_CANCEL, new String[] { "Overwrite",
			                "Rename Original", "Cancel" }, 0);

			int returnCode = dialog.open();
			if (returnCode == 1) {
				// Rename
				renameTarget(target, packageName, targetClassWithoutPackage + Properties.JUNIT_SUFFIX + ".java");
			} else if (returnCode > 1) {
				// Cancel
				return;
			}
		}

		Job job = new TestGenerationJob(shell, target, targetClass);
		job.setPriority(Job.SHORT);
		IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
		ISchedulingRule rule = ruleFactory.createRule(target.getProject());

		//IFolder folder = proj.getFolder(ResourceUtil.EVOSUITE_FILES);
		job.setRule(rule);
		job.setUser(true);
		job.schedule(); // start as soon as possible
	}

	/**
	 * If we move away an old EvoSuite test suite for a class then we rename it
	 * to <name><num>.java.
	 * 
	 * @param name
	 * @param cus
	 * @return
	 */
	public String getNewName(String name, ICompilationUnit[] cus) {
		int num = 0;
		String className = name.replace(".java", "");
		String newName = className + num + ".java";
		boolean found = true;
		while (found) {
			found = false;
			for (ICompilationUnit cu : cus) {
				if (cu.getElementName().equals(newName)) {
					num++;
					newName = className + num + ".java";
					found = true;
					break;
				}
			}
		}
		return newName;
	}

	/**
	 * If there already exists a test suite for the chosen target class, then
	 * renaming the old test suite is one of the options the user can choose
	 * 
	 * @param target
	 * @param packageName
	 * @param targetPath
	 */
	public void renameTarget(final IResource target, String packageName, String targetPath) {
		System.out.println("Renaming " + targetPath + " in package " + packageName);
		IPackageFragment[] packages;
		try {
			packages = JavaCore.create(target.getProject()).getPackageFragments();
			System.out.println("Packages found: "+packages.length);
			for (IPackageFragment f : packages) {
				if ((f.getKind() == IPackageFragmentRoot.K_SOURCE) 
						&& (f.getElementName().equals(packageName))) {
					
					for (ICompilationUnit cu : f.getCompilationUnits()) {
						
						String cuName = cu.getElementName();
						System.out.println("targetPath = " + targetPath);
						System.out.println("cuName = " + cuName);
						if (cuName.equals(targetPath)) {
							RefactoringContribution contribution = RefactoringCore.getRefactoringContribution(IJavaRefactorings.RENAME_COMPILATION_UNIT);
							RenameJavaElementDescriptor descriptor = (RenameJavaElementDescriptor) contribution.createDescriptor();
							descriptor.setProject(cu.getResource().getProject().getName());
							String newName = getNewName(cuName, f.getCompilationUnits());
							System.out.println("New name for Test Suite: " + newName);
							descriptor.setNewName(newName); // new name for a Class
							descriptor.setJavaElement(cu);

							RefactoringStatus status = new RefactoringStatus();
							try {
								Refactoring refactoring = descriptor.createRefactoring(status);

								IProgressMonitor monitor = new NullProgressMonitor();
								refactoring.checkInitialConditions(monitor);
								refactoring.checkFinalConditions(monitor);
								Change change = refactoring.createChange(monitor);
								change.perform(monitor);

							} catch (CoreException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		} catch (JavaModelException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * If we generate JUnit tests, we need to make sure that JUnit is on the
	 * classpath of the project, otherwise we will see compile errors
	 * 
	 * @param project
	 */
	public void fixJUnitClassPath(IJavaProject project) {
		IPath junitPath = JUnitCore.JUNIT4_CONTAINER_PATH;

		boolean hasJUnit = false;
		boolean hasEvoSuite = false;
		boolean hasOldEvoSuite = false;

		try {
			Path containerPath = new Path("org.evosuite.eclipse.classpathContainerInitializer");
			IClasspathContainer container = JavaCore.getClasspathContainer(containerPath, project);
			System.out.println("EvoSuite JAR at: " + container.getPath().toOSString());

			IClasspathEntry[] oldEntries = project.getRawClasspath();
			ArrayList<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>(oldEntries.length + 1);

			IClasspathEntry cpentry = JavaCore.newContainerEntry(junitPath);

			for (int i = 0; i < oldEntries.length; i++) {
				IClasspathEntry curr = oldEntries[i];

				// Check if JUnit is already in the build path
				if (curr.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
					IPath path = curr.getPath();
					if (path.equals(cpentry.getPath())) {
						hasJUnit = true;
					}
					if (path.equals(container.getPath())) {
						hasEvoSuite = true;
					}
				} else if (curr.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					// Check for older EvoSuite entries
					IPath path = curr.getPath();
					if (path.toFile().getName().equals(Activator.EVOSUITE_JAR)) {
						if (path.equals(container.getPath())) {
							System.out.println("Is current evosuite!");
							hasEvoSuite = true;
						} else {
							System.out.println("Is NOT current evosuite!");
							hasOldEvoSuite = true;
							continue;
						}
					}
					if (path.equals(cpentry.getPath())) {
						hasJUnit = true;
					}
					if (path.equals(container.getPath())) {
						hasEvoSuite = true;
					}
				}

				if (curr != null) {
					newEntries.add(curr);
				}
			}

			if (hasJUnit && hasEvoSuite && !hasOldEvoSuite) {
				return;
			}

			// add the entry
			if (!hasJUnit) {
				newEntries.add(cpentry);
			}

			if (!hasEvoSuite && container != null) {
				for (IClasspathEntry entry : container.getClasspathEntries()) {
					newEntries.add(entry);
				}
			}
			
			System.out.println("New classpath: " + newEntries);

			// newEntries.add(JavaCore.newContainerEntry(EvoSuiteClasspathContainer.ID));

			// Convert newEntries to an array
			IClasspathEntry[] newCPEntries = newEntries.toArray(new IClasspathEntry[newEntries.size()]);
			project.setRawClasspath(newCPEntries, null);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

	}

	/**
	 * This is tricky because the evosuite jar in the deployed plugin is
	 * contained in the plugin jar, but for the client process we need it
	 * explicitly on the classpath. Hence, if we see that the evosuite jar is in
	 * another jar, we extract it to a temporary location
	 * 
	 * @return Location of the jar file
	 */
	public static String getEvoSuiteJar() {
		if (EVOSUITE_CP != null)
			return EVOSUITE_CP;

		Bundle bundle = Platform.getBundle(Activator.EVOSUITE_CORE_BUNDLE);
		URL url = bundle.getEntry(Activator.EVOSUITE_JAR);
				
		try {
			URL evosuiteLib = FileLocator.resolve(url);
			System.out.println("Evosuite JAR is at " + evosuiteLib.getPath());
			if (evosuiteLib.getPath().startsWith("file")) {
				System.out.println("Need to extract jar");
				EVOSUITE_CP = setupJar(evosuiteLib);
			} else {
				System.out.println("Don't need to extract jar");
				EVOSUITE_CP = evosuiteLib.getFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return EVOSUITE_CP;
	}

	/**
	 * Performs the actual extraction of the jar file to a temporary directory
	 * 
	 * @param evosuiteLib
	 * @return
	 * @throws IOException
	 */
	protected static String setupJar(URL evosuiteLib) throws IOException {
		String tmpDir = System.getProperty("java.io.tmpdir");
		String jarName = tmpDir + File.separator + Activator.EVOSUITE_JAR;
		System.out.println("Copying jar file to " + jarName);
		File tempJar = new File(jarName);
		writeFile(evosuiteLib.openStream(), new File(jarName));
		return tempJar.getPath();
	}

	/**
	 * Helper function to copy files
	 * 
	 * @param in
	 * @param dest
	 */
	private static void writeFile(InputStream in, File dest) {
		try {
			dest.deleteOnExit();
			System.out.println("Creating file: " + dest.getPath());
			if (!dest.exists()) {
				OutputStream out = new FileOutputStream(dest);
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				out.close();
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}
}
