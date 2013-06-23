package com.examples.with.different.packagename;

/**
 * The purpose of this class is to test the parameter TARGET_METHOD_PREFIX in contrast 
 * to TARGET_METHOD. The branches must be easily reached.
 * 
 * @author galeotti
 *
 */
public class TargetMethodPrefix {

	/**
	 * This private method has 2 branches
	 * @param x
	 * @return
	 * @throws InterruptedException 
	 */
	private boolean foo_bar0(String x) {
		if (x.length() > 1)
			return true;
		else
			return false;
	}

	/**
	 * This private method has 2 branches
	 * @param x
	 * @return
	 * @throws InterruptedException 
	 */
	private boolean foo_bar1(String x) {
		if (x.length() > 0)
			return true;
		else
			return false;
	}

	/**
	 * This private method has 4 branches
	 * @param x
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean foo(String x, String y) {
		if (x != null)
			return foo_bar0(x);
		else if (y != null)
			return foo_bar1(y);
		else
			return false;
	}


	/**
	 * This private method has more than two branches
	 * @param x
	 * @return
	 * @throws InterruptedException 
	 */
	public boolean otherMethodWithDiffPrefix(String x, String y) {
		if (x == y)
			return foo(x, y);
		else if (x!=null)
			return foo_bar0(x);
		else
			return foo_bar1(x);
	}

}
