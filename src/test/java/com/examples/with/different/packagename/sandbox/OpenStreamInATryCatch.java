package com.examples.with.different.packagename.sandbox;

import java.io.FileOutputStream;

public class OpenStreamInATryCatch {

	public boolean open(int x) {
		
		try {
			new FileOutputStream(OpenStream.FILE_NAME);
		} catch (Exception e) {
		}
		
		if(x>0){
			return true;
		} else {
			return false;
		}
	}

}
