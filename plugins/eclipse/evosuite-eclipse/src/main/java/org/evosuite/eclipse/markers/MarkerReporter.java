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
