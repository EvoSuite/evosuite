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
package org.evosuite.runtime;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;

import org.junit.Assume;
import org.junit.Test;

public class GuiSupportTest {

	//only one of the 2 tests can be actually executed, as dependent on JVM options
	
	@Test
	public void testWhenHeadless(){
		Assume.assumeTrue(GraphicsEnvironment.isHeadless());
		
		GuiSupport.setHeadless(); //should do nothing
		Assert.assertTrue(GraphicsEnvironment.isHeadless());
		
		GuiSupport.restoreHeadlessMode(); //should do nothing
		Assert.assertTrue(GraphicsEnvironment.isHeadless());		
	}
	
	@Test
	public void testWhenNotHeadless(){
		Assume.assumeTrue(! GraphicsEnvironment.isHeadless());
		
		GuiSupport.setHeadless(); 
		Assert.assertTrue(GraphicsEnvironment.isHeadless());
		
		GuiSupport.restoreHeadlessMode(); //should restore headless
		Assert.assertTrue(! GraphicsEnvironment.isHeadless());		
	}
}
