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
package org.evosuite.testcase.fm;

import com.examples.with.different.packagename.fm.*;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Andrea Arcuri on 09/08/15.
 */
public class SimpleFM_SystemTest extends SystemTestBase {

    @Before
    public void init(){
        Properties.P_FUNCTIONAL_MOCKING = 0.5;
        Properties.FUNCTIONAL_MOCKING_PERCENT = 0.0;
    }

    @Test
    public void testGenericsReturnWithExtend_Single(){
        do100percentLineTest(SimpleFM_GenericsReturnWithExtend_Single.class);
    }

    @Test
    public void testGenericsReturnWithExtend_Double(){
        do100percentLineTest(SimpleFM_GenericsReturnWithExtend_Double.class);
    }

    @Test
    public void testGenericsReturnWithExtend_Double_Feasability(){

        SimpleFM_GenericsReturnWithExtend_Double.W w = mock(SimpleFM_GenericsReturnWithExtend_Double.W.class);
        when(w.isW()).thenReturn(true);

        SimpleFM_GenericsReturnWithExtend_Double.Z z = mock(SimpleFM_GenericsReturnWithExtend_Double.Z.class);
        when(z.isZ()).thenReturn(true);

        SimpleFM_GenericsReturnWithExtend_Double.A a = mock(SimpleFM_GenericsReturnWithExtend_Double.A.class);
        when(a.getB()).thenReturn(w, z);

        boolean res = SimpleFM_GenericsReturnWithExtend_Double.foo(a);
        assertTrue(res);
    }


    @Test
    public void testSimpleReturnString(){
        GeneticAlgorithm<?> ga = do100percentLineTest(SimpleFM_returnString.class);
        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        String code = best.toString();
        Assert.assertTrue(code, code.contains("doReturn"));
        Assert.assertTrue(code, code.contains("when"));
        Assert.assertTrue(code, code.contains("true"));
        Assert.assertTrue(code, code.contains("false"));
    }

    @Test
    public void testSimpleGenerics(){
        do100percentLineTest(SimpleFM_Generics.class);
    }

    @Test
    public void testSimpleGenericsAsInput(){
        do100percentLineTest(SimpleFM_GenericsAsInput.class);
    }

    @Test
    public void testSimpleGenericReturn(){
        do100percentLineTest(SimpleFM_GenericReturn.class);
    }

    @Test
    public void testSimpleGenericNullString(){
        GeneticAlgorithm<?> ga = do100percentLineTest(SimpleFM_GenericsNullString.class);

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        String code = best.toString();
        // "null" should be casted to "String" (if at all) and not to "Object"
        assertFalse(code, code.contains("(Object)"));
    }

    @Test
    public void testSimpleNullString(){
        GeneticAlgorithm<?> ga = do100percentLineTest(SimpleFM_NullString.class);

        TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
        String code = best.toString();
        // "null" should be casted to "String" (if at all) and not to "Object"
        assertFalse(code, code.contains("(Object)"));
    }

    @Ignore //FIXME once we handle package-level methods
    @Test
    public void testSimplePLM(){
        do100percentLineTest(SimpleFM_PackageMethod.class);
    }

    @Ignore //FIXME once we handle package-level methods
    @Test
    public void testSimplePLMwithReturn(){
        do100percentLineTest(SimpleFM_PackageMethodWithReturn.class);
    }

    @Test
    public void testSimpleNonFinal(){
        do100percentLineTest(SimpleFM_nonFinal.class);
    }

    @Test
    public void testSimpleFinalClass(){
        do100percentLineTest(SimpleFM_finalClass.class);
    }

    @Test
    public void testSimpleFinalMethod(){
        do100percentLineTest(SimpleFM_finalMethod.class);
    }

    @Test
    public void testSimpleBoolean(){
        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator
        do100percentLineTest(SimpleFM_Boolean.class);
    }

    @Test
    public void testSimpleDoubleMock(){
        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator
        do100percentLineTest(SimpleFM_DoubleMock.class);
    }


    @Test
    public void testSimpleInt(){
        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator
        do100percentLineTest(SimpleFM_Int.class);
    }

    @Test
    public void testSimpleString(){
        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator
        do100percentLineTest(SimpleFM_String.class);
    }

    @Test
    public void testSimpleDependency(){

        Properties.FUNCTIONAL_MOCKING_PERCENT = 1; //practically do not use FM, unless no generator
        Properties.P_REFLECTION_ON_PRIVATE = 0;
        do100percentLineTest(SimpleFM_Dependency.class);
    }
}
