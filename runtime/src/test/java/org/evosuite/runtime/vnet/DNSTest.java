package org.evosuite.runtime.vnet;

import org.junit.Assert;

import org.junit.Test;

public class DNSTest {

	@Test
	public void testResolve(){
		
		DNS dns = new DNS();
		
		String lbAddr = "127.0.0.1";
		
		String lb = dns.resolve(lbAddr);
		String google = dns.resolve("www.google.com");
		String evosuite = dns.resolve("www.evosuite.org");
		
		Assert.assertEquals(lbAddr,lb);
		
		Assert.assertNotNull(google);
		Assert.assertNotNull(evosuite);
		
		Assert.assertNotEquals(lb,google);
		Assert.assertNotEquals(lb,evosuite);
		Assert.assertNotEquals(google,evosuite);
	}
}
