package com.examples.with.different.packagename.concolic;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class TestCaseWithURL {

	public boolean test(URL url) throws IOException {
		URLConnection conn = url.openConnection();
		Scanner in = new Scanner(conn.getInputStream());
		String line = in.nextLine();
		in.close();
		if (line.contains("<html>")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isZero(int value) {
		if (value == 0) {
			return true;
		} else {
			return false;
		}
	}
}
