package org.evosuite.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public enum PureMethodsList {

	instance;
	
	private Set<String> pureMethods;

	private PureMethodsList() {
		pureMethods = loadInfo();
	}

	private Set<String> loadInfo() {
		Set<String> set = new HashSet<String>(2020);

		try {
			InputStream fstream = this.getClass().getResourceAsStream(
					"/jdkPureMethods.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				set.add(strLine);
			}
			in.close();
		} catch (IOException e) {
			System.err.println("Wrong filename/path/file is missing");
			e.printStackTrace();
		}
		if (set.isEmpty())
			throw new IllegalStateException(
					"Error in the initialization of the set containing the pure java.* methods");

		return set;
	}

	public boolean checkPurity(String qualifiedName) {
		return pureMethods.contains(qualifiedName);
	}

}
