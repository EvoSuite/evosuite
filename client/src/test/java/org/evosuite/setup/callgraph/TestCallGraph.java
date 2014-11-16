package org.evosuite.setup.callgraph;

import org.junit.Assert;
import org.junit.Test;

public class TestCallGraph {

	@Test
	public void test() {
		CallGraph c = new CallGraph("test");
		Assert.assertNotNull(c);
	}

}
