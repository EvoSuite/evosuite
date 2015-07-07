package com.examples.with.different.packagename.dse;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class DseWithServer {

	public boolean readLocalAddress() throws IOException {
		ServerSocket server = new ServerSocket();
		String localAddress = "127.0.0.1";
		int localPort = 42;
		server.bind(new InetSocketAddress(localAddress, localPort));

		Socket socket = server.accept();
		InputStream in = socket.getInputStream();
		Scanner inScan = new Scanner(in);
		String received = inScan.nextLine();
		inScan.close();
		server.close();
		if (received.startsWith("Hello") && received.endsWith("World")) {
			return true;
		} else {
			return false;
		}
	}
}
