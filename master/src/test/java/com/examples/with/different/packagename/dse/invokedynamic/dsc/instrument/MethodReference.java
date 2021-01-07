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
public class MethodReference
{
	interface CheckParam {
		boolean test(int p);
	}
	
	interface PassParam {
		int test(int p);
	}
	
	interface GetInt {
		int test();
	}	
	

	public boolean foo(int a) {
		return a==42;
	}

	public static boolean bar(int a) {
		return a==42;
	}	
	
	public boolean instanceRef(int a)
	{
		/*
    ALOAD 0
    INVOKEDYNAMIC test(Lforroops/instrument/MethodReference;)Lforroops/instrument/MethodReference$CheckParam; [
      java/lang/invoke/LambdaMetafactory.metafactory(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
      (I)Z, 
      forroops/instrument/MethodReference.foo(I)Z, 
      (I)Z
    ]
    ASTORE 2
    ALOAD 2
    ILOAD 1
    INVOKEINTERFACE forroops/instrument/MethodReference$CheckParam.test (I)Z
		 */
		CheckParam check = this::foo;
		return check.test(a);
	}
	

	public static int instanceRefTest(int x)
	{
		boolean res = new MethodReference().instanceRef(x);
		
		if (res)
			return 0;
  		return 1;
	}
	
	
	public static int staticRefTest(int x)
	{
		/*
    INVOKEDYNAMIC test()Lforroops/instrument/MethodReference$CheckParam; [
      java/lang/invoke/LambdaMetafactory.metafactory(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
      (I)Z, 
      forroops/instrument/MethodReference.bar(I)Z, 
      (I)Z
    ]
    ASTORE 1
    ALOAD 1
    ILOAD 0
    INVOKEINTERFACE forroops/instrument/MethodReference$CheckParam.test (I)Z
		 */
		CheckParam check = MethodReference::bar;		
		boolean res = check.test(x);
		
		if (res)
			return 0;
  		return 1;
	}	
	
	
	public static void staticRefJDK(int x)
	{
		PassParam pass = Integer::hashCode;		
		int res = pass.test(x);
		// since we do not instrument JDK classes, we do not
		// really expect to force res to certain values.
	}
	
	
	public static void instanceRefJDK(int x)
	{
		Integer myInt = Integer.valueOf(x);
		
		GetInt magic = myInt::intValue;		
		int res = magic.test();
		
		// since we do not instrument JDK classes, we do not
		// really expect to force res to certain values.
	}					
}
