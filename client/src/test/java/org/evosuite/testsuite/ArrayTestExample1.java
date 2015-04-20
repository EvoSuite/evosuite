/**
 * 
 */
package org.evosuite.testsuite;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Gordon Fraser
 * 
 */
public class ArrayTestExample1 {
	@Ignore
	@Test
	public void test1() {
		int[] test = new int[10];
	}

	@Ignore
	@Test
	public void test2() {
		int[] test = new int[10];
		test[5] = 7;
		test[7] = 3;
		test[9] = 6;
	}

	@Ignore
	@Test
	public void test3() {
		int[] test = new int[10];
		int[] test2 = new int[2];
		test[5] = 7;
		test[7] = 3;
		test[9] = 6;
	}

	@Ignore
	@Test
	public void test4() {
		int[] test = new int[10];
		test[0] = 7;
		test[1] = 7;
		test[2] = 7;
		test[3] = 7;
		test[4] = 7;
		test[5] = 7;
		test[6] = 7;
		test[7] = 3;
		test[9] = 6;
	}

	@Ignore
	@Test
	public void test5() {
		int[] test = new int[2];
		int x = 7;
		test[0] = x;
		test[1] = x;
	}
}
