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

import com.examples.with.different.packagename.dse.StreamAPIExample;
import com.examples.with.different.packagename.dse.StringConcatenationExample;
//import com.examples.with.different.packagename.dse.interfaces.InterfacePrivateMethodExample;
import com.examples.with.different.packagename.dse.invokedynamic.ClosureFieldTest;
import com.examples.with.different.packagename.dse.invokedynamic.InvokeExactExample;
import com.examples.with.different.packagename.dse.invokedynamic.LambdaExample;
import com.examples.with.different.packagename.dse.invokedynamic.SingleMethodReference;
import com.examples.with.different.packagename.dse.invokedynamic.TestClosureClass;
import com.examples.with.different.packagename.dse.invokedynamic.TestSAMConversions;
import org.junit.Test;

/**
 * Tests for the invokedynamic usages on java (JDK9 for now)
 *
 * @author Ignacio Lebrero
 */
public final class DSEJDK9SystemTest extends DSESystemTestBase {

    /**************** InvokeDynamic ****************/

    /**
     * Lambdas (JDK 8)
     */
    @Test
    public void testLambda() {
        testDSEExecution(6, 1, LambdaExample.class);
    }

    @Test
    public void testClosure() {
        testDSEExecution(8, 1, TestClosureClass.class);
    }

    @Test
    public void testClosureAsAField() {
        testDSEExecution(2, 1, ClosureFieldTest.class);
    }

    @Test
    public void SAMConversion() {
        testDSEExecution(3, 1, TestSAMConversions.class);
    }

    /**
     * Method references (JDK 8)
     */
    @Test
    public void testMethodReference() {
        testDSEExecution(7, 1, SingleMethodReference.class);
    }

    /**
     * We are not currently supporting the Stream API as it calls lambdas from a non-instrumented context.
     */
    @Test
    public void testStreamAPI() {
        testDSEExecution(4, 8, StreamAPIExample.class);
    }

    /**
     * String concatenation (JDK 9)
     */
    @Test
    public void testStringConcatenation() {
        testDSEExecution(2, 1, StringConcatenationExample.class);
    }

    /**
     * Method Handles (JDK 8)
     */
    // TODO: complete eventually, for now we won't support it as we don't support the reflection API either
    @Test
    public void testInvokeExact() {
        // As we don't support the Method handles API, there should be an exception throughout execution and the result should be empty.
        testDSEExecutionEmptyResult(InvokeExactExample.class);
    }

    /**************** Milling Project Coin ****************/
// TODO: Uncomment test and class when we build evosuite with JDK 9, it should work fine
// 	@Test public void testPrivateMethodsInInterfaces() {
//		testDSEExecution(4, 1, InterfacePrivateMethodExample.class);
//	}

}