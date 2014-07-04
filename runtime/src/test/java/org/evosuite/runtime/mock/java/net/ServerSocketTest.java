package org.evosuite.runtime.mock.java.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.evosuite.runtime.VirtualNetwork;
import org.junit.Assert;
import org.junit.Before;

import org.junit.Test;

public class ServerSocketTest {

	@Before
	public void init(){
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
	}
}
