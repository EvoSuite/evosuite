/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
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
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.thoughtworks.xstream.XStream;

/**
 * Class with different useful and widely used methods.
 * 
 * @author Andrey Tarasevich.
 * 
 */
public class Utils {

	/**
	 * Sleeps at least until the specified time point has passed.
	 * 
	 * (Thread.sleep() does something similar, but can stop sleeping before
	 * the specified time point has passed if the thread is interrupted.)
	 *  
	 * @param targetTimeMillis Target time in milliseconds
	 */
	public static void sleepUntil(long targetTimeMillis) {
		while (true) {
			long delta = System.currentTimeMillis() - targetTimeMillis; 
			
			if (delta <= 0) {
				break;
			}
			
			try {
				Thread.sleep(delta);
			} catch (InterruptedException e) { /* OK */ }
		}
	}

	/**
	 * Sleeps at least until the specified amount of time has passed, uninterruptibly.
	 *  
	 * @param millis Amount of milliseconds to sleep
	 */
	public static void sleepFor(long millis) {
		sleepUntil(System.currentTimeMillis() + millis);
	}
	
	/**
	 * Deletes directory and its content.
	 * 
	 * @param dirName
	 *            - name of the directory to delete
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
	 *            - name of the directory to create
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
	 *            - source file
	 * @param dest
	 *            - destination file
	 * @return true, if file was moved, false otherwise
	 */
	public static boolean moveFile(File source, File dest) {
		if (source.isDirectory() || dest.isDirectory())
			return false;

		try {
			if (dest.exists())
				dest.delete();

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

	/**
	 * Parse input string and searches for the ASM-like descriptions of the
	 * classes.
	 * 
	 * @param input
	 *            string, where class description should be
	 * @return Set of class's names in ASM manner or empty set if none were
	 *         found in input string
	 */
	public static Set<String> classesDescFromString(String input) {
		Set<String> classesDesc = new HashSet<String>();

		// If input actually equals "null" then NullPointerException is thrown. 
		// Don't ask me. I don't know why.
		try {
			if (input.equals("null"))
				return new HashSet<String>();
			;
		} catch (NullPointerException e) {
			return classesDesc;
		}

		// assume first package prefix is written with lower case letters. 
		Pattern p = Pattern.compile("([a-z])+(/\\w+)+");
		Matcher m = p.matcher(input);
		while (m.find()) {
			String str = m.group();
			classesDesc.add(str);
		}

		return classesDesc;
	}

	/**
	 * Read file line by line into list.
	 * 
	 * @param fileName
	 *            - name of the file to read from
	 * @return content of the file in a list
	 */
	public static List<String> readFile(String fileName) {
		List<String> content = new LinkedList<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String str;
			while ((str = in.readLine()) != null) {
				content.add(str);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * Read file line by line into list.
	 * 
	 * @param file
	 *            - file to read from
	 * @return content of the file in a list
	 */
	public static List<String> readFile(File file) {
		List<String> content = new LinkedList<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String str;
			while ((str = in.readLine()) != null) {
				content.add(str);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	/**
	 * Write string to file
	 * 
	 * @param fileName
	 *            - name of the file to write to
	 * @param content
	 *            - text to write into the file
	 */
	public static void writeFile(String content, String fileName) {
		try {
			FileUtils.writeStringToFile(new File(fileName), content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write string to file
	 * 
	 * @param file
	 *            - file to write to
	 * @param content
	 *            - text to write into the file
	 */
	public static void writeFile(String content, File file) {
		try {
			FileUtils.writeStringToFile(file, content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeFile(InputStream in, File dest) {
		try {
			dest.deleteOnExit();
			System.out.println("Creating file: " + dest.getPath());
			if (!dest.exists()) {
				OutputStream out = new FileOutputStream(dest);
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				out.close();
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write string to file
	 * 
	 * @param fileName
	 *            - name of the file to write to
	 * @param content
	 *            - text to write into the file
	 */
	public static void writeXML(Object data, String fileName) {
		try {
			XStream xstream = new XStream();
			FileUtils.writeStringToFile(new File(fileName), xstream.toXML(data));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write string to file
	 * 
	 * @param fileName
	 *            - name of the file to write to
	 * @param content
	 *            - text to write into the file
	 */
	@SuppressWarnings("unchecked")
	public static <T> T readXML(String fileName) {
		XStream xstream = new XStream();
		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			return (T) xstream.fromXML(in);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get package name from the class. Sometimes this maybe tricky, since
	 * clazz.getPackage() could return null
	 * 
	 * @param clazz
	 *            - class which package should be determined
	 * @return package name of the class
	 */
	public static String getPackageName(Class<?> clazz) {
		String packageName = "";
		if (clazz.getPackage() != null) {
			packageName = clazz.getPackage().getName();
		} else {
			String name = clazz.getName();
			if (name.contains("."))
				packageName = name.substring(0, name.lastIndexOf("."));
			else
				packageName = "";
		}
		return packageName;
	}
}