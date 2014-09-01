package org.evosuite.runtime.mock.java.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Scanner;

import org.junit.Assert;

import org.evosuite.runtime.vnet.EndPointInfo;
import org.evosuite.runtime.vnet.RemoteTcpServer;
import org.evosuite.runtime.vnet.VirtualNetwork;
import org.evosuite.runtime.vnet.VirtualNetwork.ConnectionType;
import org.junit.Before;
import org.junit.Test;

public class SocketTest {

	@Before
	public void init(){
		VirtualNetwork.getInstance().init();
	}
	
	@Test
	public void testNullConnect() throws IOException{
		
		MockSocket s = new MockSocket();
		try {
			s.connect(null);
		} catch (IllegalArgumentException e) {
			//expected
		}
	}
	
	@Test
	public void testConnectNoAnswer() throws IOException{
		
		InetAddress addr = InetAddress.getByName("127.42.42.42");
		SocketAddress saddr = new InetSocketAddress(addr, 12345);
		MockSocket s = new MockSocket();
				
		try{
			s.connect(saddr);
			Assert.fail();
		} catch(IOException e){
			//expected, as no listener
		}		
	}
	
	
	@Test
	public void testSingleConnection() throws IOException{
		
		String remoteHost = "127.42.0.42";
		int remotePort = 666;
		InetSocketAddress saddr = new InetSocketAddress(InetAddress.getByName(remoteHost), remotePort);
		MockSocket s = new MockSocket();
		
		RemoteTcpServer server = new RemoteTcpServer(new EndPointInfo(saddr.getAddress().getHostAddress(), saddr.getPort(), ConnectionType.TCP));
		String msgFromServer = "server";
		server.sendMessage(msgFromServer);
		
		s.connect(saddr);
		
		String msgFromSUT = "SUT";
		s.getOutputStream().write(msgFromSUT.getBytes());
		
		String sutReceived = new Scanner(s.getInputStream()).nextLine();
		Assert.assertEquals(msgFromServer, sutReceived);
		Assert.assertEquals(msgFromSUT, server.getAllReceivedDataAsString());
		
		s.close();
	}
}
