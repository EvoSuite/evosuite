/**
 * 
 */
package com.examples.with.different.packagename;

import java.io.File;

/**
 * @author Gordon Fraser
 * 
 */
public class DeleteFileExample {

	public void testMe(String x) {
		String tmpdir = System.getProperty("java.io.tmpdir");
		File f = new File(tmpdir + File.separator
		        + "this_file_should_not_be_deleted_by_evosuite");
		f.delete();
		f.deleteOnExit();
	}
}
