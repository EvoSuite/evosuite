/**
 * 
 */
package com.examples.with.different.packagename;

import java.io.File;

/**
 * @author Gordon Fraser
 * 
 */
public class DeleteFileThread {

	public void testMe(String x) {
		Thread t = new Thread() {
			@Override
			public void run() {
				while (!isInterrupted()) {
					String tmpdir = System.getProperty("java.io.tmpdir");
					File f = new File(tmpdir + File.separator
					        + "this_file_should_not_be_deleted_by_evosuite");
					f.delete();
					f.deleteOnExit();
					try {
						sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
		};
		t.start();
	}
}
