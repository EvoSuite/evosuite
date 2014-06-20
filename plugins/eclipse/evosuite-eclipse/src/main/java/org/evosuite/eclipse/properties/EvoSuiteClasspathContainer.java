/**
 * 
 */
package org.evosuite.eclipse.properties;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

/**
 * @author Gordon Fraser
 * 
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
	@SuppressWarnings("deprecation")
	@Override
	public IPath getPath() {
		URL url = org.eclipse.core.runtime.Platform.getPlugin("org.evosuite.eclipse.core").getBundle().getEntry("evosuite.jar");
		try {
			URL evosuiteLib = org.eclipse.core.runtime.Platform.resolve(url);
			System.out.println("Evosuite jar is at " + evosuiteLib.getPath());
			return new Path(evosuiteLib.getPath());
		} catch (Exception e) {
		}

		return null;
	}

}
