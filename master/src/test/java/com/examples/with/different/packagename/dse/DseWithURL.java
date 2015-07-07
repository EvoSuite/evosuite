package com.examples.with.different.packagename.dse;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class DseWithURL {

	public boolean checkURL(URL url) throws IOException {
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
}
