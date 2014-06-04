package com.examples.with.different.packagename.mock.java.io;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

public class ReadHelloWorldFromFileWithNameAsInput {

	public boolean check(String s) throws Exception{
		File file = new File(s);

		if(file.exists()){

			Scanner fromFile = new Scanner(new FileInputStream(file));
			String fileContent = fromFile.nextLine();
			if(fileContent.equals("Hello World!")){
				System.out.println("Target reached");
			}			
			fromFile.close();
			
			return true;
		} else {
			return false;
		}
	}
}
