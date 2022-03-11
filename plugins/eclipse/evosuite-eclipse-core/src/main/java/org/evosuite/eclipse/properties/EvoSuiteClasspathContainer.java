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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.evosuite.eclipse.Activator;
import org.osgi.framework.Bundle;

/**
 * @author Gordon Fraser
 */
public class EvoSuiteClasspathContainer implements IClasspathContainer {

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IClasspathContainer#getClasspathEntries()
     */
    @Override
    public IClasspathEntry[] getClasspathEntries() {
        List<IClasspathEntry> entryList = new ArrayList<IClasspathEntry>();

        entryList.add(JavaCore.newLibraryEntry(getPath(), null, null));

        // convert the list to an array and return it
        IClasspathEntry[] entryArray = new IClasspathEntry[entryList.size()];
        return entryList.toArray(entryArray);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IClasspathContainer#getDescription()
     */
    @Override
    public String getDescription() {
        return "EvoSuite test generation library";
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IClasspathContainer#getKind()
     */
    @Override
    public int getKind() {
        return IClasspathContainer.K_APPLICATION;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IClasspathContainer#getPath()
     */
    @Override
    public IPath getPath() {
        Bundle bundle = Platform.getBundle(Activator.EVOSUITE_CORE_BUNDLE);
        URL url = bundle.getEntry(Activator.EVOSUITE_JAR);
        try {
            URL evosuiteLib = FileLocator.resolve(url);
            System.out.println("Evosuite jar is at " + evosuiteLib.getPath());
            return new Path(evosuiteLib.getPath());
        } catch (Exception e) {
            System.err.println("Error accessing Evosuite jar at " + url);
        }
        System.err.println("Did not find Evosuite jar!");
        return null;
    }

}
