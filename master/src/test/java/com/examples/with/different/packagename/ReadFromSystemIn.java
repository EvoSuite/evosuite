package com.examples.with.different.packagename;

import java.util.Scanner;

public class ReadFromSystemIn {

	public boolean readHelloWorld(){
		
		Scanner scanner = new Scanner(System.in);
		String line = scanner.nextLine();
		scanner.close();
		
		if(line.equals("Hello World")){
			return true;
		} else {
			return false;
		}
	}

}
