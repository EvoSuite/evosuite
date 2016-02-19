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
package org.evosuite.mock.java.io;

import java.io.File;

import org.junit.Assert;

import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.mock.java.io.MockFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.mock.java.io.ExtendingFile;

public class ParentReplacementTest {

	private static final boolean USING_VFS = Properties.VIRTUAL_FS;
	
	@After
	public void reset(){
		RuntimeSettings.useVFS = USING_VFS;
		Properties.VIRTUAL_FS = USING_VFS;
	}
	
	@Before
	public void init(){
		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
	}
	
	@Test
	public void testNoVFS() throws ClassNotFoundException{
		
		RuntimeSettings.useVFS = false;
		Properties.VIRTUAL_FS = false;
		InstrumentingClassLoader cl = new InstrumentingClassLoader();
		Class<?> clazz = cl.loadClass(ExtendingFile.class.getCanonicalName());
		
		Class<?> parent = clazz.getSuperclass();
		Assert.assertEquals(File.class.getCanonicalName(), parent.getCanonicalName());
	}

	@Test
	public void testWithVFS() throws ClassNotFoundException{
		
		RuntimeSettings.useVFS = true;
		Properties.VIRTUAL_FS = true;
		InstrumentingClassLoader cl = new InstrumentingClassLoader();
		Class<?> clazz = cl.loadClass(ExtendingFile.class.getCanonicalName());
		
		Class<?> parent = clazz.getSuperclass();
		Assert.assertEquals(MockFile.class.getCanonicalName(), parent.getCanonicalName());
	}

}
