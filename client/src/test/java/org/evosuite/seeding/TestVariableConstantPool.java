package org.evosuite.seeding;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by gordon on 06/01/2017.
 */
public class TestVariableConstantPool {

    @Test
    public void testBasicProbabilities() {
        StaticConstantVariableProbabilityPool pool1 = new StaticConstantVariableProbabilityPool();
        StaticConstantVariableProbabilityPool pool2 = new StaticConstantVariableProbabilityPool();
        for(int i = 0; i < 99; i++) {
            pool1.add("Foo");
            pool2.add("Bar");
        }
        pool1.add("Bar");
        pool2.add("Foo");
        int count1 = 0;
        int count2 = 0;
        for(int i = 0; i <100; i++) {
            if(pool1.getRandomString().equals("Bar"))
                count1++;
            if(pool2.getRandomString().equals("Bar"))
                count2++;
        }
        assertTrue(count1 < count2);
    }

    @Test
    public void testBasicProbabilitiesDynamic() {
        DynamicConstantVariableProbabilityPool pool1 = new DynamicConstantVariableProbabilityPool();
        DynamicConstantVariableProbabilityPool pool2 = new DynamicConstantVariableProbabilityPool();
        for(int i = 0; i < 99; i++) {
            pool1.add("Foo");
            pool2.add("Bar");
        }
        pool1.add("Bar");
        pool2.add("Foo");
        int count1 = 0;
        int count2 = 0;
        for(int i = 0; i <100; i++) {
            if(pool1.getRandomString().equals("Bar"))
                count1++;
            if(pool2.getRandomString().equals("Bar"))
                count2++;
        }
        assertTrue(count1 < count2);
    }


    @Test
    public void testBasicProbabilitiesDynamicUpdate() {
        DynamicConstantVariableProbabilityPool pool = new DynamicConstantVariableProbabilityPool();
        for(int i = 0; i < 99; i++) {
            pool.add("Foo");
        }
        pool.add("Bar");
        int count1 = 0;
        for(int i = 0; i <100; i++) {
            if(pool.getRandomString().equals("Bar"))
                count1++;
        }

        for(int i = 0; i < 99; i++) {
            pool.add("Bar");
        }

        int count2 = 0;
        for(int i = 0; i <100; i++) {
            if(pool.getRandomString().equals("Bar"))
                count2++;
        }
        assertTrue(count1 < count2);
    }
}
