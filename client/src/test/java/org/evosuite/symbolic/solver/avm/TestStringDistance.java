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
package org.evosuite.symbolic.solver.avm;

import org.evosuite.RandomizedTC;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.constraint.StringConstraint;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.solver.DistanceEstimator;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestStringDistance extends RandomizedTC {

    private static final double DELTA = 0;// Math.pow(1,-10000000);


    @Test
    public void testEquals() {
        final String str1 = "abc";
        final String str2 = "abcd";
        Collection<Constraint<?>> cnstr = createConstraints(str1, str2);
        double distance = DistanceEstimator.getDistance(cnstr);
        assertEquals(0.5, distance, 0.0);
    }

    @Test
    public void testEquals2() {
        final String str1 = "abcd";
        final String str2 = "abc";
        Collection<Constraint<?>> cnstr = createConstraints(str1, str2);
        double distance = DistanceEstimator.getDistance(cnstr);
        assertEquals(0.5, distance, 0.0);
    }

    @Test
    public void testEquals3() {
        final String str1 = "abc";
        final String str2 = "abcde";
        Collection<Constraint<?>> cnstr = createConstraints(str1, str2);
        double distance = DistanceEstimator.getDistance(cnstr);
        assertEquals(0.666666666666666666, distance, DELTA);
    }

    @Test
    public void testEquals4() {
        final String str1 = "abc";
        final String str2 = "xbc";
        Collection<Constraint<?>> cnstr = createConstraints(str1, str2);
        double distance = DistanceEstimator.getDistance(cnstr);
        assertEquals(0.48936170212765956, distance, DELTA);
    }

    @Test
    public void testEquals5() {
        final String str1 = "abc";
        final String str2 = "xbc";
        Collection<Constraint<?>> cnstr1 = createConstraints(str1, str2);
        double distance1 = DistanceEstimator.getDistance(cnstr1);

        final String str3 = "abc";
        final String str4 = "abcd";
        Collection<Constraint<?>> cnstr2 = createConstraints(str3, str4);
        double distance2 = DistanceEstimator.getDistance(cnstr2);

        assertTrue(distance1 < distance2);
    }


    @Test
    public void testEquals6() {
        final String str1 = "abc";
        final String str2 = "xbc";
        Collection<Constraint<?>> cnstr1 = createConstraints(str1, str2);
        double distance1 = DistanceEstimator.getDistance(cnstr1);

        final String str3 = "abc";
        final String str4 = "bbc";
        Collection<Constraint<?>> cnstr2 = createConstraints(str3, str4);
        double distance2 = DistanceEstimator.getDistance(cnstr2);

        assertTrue(distance2 < distance1);
    }

    @Test
    public void testEquals7() {
        final String str1 = "s";
        final String str2 = "test";
        Collection<Constraint<?>> cnstr1 = createConstraints(str1, str2);
        double distance1 = DistanceEstimator.getDistance(cnstr1);

        final String str3 = "t";
        final String str4 = "test";
        Collection<Constraint<?>> cnstr2 = createConstraints(str3, str4);
        double distance2 = DistanceEstimator.getDistance(cnstr2);

        assertTrue(distance1 > distance2);
    }

    @Test
    public void testEquals8() {
        final String str1 = "test";
        final String str2 = "est";
        Collection<Constraint<?>> cnstr1 = createConstraints(str1, str2);
        double distance1 = DistanceEstimator.getDistance(cnstr1);

        final String str3 = "test";
        final String str4 = "estx";
        Collection<Constraint<?>> cnstr2 = createConstraints(str3, str4);
        double distance2 = DistanceEstimator.getDistance(cnstr2);

        assertTrue(distance1 > distance2);
    }

    private Collection<Constraint<?>> createConstraints(final String str1,
                                                        final String str2) {
        StringVariable var1 = new StringVariable("var0", str1);
        StringConstant const1 = new StringConstant(str2);
        StringBinaryComparison comp = new StringBinaryComparison(var1,
                Operator.EQUALS, const1, 0L);
        IntegerConstant zero = new IntegerConstant(0);
        StringConstraint stringConstraint = new StringConstraint(comp,
                Comparator.NE, zero);
        Collection<Constraint<?>> cnstr = Collections
                .<Constraint<?>>singletonList(stringConstraint);
        return cnstr;
    }

}
