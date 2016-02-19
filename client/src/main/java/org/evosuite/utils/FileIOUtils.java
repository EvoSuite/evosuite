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
package org.evosuite.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.evosuite.runtime.util.Inputs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

/**
 *  Class used to cover some limitations of Apache IO FileUtils
 *
 */
public class FileIOUtils {

	private static Logger logger = LoggerFactory.getLogger(FileIOUtils.class);


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


	public static List<File> getRecursivelyAllFilesInAllSubfolders(File folder, String suffix) throws IllegalArgumentException{
		Inputs.checkNull(folder, suffix);
		List<File> buffer = new ArrayList<>();
		_recursiveAllFiles(folder, suffix, buffer);
		return buffer;
	}

	/**
	 * Scan the <code>base</code> folder, and return a list of all files with the given name <code>ending</code>
	 *
	 * @param base
	 * @param suffix
	 * @return
	 */
	public static List<File> getRecursivelyAllFilesInAllSubfolders(String base, String suffix) throws IllegalArgumentException{
		Inputs.checkNull(base, suffix);
		return getRecursivelyAllFilesInAllSubfolders(new File(base), suffix);
	}

	private static void _recursiveAllFiles(File folder, String suffix, List<File> buffer){
		if(! folder.exists()){
			throw new IllegalArgumentException("Folder does not exist: "+folder.getAbsolutePath());
		}
		if(! folder.isDirectory()){
			throw new IllegalArgumentException("File is not a folder: "+folder.getAbsolutePath());
		}

		for(File file : folder.listFiles()){
			if(file.isDirectory()){
				_recursiveAllFiles(file, suffix, buffer);
			} else {
				if(file.getName().endsWith(suffix)){
					buffer.add(file);
				}
			}
		}
	}


	/**
	 * Method similar to FileUtils.copyDirectory, but with overwrite
	 *
	 * @param srcDir
	 * @param destDir
	 * @throws IllegalArgumentException
	 */
	public static void copyDirectoryAndOverwriteFilesIfNeeded(File srcDir, File destDir) throws IllegalArgumentException, IOException {
		if(srcDir==null || destDir==null){
			throw new IllegalArgumentException("Null inputs");
		}
		if(!srcDir.exists()){
			throw new IllegalArgumentException("Source folder does not exist: "+srcDir.getAbsolutePath());
		}

		recursiveCopy(srcDir,destDir);
	}

	private static void recursiveCopy(File src, File dest) throws IOException{

		if(src.isDirectory()){

			//the destination might not exist. if so, let's create it
			if(!dest.exists()){
				dest.mkdirs();
			}

			//iterate over the children
			for (String file : src.list()) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				//recursive call
				recursiveCopy(srcFile, destFile);
			}

		}else{

			boolean sameTime = src.lastModified() == dest.lastModified();
			if(sameTime){
				//file was not modified, so no need to copy over
				return;
			}

			try(InputStream in = new FileInputStream(src);
				OutputStream out = new FileOutputStream(dest);) {

				byte[] buffer = new byte[2048];

				int length;
				//copy the file content in bytes
				while ((length = in.read(buffer)) > 0) {
					out.write(buffer, 0, length);
				}
			}

			//as it is a copy, make sure to get same time stamp as src, otherwise it ll be always copied over
			dest.setLastModified(src.lastModified());
		}

	}
}
