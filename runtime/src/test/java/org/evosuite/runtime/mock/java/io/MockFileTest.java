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
package org.evosuite.runtime.mock.java.io;

import java.io.File;

import org.evosuite.runtime.mock.java.io.MockFile;
import org.junit.Assert;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MockFileTest {

	@Test
	public void testSamePath(){
		
		String name = "foo.txt";
		File real = new File(name);
		MockFile mock = new MockFile(name);
		
		assertEquals(real.toString(), mock.toString());
		assertEquals(real.getPath(), mock.getPath());
	}


	@Test
	public void testAssumptionOnConstructors_Real(){

		String name = "base";

		File base = new File(name);
		File emptyParent = new File("",name);
		File nullParent = new File((String)null,name);

		assertEquals(base.getAbsolutePath(), nullParent.getAbsolutePath());
		assertTrue(base.getAbsolutePath().length() > (name.length()+1));

		if(emptyParent.getAbsolutePath().startsWith("/")) {
			//Mac/Linux
			assertEquals("/" + name, emptyParent.getAbsolutePath());
		}
	}

	@Test
	public void testAssumptionOnConstructors_Mock(){

		String name = "base";

		MockFile base = new MockFile(name);
		MockFile emptyParent = new MockFile("",name);
		MockFile nullParent = new MockFile((String)null,name);

		assertEquals(base.getAbsolutePath(), nullParent.getAbsolutePath());
		assertTrue(base.getAbsolutePath().length() > (name.length()+1));

		if(emptyParent.getAbsolutePath().startsWith("/")) {
			//Mac/Linux
			assertEquals("/" + name, emptyParent.getAbsolutePath());
		}
	}

}
