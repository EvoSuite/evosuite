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
/**
 * 
 */
package org.evosuite.eclipse.markers;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Gordon Fraser
 * 
 */
public class MarkerReporter implements IWorkspaceRunnable {

	public static final String NAME = "evosuite-eclipse.marker";

	private final IResource resource;
	private final int startLine;
	private final String message;

	public MarkerReporter(IResource resource, int startLine, String message) {
		this.startLine = startLine;
		this.resource = resource;
		this.message = message;
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {

		System.out.println("Creating marker for " + resource.getLocation());

		IMarker marker = resource.createMarker(NAME);
		System.out.println("Setting attibutes for marker in " + resource.getLocation());

		marker.setAttribute(IMarker.LINE_NUMBER, startLine);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
	}

}
