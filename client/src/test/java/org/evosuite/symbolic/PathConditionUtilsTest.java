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
package org.evosuite.symbolic;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.constraint.IntegerConstraint;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class PathConditionUtilsTest {

    private static final String TEST_CLASS_NAME = "test_class";
    private static final String TEST_METHOD_NAME = "test_method";

    @Test
    public void isConstraintSetSubSetOfNullArguments() {
        IllegalArgumentException exQueryNull = null;
        IllegalArgumentException exQueriesNull = null;

        try {
            PathConditionUtils.isConstraintSetSubSetOf(null, null);
        } catch (IllegalArgumentException e) {
            exQueryNull = e;
        }

        try {
            PathConditionUtils.isConstraintSetSubSetOf(new HashSet(), null);
        } catch (IllegalArgumentException e) {
            exQueriesNull = e;
        }

        assertNotNull(exQueryNull);
        assertEquals(PathConditionUtils.QUERY_CANNOT_BE_NULL_EXCEPTION_MESSAGE, exQueryNull.getMessage());

        assertNotNull(exQueriesNull);
        assertEquals(PathConditionUtils.QUERIES_CANNOT_BE_NULL_EXCEPTION_MESSAGE, exQueriesNull.getMessage());
    }

    @Test
    public void isConstraintSetSubSetOfIsSubset() {
        Set<Constraint<?>> query = new HashSet();
        List<Set<Constraint<?>>> queries = new ArrayList();

        query.add(buildConstraintA());

        queries.add(query);

        boolean result = PathConditionUtils.isConstraintSetSubSetOf(query, queries);
        assertTrue(result);
    }

    @Test
    public void isConstraintSetSubSetOfIsNotSubset() {
        Set<Constraint<?>> queryA = new HashSet();
        Set<Constraint<?>> queryB = new HashSet();
        Set<Constraint<?>> queryC = new HashSet();
        List<Set<Constraint<?>>> queries = new ArrayList();

        queryA.add(
                new IntegerConstraint(
                        new IntegerConstant(12),
                        Comparator.EQ,
                        new IntegerConstant(0)
                )
        );

        queryB.add(
                new IntegerConstraint(
                        new IntegerConstant(12),
                        Comparator.LT,
                        new IntegerConstant(0)
                )
        );

        queryB.add(
                new IntegerConstraint(
                        new IntegerConstant(39),
                        Comparator.GE,
                        new IntegerConstant(89)
                )
        );

        queryC.add(
                new IntegerConstraint(
                        new IntegerConstant(1),
                        Comparator.NE,
                        new IntegerConstant(8)
                )
        );

        queries.add(queryB);
        queries.add(queryC);

        boolean result = PathConditionUtils.isConstraintSetSubSetOf(queryA, queries);
        assertFalse(result);
    }

    @Test
    public void hasPathConditionDivergeedNullArguments() {
        IllegalArgumentException newPath = null;
        IllegalArgumentException expectedPathPrefix = null;

        try {
            PathConditionUtils.hasPathConditionDiverged(
                    null,
                    buildPathConditionA());
        } catch (IllegalArgumentException e) {
            expectedPathPrefix = e;
        }

        try {
            PathConditionUtils.hasPathConditionDiverged(
                    buildPathConditionA(),
                    null);
        } catch (IllegalArgumentException e) {
            newPath = e;
        }

        assertNotNull(newPath);
        assertEquals(PathConditionUtils.NEW_PATH_CONDITION_CANNOT_BE_NULL, newPath.getMessage());

        assertNotNull(expectedPathPrefix);
        assertEquals(PathConditionUtils.EXPECTED_PREFIX_PATH_CONDITION_CANNOT_BE_NULL, expectedPathPrefix.getMessage());
    }

    @Test
    public void hasPathConditionDivergedHasDiverged() {
        PathCondition newPath = buildPathConditionB();
        PathCondition originalPath = buildPathConditionA();
        PathCondition shortPathCondition = buildPathConditionB();

        boolean result;

        result = PathConditionUtils.hasPathConditionDiverged(originalPath, newPath);
        assertTrue(result);

        result = PathConditionUtils.hasPathConditionDiverged(originalPath, shortPathCondition);
        assertTrue(result);
    }

    @Test
    public void hasPathConditionDivergedHasntDiverged() {
        PathCondition newPathEqual = buildPathConditionA();
        PathCondition originalPath = buildPathConditionA();

        // Shouldn't diverge against itself
        assertFalse(
                PathConditionUtils.hasPathConditionDiverged(originalPath, newPathEqual)
        );

        // Shouldn't diverge against a prefix
        List<BranchCondition> conditions = newPathEqual.getBranchConditions();

        conditions.add(
                buildPathConditionNode(buildConstraintD(), 5)
        );

        PathCondition newPath = new PathCondition(conditions);
        assertFalse(
                PathConditionUtils.hasPathConditionDiverged(originalPath, newPath)
        );
    }

    @Test
    public void calculatePathDivergenceRatioZeroInput() {
        ArithmeticException ex = null;

        try {
            PathConditionUtils.calculatePathDivergenceRatio(0, 0);
        } catch (ArithmeticException e) {
            ex = e;
        }

        assertNotNull(ex);
        assertEquals(PathConditionUtils.TOTAL_AMOUNT_OF_PATHS_HAS_TO_BE_HIGHER_THAN_0, ex.getMessage());
    }

    @Test
    public void calculatePathDivergenceRatioExceptions() {
        ArithmeticException zeroDivisionException = null;
        IllegalArgumentException negativeValuesException = null;

        try {
            PathConditionUtils.calculatePathDivergenceRatio(0, 0);
        } catch (ArithmeticException e) {
            zeroDivisionException = e;
        }

        try {
            PathConditionUtils.calculatePathDivergenceRatio(-3, -4);
        } catch (IllegalArgumentException e) {
            negativeValuesException = e;
        }

        assertNotNull(zeroDivisionException);
        assertEquals(PathConditionUtils.TOTAL_AMOUNT_OF_PATHS_HAS_TO_BE_HIGHER_THAN_0, zeroDivisionException.getMessage());

        assertNotNull(zeroDivisionException);
        assertEquals(PathConditionUtils.PATH_AMOUNTS_CANNOT_BE_NEGATIVE, negativeValuesException.getMessage());
    }

    @Test
    public void calculatePathDivergenceRatio() {
        assertEquals(0.5d, PathConditionUtils.calculatePathDivergenceRatio(1, 2), 0.0d);
    }

    private Constraint buildConstraintA() {
        return new IntegerConstraint(
                new IntegerConstant(12),
                Comparator.EQ,
                new IntegerConstant(0)
        );
    }

    private Constraint buildConstraintB() {
        return new IntegerConstraint(
                new IntegerConstant(12),
                Comparator.LT,
                new IntegerConstant(0)
        );
    }

    private Constraint buildConstraintC() {
        return new IntegerConstraint(
                new IntegerConstant(39),
                Comparator.GE,
                new IntegerConstant(89)
        );
    }

    private Constraint buildConstraintD() {
        return new IntegerConstraint(
                new IntegerConstant(1),
                Comparator.NE,
                new IntegerConstant(8)
        );
    }

    private BranchCondition buildPathConditionNode(Constraint constraint, int instructionIndex) {
        return new BranchCondition(
                TEST_CLASS_NAME,
                TEST_METHOD_NAME,
                instructionIndex,
                constraint,
                new ArrayList()
        );
    }

    private PathCondition buildPathConditionA() {
        List<BranchCondition> pathConditionList = new ArrayList();

        pathConditionList.add(buildPathConditionNode(buildConstraintA(), 1));
        pathConditionList.add(buildPathConditionNode(buildConstraintB(), 2));

        return new PathCondition(pathConditionList);
    }

    private PathCondition buildPathConditionB() {
        List<BranchCondition> pathConditionList = new ArrayList();

        pathConditionList.add(buildPathConditionNode(buildConstraintA(), 1));
        pathConditionList.add(buildPathConditionNode(buildConstraintC(), 2));

        return new PathCondition(pathConditionList);
    }
}