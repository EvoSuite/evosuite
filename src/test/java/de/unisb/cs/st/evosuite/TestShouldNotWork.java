/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite;

import static org.junit.Assert.fail;

import org.junit.Test;



public class TestShouldNotWork {

	@Test
	public void testShouldNotWorkOnJavaPackage(){
		EvoSuite evosuite = new EvoSuite();
		
		String targetClass = java.util.TreeMap.class.getCanonicalName();
		
		Properties.TARGET_CLASS = targetClass;
		
		String[] command = new String[]{				
				"-generateSuite",
				"-class",
				targetClass
		};

		try{
			Object result = evosuite.parseCommandLine(command);
		} catch(IllegalArgumentException e){
			//as expected
			System.out.println(e.toString());
			return;
		}
		
		fail("An exception should have been thrown");
	}
	
}
