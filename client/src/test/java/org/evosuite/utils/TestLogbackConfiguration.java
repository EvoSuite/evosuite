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
package org.evosuite.utils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestLogbackConfiguration {

	public static final PrintStream defaultOut = System.out;
	public static final PrintStream defaultErr = System.err;

	@After
	public void resetDefaultPrinters() {
		System.setOut(defaultOut);
		System.setErr(defaultErr);
		LoggingUtils.changeLogbackFile("logback.xml");
	}

    @Test
	public void testStdOutErr() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));

		ByteArrayOutputStream err = new ByteArrayOutputStream();
		System.setErr(new PrintStream(err));

		boolean loaded = LoggingUtils.changeLogbackFile(LoggingUtils.getLogbackFileName());
        Assert.assertTrue(loaded);
		org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestLogbackConfiguration.class);

		final String warnMsg = "this should go to std out";
		final String errMsg = "this should go to std err";

		logger.warn(warnMsg);
		logger.error(errMsg);

		String printedOut = out.toString();
		String printedErr = err.toString();

		Assert.assertTrue("Content of std out is: " + printedOut,
		                  printedOut.contains(warnMsg));
		Assert.assertTrue("Content of std err is: " + printedErr,
		                  printedErr.contains(errMsg));
		Assert.assertTrue("Content of std out is: " + printedOut,
		                  !printedOut.contains(errMsg));
		Assert.assertTrue("Content of std err is: " + printedErr,
		                  !printedErr.contains(warnMsg));
	}

}
