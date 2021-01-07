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
package org.evosuite.dse;

import com.examples.with.different.packagename.dse.LambdaExample;
import com.examples.with.different.packagename.dse.StreamAPIExample;
import com.examples.with.different.packagename.dse.StringConcatenationExample;
import com.examples.with.different.packagename.dse.TestClosureClass;
import com.examples.with.different.packagename.dse.TestSAMConversions;
import com.examples.with.different.packagename.dse.invokedynamic.dsc.instrument.SingleMethodReference;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the invokedynamic usages on java (JDK9 for now)
 *
 * @author Ignacio Lebrero
 */
public class DSEInvokeDynamicSystemTest extends DSESystemTestBase {

    /** Lambdas (JDK 8) */

	@Test
	public void testLambda() {
		testDSEExecution(6, 1, LambdaExample.class);
	}

	@Test
	public void testClosure() {
		testDSEExecution(8, 1, TestClosureClass.class);
	}

	@Test
	public void SAMConversion() {
		testDSEExecution(3, 1, TestSAMConversions.class);
	}

	/** Method references (JDK 8) */

	@Test
	public void testMethodReference() {
		testDSEExecution(6, 2, SingleMethodReference.class);
	}

	/**
	 * We are not currently supporting the Stream API as it calls lambdas from a non-instrumented context.
	 */
	@Test
	public void testStreamAPI() {
		testDSEExecution(4, 8, StreamAPIExample.class);
	}

	/** String concatenation (JDK 9) */

	@Test
	public void testStringConcatenation() {
		testDSEExecution(2, 1, StringConcatenationExample.class);
	}

	/** Method Handles (JDK 8) */
	// TODO: complete eventually, for now we won't support it as we don't support the reflection API either
}