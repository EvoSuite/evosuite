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
