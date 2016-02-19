/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
