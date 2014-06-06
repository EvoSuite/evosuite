/**
 * 
 */
package com.examples.with.different.packagename;

import java.io.File;
import java.io.IOException;

/**
 * @author Gordon Fraser
 * 
 */
public class DeleteFileProcess {

	public void testMe() throws IOException {
		String tmpdir = System.getProperty("java.io.tmpdir");
		String fileName = tmpdir + File.separator
		        + "this_file_should_not_be_deleted_by_evosuite";

		String[] cmdAttribs = new String[] { "/bin/rm", fileName };
		Process proc = Runtime.getRuntime().exec(cmdAttribs);
		try {
			proc.waitFor();
		} catch (InterruptedException e) {
		} finally {
			proc.destroy();
		}
	}

}
