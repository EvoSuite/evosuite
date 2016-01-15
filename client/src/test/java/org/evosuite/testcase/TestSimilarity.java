package org.evosuite.testcase;

import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.LongPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
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
        ConstructorStatement cs = new ConstructorStatement(test2, new GenericConstructor(c, Object.class), new ArrayList<VariableReference>());
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
        ConstructorStatement cs = new ConstructorStatement(test2, new GenericConstructor(c, Object.class), new ArrayList<VariableReference>());
        test2.addStatement(cs);

        double score = DiversityObserver.getNeedlemanWunschScore(test1, test2);
        Assert.assertTrue(score <= 0);
    }
}
