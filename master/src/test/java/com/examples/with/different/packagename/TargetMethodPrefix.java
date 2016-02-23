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
