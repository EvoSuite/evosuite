/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

/**
 * Class with different useful and widely used methods.
 *
 * @author Andrey Tarasevich.
 */
public class Utils {

	private static Logger logger = LoggerFactory.getLogger(Utils.class);


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
			long delta = targetTimeMillis - System.currentTimeMillis(); 

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
			Reader reader = new InputStreamReader(
					new FileInputStream(fileName), "utf-8");
			BufferedReader in = new BufferedReader(reader);
			try {
				String str;
				while ((str = in.readLine()) != null) {
					content.add(str);
				}
			} finally {
				in.close();
			}
		} catch (Exception e) {
			logger.error("Error while reading file "+fileName+" , "+
					e.getMessage(), e);
		}
		return content;
	}
	
	public static String readFileToString(String fileName) {
		StringBuilder content = new StringBuilder();
		try {
			Reader reader = new InputStreamReader(
					new FileInputStream(fileName), "utf-8");
			BufferedReader in = new BufferedReader(reader);
			try {
				String str;
				while ((str = in.readLine()) != null) {
					content.append(str);
				}
			} finally {
				in.close();
			}
		} catch (Exception e) {
			logger.error("Error while reading file "+fileName+" , "+
					e.getMessage(), e);
		}
		return content.toString();
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
			Reader reader = new InputStreamReader(
					new FileInputStream(file), "utf-8");
			BufferedReader in = new BufferedReader(reader);
			try {
				String str;
				while ((str = in.readLine()) != null) {
					content.add(str);
				}
			} finally {
				in.close();
			}
		} catch (Exception e) {
			logger.error("Error while reading file "+file.getName()+" , "+
					e.getMessage(), e);
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
			logger.error("Error while writing file "+fileName+" , "+
					e.getMessage(), e);			
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
		} catch (Exception e) {
			logger.error("Error while reading file "+file.getName()+" , "+
					e.getMessage(), e);
		}
	}

	public static void writeFile(InputStream in, File dest) {
		try {
			dest.deleteOnExit();
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
		} catch (Exception e) {
			logger.error("Error while writing file "+dest.getName()+" , "+
					e.getMessage(), e);
		}
	}

	/**
	 * Write string to file
	 *
	 * @param fileName
	 *            - name of the file to write to
	 * @param data a {@link java.lang.Object} object.
	 */
	public static void writeXML(Object data, String fileName) {
		try {
			XStream xstream = new XStream();
			FileUtils.writeStringToFile(new File(fileName), xstream.toXML(data));
		} catch (Exception e) {
			logger.error("Error while writing file "+fileName+" , "+
					e.getMessage(), e);
		}
	}

	/**
	 * Write string to file
	 *
	 * @param fileName
	 *            - name of the file to write to
	 * @param <T> a T object.
	 * @return a T object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T readXML(String fileName) {
		XStream xstream = new XStream();
		try {
			Reader reader = new InputStreamReader(
					new FileInputStream(fileName), "utf-8");
			BufferedReader in = new BufferedReader(reader);
			return (T) xstream.fromXML(in);

		} catch (Exception e) {
			logger.error("Error while reading file "+fileName+" , "+
					e.getMessage(), e);
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
	
	public static String createFolderForTests(String base, String fullClassName){
		String packageName = "";
		if (fullClassName.contains(".")){
			packageName = fullClassName.substring(0, fullClassName.lastIndexOf("."));
		} else {
			packageName = "";
		}
		packageName = packageName.replaceAll(".", File.separator);
		
		String testFolderName = base+File.separator+packageName;
		File testFolder = new File(testFolderName);
		if(testFolder.exists()){
			return testFolderName;
		} else {
			boolean created = testFolder.mkdirs();
			if(!created){
				logger.error("Failed to create: "+testFolderName);
				return null;
			} else {
				return testFolderName;
			}
		}
	}
	
	/**
	 * Scan the <code>base</code> folder, and return a list of all files with the given name <code>ending</code>
	 * 
	 * @param base
	 * @param ending
	 * @return
	 */
	public static List<File> getAllFilesInSubFolder(String base, String ending) throws IllegalArgumentException{
		
		File dir = new File(base);
		if(!dir.exists() || !dir.isDirectory()){
			throw new IllegalArgumentException("Invalid folder "+base);
		}
		
		List<File> files = new LinkedList<File>();
		recursiveFileSearch(files,dir,ending);
		
		return files;
	}
	
	private static void recursiveFileSearch(List<File> files, File dir, String ending){
		
		for(File file : dir.listFiles()){
			if(file.isDirectory()){
				recursiveFileSearch(files,file,ending);
			} else {
				if(file.getName().endsWith(ending)){
					files.add(file);
				}
			}
		}
		
	}
}
