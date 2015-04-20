/**
 * 
 */
package com.examples.with.different.packagename;

import java.io.File;

import org.apache.commons.io.FileUtils;

/**
 * @author Gordon Fraser
 * 
 */
public class DeleteFileCommonsIO {
	public void testMe(String x) {
		String tmpdir = System.getProperty("java.io.tmpdir");
		File f = new File(tmpdir + File.separator
		        + "this_file_should_not_be_deleted_by_evosuite");
		FileUtils.deleteQuietly(f);
	}
}
