package com.examples.with.different.packagename.sandbox;

import java.io.File;

public class DeleteInStaticInitializer {

	static {
		String tmpdir = System.getProperty("java.io.tmpdir");
		File f = new File(tmpdir + File.separator
		        + "this_file_should_not_be_deleted_by_evosuite");
		f.delete();
		f.deleteOnExit();
	}
	
	public boolean foo(int x){
		return x > 0;
	}
}
