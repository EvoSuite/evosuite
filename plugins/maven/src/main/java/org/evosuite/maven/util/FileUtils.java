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
package org.evosuite.maven.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;

public class FileUtils {

	/**
	 * Scans a set of directories
	 *
	 * @param roots Directories to scan
	 * @param includes
	 * @param excludes
	 * @return
	 */
	public static List<File> scan(List<String> roots, String[] includes, String[] excludes) {
		List<File> files = new ArrayList<File>();
		for (String root : roots) {
			files.addAll(FileUtils.scan(new File(root), includes, excludes));
		}
		return files;
	}

	/**
	 * Scans a single directory
	 *
	 * @param root Directory to scan
	 * @param includes
	 * @param excludes
	 * @return
	 */
	public static List<File> scan(File root, String[] includes, String[] excludes) {
		List<File> files = new ArrayList<File>();
		if (!root.exists()) {
			return files;
		}

		final DirectoryScanner directoryScanner = new DirectoryScanner();
		directoryScanner.setIncludes(includes);
		directoryScanner.setExcludes(excludes);
		directoryScanner.setBasedir(root);
		directoryScanner.scan();

		for (final String fileName : directoryScanner.getIncludedFiles()) {
			files.add(new File(root, fileName));
		}

		return files;
	}
}
