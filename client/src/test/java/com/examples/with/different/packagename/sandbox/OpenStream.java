package com.examples.with.different.packagename.sandbox;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class OpenStream {

	public static final String FILE_NAME = "OpenStream_foo.txt";
	
	public boolean open(int x) throws FileNotFoundException{
		new FileOutputStream(FILE_NAME);
		
		if(x>0){
			return true;
		} else {
			return false;
		}
	}

}
