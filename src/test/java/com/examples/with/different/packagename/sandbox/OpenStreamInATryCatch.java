package com.examples.with.different.packagename.sandbox;

import java.io.FileOutputStream;

public class OpenStreamInATryCatch {

	public boolean open(int x) {
		
		try {
			new FileOutputStream(OpenStream.FILE_NAME);
			System.out.println("This should never be executed without a VFS");
		} catch (Exception e) {
			System.out.println("Denied permission: "+e.getMessage());
		}
		
		if(x>0){
			return true;
		} else {
			return false;
		}
	}

}
