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
