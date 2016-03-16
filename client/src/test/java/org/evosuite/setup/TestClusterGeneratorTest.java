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
package org.evosuite.setup;

import java.io.File;

import org.evosuite.runtime.RuntimeSettings;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestClusterGeneratorTest {

	private static final boolean defaultVFS = RuntimeSettings.useVFS;
	
	@After
	public void tearDown(){
		RuntimeSettings.useVFS = defaultVFS;
	}
	
	@Test
	public void test_checkIfCanUse_noVFS(){
		
		RuntimeSettings.useVFS = false;
		boolean canUse = TestClusterUtils.checkIfCanUse(File.class.getCanonicalName());
		Assert.assertTrue(canUse);
	}

	@Test
	public void test_checkIfCanUse_withVFS(){
		
		RuntimeSettings.useVFS = true;
		boolean canUse = TestClusterUtils.checkIfCanUse(File.class.getCanonicalName());
		Assert.assertFalse(canUse);
	}
}
