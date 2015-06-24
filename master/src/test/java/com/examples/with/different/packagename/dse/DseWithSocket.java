package com.examples.with.different.packagename.dse;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;

public class DseWithSocket {

	public boolean readSocket(Socket socket) throws IOException {
		InputStream in = socket.getInputStream();
		Scanner inScan = new Scanner(in);
		String received = inScan.nextLine();
		inScan.close();
		if (received.equals("Hello World!")) {
			return true;
		} else {
			return false;
		}
	}
}
