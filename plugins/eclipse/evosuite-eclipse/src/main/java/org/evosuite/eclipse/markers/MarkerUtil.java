/**
 * 
 */
package org.evosuite.eclipse.markers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Gordon Fraser
 * 
 */
public class MarkerUtil {

	private MarkerUtil() {
		throw new AssertionError("Shouldn't be initialized");
	}

	/**
	 * Remove all FindBugs problem markers for given resource.
	 */
	public static void removeMarkers(IResource res) throws CoreException {
		// remove any markers added by our builder
		// This triggers resource update on IResourceChangeListener's
		// (BugTreeView)
		System.out.println("Removing JSR 308 markers in " + res.getLocation());

		res.deleteMarkers(MarkerReporter.NAME, true, IResource.DEPTH_INFINITE);
	}

	public static void addMarker(String message, IProject project, IResource resource,
	        int startLine) {
		System.out.println("Creating marker for " + resource.getLocation() + ": line "
		        + startLine + " " + message);

		try {
			project.getWorkspace().run(new MarkerReporter(resource, startLine, message),
			                           null, 0, null);
		} catch (CoreException e) {
			System.err.println(e + ": Core exception on add marker");
		}
	}
}
