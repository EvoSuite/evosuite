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
package org.evosuite.testcase;

import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.LongPrimitiveStatement;
import org.evosuite.testsuite.similarity.DiversityObserver;
import org.evosuite.utils.generic.GenericConstructor;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * Created by gordon on 18/12/2015.
 */
public class TestSimilarity {

    @Test
    public void testSelfSimilarityBase() {

        TestCase test = new DefaultTestCase();
        double score = DiversityObserver.getNeedlemanWunschScore(test, test);
        Assert.assertTrue(score <= 0.0);
    }

    @Test
    public void testSelfSimilarity() {

        TestCase test = new DefaultTestCase();

        PrimitiveStatement<?> aInt = new IntPrimitiveStatement(test, 42);
        test.addStatement(aInt);

        double score = DiversityObserver.getNeedlemanWunschScore(test, test);
        Assert.assertTrue(score > 0);
    }

    @Test
    public void testBasicSimilarity() {

        TestCase test1 = new DefaultTestCase();
        TestCase test2 = new DefaultTestCase();

        PrimitiveStatement<?> aInt = new IntPrimitiveStatement(test1, 42);
        test1.addStatement(aInt);

        PrimitiveStatement<?> bInt = new IntPrimitiveStatement(test2, 42);
        test2.addStatement(bInt);

        double score = DiversityObserver.getNeedlemanWunschScore(test1, test2);
        Assert.assertTrue(score > 0);
    }

    @Test
    public void testBasicSimilarityDifferentLength() {

        TestCase test1 = new DefaultTestCase();
        TestCase test2 = new DefaultTestCase();

        PrimitiveStatement<?> aInt = new IntPrimitiveStatement(test1, 42);
        test1.addStatement(aInt);
        PrimitiveStatement<?> aInt2 = new IntPrimitiveStatement(test1, 42);
        test1.addStatement(aInt2);

        PrimitiveStatement<?> bInt = new IntPrimitiveStatement(test2, 42);
        test2.addStatement(bInt);

        double score = DiversityObserver.getNeedlemanWunschScore(test1, test2);
        Assert.assertTrue(score <= 0);
    }

    @Test
    public void testBasicSimilarityDifferentTypes() {

        TestCase test1 = new DefaultTestCase();
        TestCase test2 = new DefaultTestCase();

        PrimitiveStatement<?> aInt = new IntPrimitiveStatement(test1, 42);
        test1.addStatement(aInt);
        PrimitiveStatement<?> aInt2 = new IntPrimitiveStatement(test1, 42);
        test1.addStatement(aInt2);

        PrimitiveStatement<?> bInt = new IntPrimitiveStatement(test2, 42);
        test2.addStatement(bInt);
        Constructor<?> c = Object.class.getConstructors()[0];
        ConstructorStatement cs = new ConstructorStatement(test2, new GenericConstructor(c, Object.class), new ArrayList<>());
        test2.addStatement(cs);

        double score = DiversityObserver.getNeedlemanWunschScore(test1, test2);
        Assert.assertTrue(score <= 0);
    }

    @Test
    public void testBasicSimilarityDifferentTypes2() {

        TestCase test1 = new DefaultTestCase();
        TestCase test2 = new DefaultTestCase();

        PrimitiveStatement<?> aInt = new LongPrimitiveStatement(test1, 42L);
        test1.addStatement(aInt);
        PrimitiveStatement<?> aInt2 = new IntPrimitiveStatement(test1, 42);
        test1.addStatement(aInt2);

        PrimitiveStatement<?> bInt = new IntPrimitiveStatement(test2, 42);
        test2.addStatement(bInt);
        Constructor<?> c = Object.class.getConstructors()[0];
        ConstructorStatement cs = new ConstructorStatement(test2, new GenericConstructor(c, Object.class), new ArrayList<>());
        test2.addStatement(cs);

        double score = DiversityObserver.getNeedlemanWunschScore(test1, test2);
        Assert.assertTrue(score <= 0);
    }
}
