/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.FileUtils;

/**
 * Class with different useful and widely used methods.
 * 
 * @author Andrey Tarasevich.
 * 
 */
public class Utils {

	/**
	 * Deletes directory and its content.
	 * 
	 * @param dirName
	 *            name of the directory to delete
	 */
	public static void deleteDir(String dirName) {
		File dir = new File(dirName);
		if (dir.exists()) {
			String[] children = dir.list();
			for (String s : children) {
				File f = new File(dir, s);
				if (f.isDirectory())
					deleteDir(f.getAbsolutePath());
				f.delete();
			}
		}
		dir.delete();
	}

	/**
	 * Create directory and subdirectories if any.
	 * 
	 * @param dirName
	 *            name of the directory to create
	 * @return true if directory is created successfully, false otherwise
	 */
	public static boolean createDir(String dirName) {
		File srcDirs = new File(dirName);
		return srcDirs.mkdirs();
	}

	/**
	 * Move file to another directory. If file already exist in {@code dest} it
	 * will be rewritten.
	 * 
	 * @param source
	 *            source file
	 * @param dest
	 *            destination file
	 * @return true, if file was moved, false otherwise
	 */
	public static boolean moveFile(File source, File dest) {
		if (source.isDirectory() || dest.isDirectory())
			return false;

		try {
			if (dest.exists())
				dest.delete();
			dest.createNewFile();

			FileUtils.moveFile(source, dest);

		} catch (IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * Hack for adding URL with stubs to the ClassPath. Although the stubs path
	 * is on the ClassPath ClassLoader can not load stub class if directory does
	 * not exist during JVM initialization.
	 * 
	 * @param path
	 *            - path to the folder or jar.
	 * @return true if ClassPath updated successfully.
	 */
	public static boolean addURL(String path) {
		URL url = null;
		try {
			url = (new File(path).toURI().toURL());
		} catch (MalformedURLException e) {
			return false;
		}

		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<URLClassLoader> sysclass = URLClassLoader.class;

		try {
			Class<?>[] parameters = new Class[] { URL.class };
			Method method = sysclass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { url });
			method.setAccessible(false);
		} catch (Throwable t) {
			return false;
		}
		return true;
	}
}
