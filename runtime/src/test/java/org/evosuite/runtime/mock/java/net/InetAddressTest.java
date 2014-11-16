package org.evosuite.runtime.mock.java.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;

public class InetAddressTest {

	@Test
	public void testGetByName() throws UnknownHostException{
		
		String googleAddr = "www.google.com";
		String evosuiteAddr = "www.evosuite.org";
		
		InetAddress google = MockInetAddress.getByName(googleAddr);
		InetAddress evosuite = MockInetAddress.getByName(evosuiteAddr);
		
		Assert.assertEquals(googleAddr, google.getHostName());
		Assert.assertEquals(evosuiteAddr, evosuite.getHostName());
		
		Assert.assertNotEquals(google.getHostAddress(), evosuite.getHostAddress());
	}
}
