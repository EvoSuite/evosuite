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

package org.evosuite.eclipse.markers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Gordon Fraser
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
