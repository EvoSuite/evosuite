package org.evosuite.runtime.mock.java.net;

import java.io.IOException;

import org.evosuite.runtime.vnet.VirtualNetwork;
import org.junit.Before;
import org.junit.Test;

public class SocketTest {

	@Before
	public void init(){
		VirtualNetwork.getInstance().reset();
	}
	
	@Test
	public void testConnect(){
		
		MockSocket s = new MockSocket();
		try {
			s.connect(null);
		} catch (IOException e) {
			//expected
		}
	}
}
