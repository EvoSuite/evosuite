/**
 * Copyright (C) 2010-2020 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.symbolic.DSE;

import org.evosuite.symbolic.DSE.algorithm.DSEPathCondition;
import org.evosuite.testcase.TestCase;

import java.util.Objects;

/**
 * A DSE test case also contains a score and an original PathCondition.
 * Note: it's not extending TestCase to avoid coupling with current code.
 *
 * @author Ignacio Lebrero
 */
public class DSETestCase implements Comparable<DSETestCase>, Cloneable {

    /**
     * A priority score based on any sort of metric.
     *
     * e.g.: Godefroid P., Levin Y. M. & Molnar D. (2008) Automated White Box Fuzz Testing, pg. 5.
     */
    private double score;

    /**
     * Test case in question
     */
    private TestCase testCase;

    /**
     * Path condition from wich the test case was created.
     * Useful for checking path divergences.
     */
    private DSEPathCondition originalPathCondition;

    public DSETestCase(TestCase testCase, DSEPathCondition originalPathCondition, double score) {
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

    public DSEPathCondition getOriginalPathCondition() {
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
                .append("DSETestCase{")
                .append("score=")
                .append(score)
                .append(", testCase=")
                .append(testCase)
                .append(", originalPathCondition=")
                .append(originalPathCondition)
                .append('}').toString();
    }
}
