package com.examples.with.different.packagename.concolic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

public class TestCaseWithFile {

	public String test(File f) throws IOException {
		if (!f.exists()) {
			return "No File";
		}
		FileInputStream fileInputStream = new FileInputStream(f);
		Scanner fromFile = new Scanner(fileInputStream);
		String str = fromFile.nextLine();
		fromFile.close();
		if (str.equals("<<FILE CONTENT>>"))
			return str;
		else
			return null;
	}

	public static boolean isZero(int value) {
		if (value == 0) {
			return true;
		} else {
			return false;
		}
	}

}
