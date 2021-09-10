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
package org.evosuite.symbolic.dse;

import org.evosuite.symbolic.dse.algorithm.GenerationalSearchPathCondition;
import org.evosuite.testcase.TestCase;

import java.util.Objects;

/**
 * A DSE test case also contains a score and an original PathCondition.
 *
 * @author Ignacio Lebrero
 */
public class DSETestCase implements Comparable<DSETestCase>, Cloneable {

    public static final String SCORE = "score=";
    public static final String TEST_CASE = ", testCase=";
    public static final String DSE_TEST_CASE = "DSETestCase{";
    public static final String ORIGINAL_PATH_CONDITION = ", originalPathCondition=";

    /**
     * A priority score based on any sort of metric.
     * <p>
     * e.g.: Godefroid P., Levin Y. M. & Molnar D. (2008) Automated White Box Fuzz Testing, pg. 5.
     */
    private final double score;

    /**
     * Test case
     */
    private final TestCase testCase;

    /**
     * Path condition from which the test case was created.
     * Used for checking path divergences.
     */
    private final GenerationalSearchPathCondition originalPathCondition;

    public DSETestCase(TestCase testCase, GenerationalSearchPathCondition originalPathCondition, double score) {
        this.score = score;
        this.testCase = testCase;
        this.originalPathCondition = originalPathCondition;
    }

    public double getScore() {
        return score;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public GenerationalSearchPathCondition getOriginalPathCondition() {
        return originalPathCondition;
    }

    public DSETestCase clone() {
        return new DSETestCase(
                testCase.clone(),
                originalPathCondition,
                score
        );
    }

    @Override
    public int compareTo(DSETestCase dseTestCase) {
        if (this.score < dseTestCase.score) {
            return 1;
        } else if (this.score > dseTestCase.score) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DSETestCase testCase1 = (DSETestCase) o;
        return Double.compare(testCase1.score, score) == 0 &&
                Objects.equals(testCase, testCase1.testCase) &&
                Objects.equals(originalPathCondition, testCase1.originalPathCondition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(score, testCase, originalPathCondition);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append(DSE_TEST_CASE)
                .append(SCORE)
                .append(score)
                .append(TEST_CASE)
                .append(testCase)
                .append(ORIGINAL_PATH_CONDITION)
                .append(originalPathCondition)
                .append('}').toString();
    }
}
