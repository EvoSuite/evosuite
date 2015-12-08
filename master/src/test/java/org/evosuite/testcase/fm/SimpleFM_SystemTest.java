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
package org.evosuite.testcase.fm;

import com.examples.with.different.packagename.fm.*;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestCase;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * Created by Andrea Arcuri on 09/08/15.
 */
public class SimpleFM_SystemTest extends SystemTest{


    @Test
    public void testSimpleReturnString(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5;
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;

        GeneticAlgorithm<?> ga = do100percentLineTest(SimpleFM_returnString.class);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        String code = best.toString();
        Assert.assertTrue(code, code.contains("when"));
        Assert.assertTrue(code, code.contains("thenReturn"));
        Assert.assertTrue(code, code.contains("true"));
        Assert.assertTrue(code, code.contains("false"));
    }

    @Test
    public void testSimpleGenerics(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;

        do100percentLineTest(SimpleFM_Generics.class);
    }

    @Test
    public void testSimpleGenericsAsInput(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;

        do100percentLineTest(SimpleFM_GenericsAsInput.class);
    }

    @Test
    public void testSimpleGenericReturn(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;

        do100percentLineTest(SimpleFM_GenericReturn.class);
    }

    @Test
    public void testSimpleGenericNullString(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;

        GeneticAlgorithm<?> ga = do100percentLineTest(SimpleFM_GenericsNullString.class);

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        String code = best.toString();
        // "null" should be casted to "String" (if at all) and not to "Object"
        assertFalse(code, code.contains("(Object)"));
    }

    @Test
    public void testSimpleNullString(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;

        GeneticAlgorithm<?> ga = do100percentLineTest(SimpleFM_NullString.class);

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        String code = best.toString();
        // "null" should be casted to "String" (if at all) and not to "Object"
        assertFalse(code, code.contains("(Object)"));
    }

    @Ignore //FIXME once we handle package-level methods
    @Test
    public void testSimplePLM(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;

        do100percentLineTest(SimpleFM_PackageMethod.class);
    }

    @Ignore //FIXME once we handle package-level methods
    @Test
    public void testSimplePLMwithReturn(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;

        do100percentLineTest(SimpleFM_PackageMethodWithReturn.class);
    }

    @Test
    public void testSimpleNonFinal(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;

        do100percentLineTest(SimpleFM_nonFinal.class);
    }

    @Test
    public void testSimpleFinalClass(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;

        do100percentLineTest(SimpleFM_finalClass.class);
    }

    @Test
    public void testSimpleFinalMethod(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;

        do100percentLineTest(SimpleFM_finalMethod.class);
    }

    @Test
    public void testSimpleBoolean(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator

        do100percentLineTest(SimpleFM_Boolean.class);
    }

    @Test
    public void testSimpleDoubleMock(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator

        do100percentLineTest(SimpleFM_DoubleMock.class);
    }


    @Test
    public void testSimpleInt(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator

        do100percentLineTest(SimpleFM_Int.class);
    }

    @Test
    public void testSimpleString(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator

        do100percentLineTest(SimpleFM_String.class);
    }

    @Test
    public void testSimpleDependency(){

        Properties.P_FUNCTIONAL_MOCKING = 0.5; //any value above 0
        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator
        Properties.P_REFLECTION_ON_PRIVATE = 0;

        do100percentLineTest(SimpleFM_Dependency.class);
    }
}
