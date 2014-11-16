/**
 * 
 */
package org.evosuite.eclipse.properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author Gordon Fraser
 * 
 */
public class EvoSuiteClasspathContainerInitializer extends ClasspathContainerInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#initialize(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject)
	 */
	@Override
	public void initialize(IPath containerPath, IJavaProject project)
	        throws CoreException {
		System.out.println("INITIALIZING CONTAINER");
		EvoSuiteClasspathContainer container = new EvoSuiteClasspathContainer();
		JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project },
		                               new IClasspathContainer[] { container }, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#canUpdateClasspathContainer(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject)
	 */
	@Override
	public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#requestClasspathContainerUpdate(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject, org.eclipse.jdt.core.IClasspathContainer)
	 */
	@Override
	public void requestClasspathContainerUpdate(IPath containerPath,
	        IJavaProject project, IClasspathContainer containerSuggestion)
	        throws CoreException {
		System.out.println("UPDATING CONTAINER "+containerPath+" in project "+project+" to "+containerSuggestion);
		JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project },
		                               new IClasspathContainer[] { containerSuggestion },
		                               null);
	}
}
