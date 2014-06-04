package com.examples.with.different.packagename.mock.java.io;

import java.io.File;
import java.io.IOException;

public class CreateNewTmpFile {

	public void create(int x){
		
		try {
			File tmp = File.createTempFile("foo", ".tmp");
			tmp.deleteOnExit(); //just in case
		} catch (IOException e) {
			if(x>0){
				System.out.println("target");
			}
		}
	}

}
