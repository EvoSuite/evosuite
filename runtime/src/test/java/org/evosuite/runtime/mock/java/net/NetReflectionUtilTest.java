package org.evosuite.runtime.mock.java.net;

import org.junit.Assert;
import org.junit.Test;

public class NetReflectionUtilTest {

	@Test
	public void test_anyLocalAddress(){
		Assert.assertNotNull(NetReflectionUtil.anyLocalAddress());
	}
}
