package com.examples.with.different.packagename.sandbox;

import org.junit.Test;

/**
 * Test used by carver to get the right chromosome for OpenStreamInATryCatch
 * @author arcuri
 *
 */
public class OpenStreamInATryCatch_FakeTestToCarve {

	@Test
	public void test(){
		OpenStreamInATryCatch foo = new OpenStreamInATryCatch();
		foo.open(42);
	}
}
