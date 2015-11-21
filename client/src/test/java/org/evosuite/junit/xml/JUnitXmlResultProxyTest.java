/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.junit.xml;

import static org.junit.Assert.*;

import java.io.File;

import org.evosuite.junit.FooTestClassLoader;
import org.evosuite.junit.JUnitResult;
import org.evosuite.utils.FileIOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JUnitXmlResultProxyTest {

	private static final String JUNIT_RESULT_FILENAME = "junitresult.xml";

	@Before
	public void setUp() throws Exception {
		cleanFileSystem();
	}

	@After
	public void tearDown() throws Exception {
		cleanFileSystem();
	}

	@Test
	public void test() {
		assertFalse(checkFileExists());
		JUnitExecutor executor = new JUnitExecutor();
		Class<?> fooTestClass = new FooTestClassLoader().loadFooTestClass();
		JUnitResult result = executor.execute(fooTestClass);

		FileIOUtils.writeXML(result, JUNIT_RESULT_FILENAME);
		assertTrue(checkFileExists());

		JUnitResult result_from_xml_file = FileIOUtils
				.<JUnitResult> readXML(JUNIT_RESULT_FILENAME);

		assertEquals(result, result_from_xml_file);
	}

	private void cleanFileSystem() {
		File junitResult = new File(JUNIT_RESULT_FILENAME);
		if (junitResult.exists()) {
			junitResult.delete();
		}
	}

	private boolean checkFileExists() {
		File file = new File(JUNIT_RESULT_FILENAME);
		return file.exists();

	}

}
