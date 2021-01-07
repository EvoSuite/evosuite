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
public class SingleLambda
{
	interface CheckCapture 
	{
		boolean test();
	}
	
	
	/**
	 * JVM seems to create fields in the generated Lambda class
	 * in the same order they are later passed to the lambda method
	 */
	public static int basic(int x, boolean f)
	{
		CheckCapture cc = () -> x==42 && f; 
		boolean b = cc.test();		
		
		if (b)
			return 0;
  		return 1;
	}

	/**
	 * Exact same signature as the javac-generated lambda,
	 * (except that this method here is not synthetic).
	 * 
	 * jvm rejects it, but the Eclipse compiler does not :-)
	 */
	// Todo: Check this
//	private static boolean lambda$0(int i, boolean b) {
//		return false;
//	}
	
	/*
  private static synthetic lambda$0(IZ)Z
    ILOAD 0
    BIPUSH 42
    IF_ICMPNE L1
    ILOAD 1
    IFEQ L1
    ICONST_1
    GOTO L2
   L1
    ICONST_0
   L2
    IRETURN
	 */
}
