/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package com.examples.with.different.packagename.dse.invokedynamic.dsc.instrument;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */

/** 
 * @author csallner@uta.edu (Christoph Csallner)
 */
public class SingleMethodReference
{	
	interface GetInt {
		int test(int y);
	}

	private static class MyIntegerClass implements GetInt {
		private int val;

		public MyIntegerClass(int x) {
			this.val = x;
		}

		@Override
		public int test(int y) {
			if (this.val * y > 748)
				return 0;
			else
				return 1;
		}
	}
		
	public static int instanceRef(int x, int y)
	{
		MyIntegerClass myInt = new MyIntegerClass(x);
		
		GetInt magic = myInt::test;
		return magic.test(y);
	}
}
