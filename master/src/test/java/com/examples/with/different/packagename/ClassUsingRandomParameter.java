/**
 * 
 */
package com.examples.with.different.packagename;

import java.util.Random;

/**
 * @author Gordon Fraser
 * 
 */
public class ClassUsingRandomParameter {

	public boolean doSomethingRandom(Random r, int x) {
		if (r.nextInt() == x)
			return true;

		return false;
	}
}
