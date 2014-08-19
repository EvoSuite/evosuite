package org.evosuite.runtime.mock.java.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.junit.Assert;

import org.evosuite.runtime.vnet.VirtualNetwork;
import org.junit.Before;
import org.junit.Test;

public class SocketTest {

	@Before
	public void init(){
		VirtualNetwork.getInstance().reset();
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
		
		/* FIXME
		try{
			s.connect(saddr);
			Assert.fail();
		} catch(IOException e){
			//expected, as no listener
		}
		*/
	}
	
}
