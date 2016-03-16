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
package org.evosuite.javaagent;

public final class InnerClassesTestSubject 
{
	@Override
	public String toString()
	{
		System.out.println("Starting toString()");
		
		Object obj = new Object(){
			@Override 
			public String toString()
			{
				return "a";
			}
						
		};
		
		String a = obj.toString();
		
		Foo0 foo0 = new Foo0();
		String b = foo0.toString();

		Foo1 foo1 = new Foo1();
		String c = foo1.toString();
		
		Foo2 foo2 = new Foo2();
		String d = foo2.toString();
		
		return a+b+c+d;
	}
	
	private class Foo0
	{
		public String toString()
		{
			System.out.println("Printing in private class");
			return "b";
		}
	}
	
	private static class Foo1
	{
		public String toString()
		{
			System.out.println("Printing in private static class");
			return "c";
		}		
	}

	public static final class Foo2
	{
		public String toString()
		{
			System.out.println("Printing in public static final class");
			return "d";
		}		
	}
}
