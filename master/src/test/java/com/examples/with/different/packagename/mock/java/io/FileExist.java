package com.examples.with.different.packagename.mock.java.io;

import java.io.File;

public class FileExist {

	public boolean fooExists(){
		File f = new File("foo");
		if(f.exists()){
			return true;
		} else {
			return false;
		}
	}

}
