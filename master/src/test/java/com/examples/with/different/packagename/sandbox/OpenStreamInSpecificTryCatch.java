package com.examples.with.different.packagename.sandbox;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class OpenStreamInSpecificTryCatch {

	public boolean open(int x) {

		try {
			new FileOutputStream(OpenStream.FILE_NAME);
		} catch (FileNotFoundException e) {
		}

		if(x>0){
			return true;
		} else {
			return false;
		}
	}

}
