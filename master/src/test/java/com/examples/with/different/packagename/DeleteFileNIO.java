/**
 * 
 */
package com.examples.with.different.packagename;

import java.io.IOException;

/**
 * @author Gordon Fraser
 * 
 */
public class DeleteFileNIO {
	public void testMe(String x) throws IOException {
		String tmpdir = System.getProperty("java.io.tmpdir");
		/*
		 * TODO: This only works with Java 7, so for now it is commented out
		java.nio.file.Path p = java.nio.file.FileSystems.getDefault().getPath(tmpdir,
		                                          "this_file_should_not_be_deleted_by_evosuite");
		java.nio.file.Files.delete(p);
		*/
	}
}
