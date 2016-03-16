package com.examples.with.different.packagename.papers.vnet;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;


public class Example_URL {

	public boolean checkURL() {
		try {
			URL url = new URL("http://www.evosuite.org/index.html");			
			URLConnection connection = url.openConnection();
			Scanner in = new Scanner(connection.getInputStream());
			String line = in.nextLine(); 
			in.close();
			
			if(line.contains("<html>")){
				return true;
			} else {
				return false;
			}
			
		} catch (IOException e) {
			return false;
		}
	}
}
