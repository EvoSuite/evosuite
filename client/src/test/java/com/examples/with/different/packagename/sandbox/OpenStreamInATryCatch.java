package com.examples.with.different.packagename.sandbox;

import java.io.FileOutputStream;
import java.io.IOException;

public class OpenStreamInATryCatch {

	public boolean open(int x) {
		
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(OpenStream.FILE_NAME);
			System.out.println("This should never be executed without a VFS");
		} catch (Exception e) {
			System.out.println("Denied permission: "+e.getMessage());
		}
		
		if(stream!=null){
			try {
				stream.close();
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}
		
		if(x>0){
			return true;
		} else {
			return false;
		}
	}

}
