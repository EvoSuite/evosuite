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
package org.evosuite.eclipse.properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author Gordon Fraser
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
        JavaCore.setClasspathContainer(containerPath, new IJavaProject[]{project},
                new IClasspathContainer[]{container}, null);
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
        System.out.println("UPDATING CONTAINER " + containerPath + " in project " + project + " to " + containerSuggestion);
        JavaCore.setClasspathContainer(containerPath, new IJavaProject[]{project},
                new IClasspathContainer[]{containerSuggestion},
                null);
    }
}
