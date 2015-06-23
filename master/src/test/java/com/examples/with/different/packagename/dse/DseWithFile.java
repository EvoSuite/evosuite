package com.examples.with.different.packagename.dse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DseWithFile {

	public String test(File f) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();

		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
		}
		reader.close();
		String str = stringBuilder.toString();
		if (str.equals("<<FILE CONTENT>>"))
			return str;
		else
			return null;
	}
}
