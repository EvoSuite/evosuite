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
package org.evosuite.runtime.mock.java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.testdata.EvoSuiteAddress;
import org.evosuite.runtime.testdata.EvoSuiteLocalAddress;
import org.evosuite.runtime.vnet.NativeTcp;
import org.evosuite.runtime.testdata.NetworkHandling;
import org.evosuite.runtime.vnet.VirtualNetwork;
import org.junit.Assert;
import org.junit.Before;

import org.junit.Test;

public class ServerSocketTest {

	@Before
	public void init(){
        MockFramework.enable();
        VirtualNetwork.getInstance().reset();
	}
	
	@Test
	public void testNotBound() throws IOException{
		MockServerSocket server = new MockServerSocket();
		try{
			server.accept();
			Assert.fail();
		} catch(Exception e){
			//expected, because not bound
		}
		
		int port = 42;		
		server.bind(new InetSocketAddress(port));
		Assert.assertTrue(server.isBound());
		
		try{
			server.accept();
			Assert.fail();
		} catch(Exception e){
			//expected. as there is no simulated inbound connection, the virtual network
			//should throw an IOE rather than blocking the test case
		}
		
		server.close();
	}
	
	@Test
	public void testCollidingBinding() throws IOException{
		MockServerSocket first = new MockServerSocket();
		int port = 42;		
		first.bind(new InetSocketAddress(port));
		
		MockServerSocket second = new MockServerSocket();
		try{
			second.bind(new InetSocketAddress(port));
			Assert.fail();
		} catch(IOException e){
			//expected, as binding on same port/interface
		}
		
		//binding on different port should work
		second.bind(new InetSocketAddress(port+1));
		
		first.close();
		second.close();
	}
	
	
	@Test
	public void testIncomingConnection() throws IOException{
		//first bind a listening server
		MockServerSocket server = new MockServerSocket();
		String localAddress = "127.0.0.1";
		int localPort = 42;
		server.bind(new InetSocketAddress(localAddress,localPort));
		
		//before accepting a connection, do register an incoming one
		String remoteAddress = "127.0.0.2";
		int remotePort = 1234;
		VirtualNetwork.getInstance().registerIncomingTcpConnection(remoteAddress, remotePort, localAddress, localPort);
		
		Socket socket = server.accept();
		Assert.assertNotNull(socket);
		Assert.assertEquals(remoteAddress,socket.getInetAddress().getHostAddress());
		Assert.assertEquals(remotePort, socket.getPort());
		Assert.assertEquals(localAddress, socket.getLocalAddress().getHostAddress());
		Assert.assertEquals(localPort, socket.getLocalPort());
		
		server.close();
		socket.close();
	}
	
	@Test 
	public void testReceiveAndReplyMessage() throws IOException{

		int n = VirtualNetwork.getInstance().getViewOfOpenedTcpConnections().size();
		Assert.assertEquals(0, n);

		//first bind a listening server
		MockServerSocket server = new MockServerSocket();
		String localAddress = "127.0.0.1";
		int localPort = 42;
		server.bind(new InetSocketAddress(localAddress,localPort));

		n = VirtualNetwork.getInstance().getViewOfOpenedTcpConnections().size();
		Assert.assertEquals(0, n);
		
		//send a message on tcp connection, although SUT is not listening yet
		String msg = "Hello World! Sent from mocked TCP connection";
        EvoSuiteLocalAddress addr = new EvoSuiteLocalAddress(localAddress,localPort);
		NetworkHandling.sendMessageOnTcp(addr, msg);
		
		//open listening port, and read message
		Socket socket = server.accept();
		Assert.assertNotNull(socket);
		InputStream in = socket.getInputStream();
		Assert.assertNotNull(in);
		Scanner inScan = new Scanner(in);
		String received = inScan.nextLine();
		inScan.close();
		Assert.assertEquals(msg, received);
		
		
		//send a reply to remote host on same TCP connection		
		String reply = "Reply to Hello Message";
		OutputStream out = socket.getOutputStream();
		out.write(reply.getBytes());

		n = VirtualNetwork.getInstance().getViewOfOpenedTcpConnections().size();
		Assert.assertEquals(1, n);
		NativeTcp connection = VirtualNetwork.getInstance().getViewOfOpenedTcpConnections().iterator().next();
		Assert.assertEquals(reply.length(), connection.getAmountOfDataInRemoteBuffer());
		
		server.close();
	}
}
