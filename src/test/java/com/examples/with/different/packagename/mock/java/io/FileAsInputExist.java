package com.examples.with.different.packagename.mock.java.io;

import java.io.File;

public class FileAsInputExist {

	public boolean inputExists(File f) throws NullPointerException{
		 
		if(f.exists()){
			return true;
		} else {
			return false;
		}
	}
}
