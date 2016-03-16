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
package org.evosuite.ga;

import org.evosuite.Properties;
import org.evosuite.Properties.Algorithm;
import org.evosuite.coverage.branch.OnlyBranchCoverageSuiteFitness;
import org.evosuite.coverage.exception.ExceptionCoverageSuiteFitness;
import org.evosuite.coverage.method.MethodNoExceptionCoverageSuiteFitness;
import org.evosuite.coverage.io.output.OutputCoverageSuiteFitness;
import org.evosuite.coverage.line.LineCoverageSuiteFitness;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Jose Miguel Rojas
 */
public class ChromosomeTest {

    private final double ANY_DOUBLE_1 = 2.0;
    private final double ANY_DOUBLE_2 = 5.0;
    private final double ANY_DOUBLE_3 = 6.0;
    private final double ANY_DOUBLE_4 = 3.0;

    private final double ANY_DOUBLE_BETWEEN_0_AND_1_1 = 0.2;
    private final double ANY_DOUBLE_BETWEEN_0_AND_1_2 = 0.6;

    @Test
    public void testGetFitnessForOneFunctionNoCompositional() {
        Properties.ALGORITHM = Algorithm.MONOTONICGA;
        TestSuiteChromosome c = new TestSuiteChromosome();
        c.addFitness(new LineCoverageSuiteFitness(), ANY_DOUBLE_1);
        assertEquals(ANY_DOUBLE_1, c.getFitness(), 0.001);
    }

    @Test
    public void testGetFitnessForNoFunctionNoCompositional() {
        Properties.ALGORITHM = Algorithm.MONOTONICGA;
        TestSuiteChromosome c = new TestSuiteChromosome();
        assertEquals(0.0, c.getFitness(), 0.001);
    }

    @Test
    public void testCompositionalGetFitnessForNoFunction() {
        Properties.ALGORITHM = Algorithm.MONOTONICGA;
        TestSuiteChromosome c = new TestSuiteChromosome();
        assertEquals(0.0, c.getFitness(), 0.001);
    }

    @Test
    public void testCompositionalGetFitnessForOneFunction() {
        Properties.ALGORITHM = Algorithm.MONOTONICGA;
        TestSuiteChromosome c = new TestSuiteChromosome();
        LineCoverageSuiteFitness f1 = new LineCoverageSuiteFitness();
        c.addFitness(f1);
        c.setFitness(f1, ANY_DOUBLE_1);
        assertEquals(ANY_DOUBLE_1, c.getFitness(), 0.001);
    }

    @Test
    public void testCompositionalGetFitnessForTwoFunctions() {
        Properties.ALGORITHM = Algorithm.MONOTONICGA;
        TestSuiteChromosome c = new TestSuiteChromosome();
        LineCoverageSuiteFitness f1 = new LineCoverageSuiteFitness();
        OnlyBranchCoverageSuiteFitness f2 = new OnlyBranchCoverageSuiteFitness();
        c.addFitness(f1);
        c.addFitness(f2);
        c.setFitness(f1, ANY_DOUBLE_1);
        c.setFitness(f2, ANY_DOUBLE_2);
        c.setCoverage(f1, ANY_DOUBLE_BETWEEN_0_AND_1_1);
        c.setCoverage(f2, ANY_DOUBLE_BETWEEN_0_AND_1_2);
        assertEquals(ANY_DOUBLE_1, c.getFitnessInstanceOf(LineCoverageSuiteFitness.class), 0.001);
        assertEquals(ANY_DOUBLE_2, c.getFitnessInstanceOf(OnlyBranchCoverageSuiteFitness.class), 0.001);
        assertEquals(ANY_DOUBLE_BETWEEN_0_AND_1_1, c.getCoverageInstanceOf(LineCoverageSuiteFitness.class), 0.001);
        assertEquals(ANY_DOUBLE_BETWEEN_0_AND_1_2, c.getCoverageInstanceOf(OnlyBranchCoverageSuiteFitness.class), 0.001);
        assertEquals(ANY_DOUBLE_1 + ANY_DOUBLE_2, c.getFitness(), 0.001);
        assertEquals((ANY_DOUBLE_BETWEEN_0_AND_1_1 + ANY_DOUBLE_BETWEEN_0_AND_1_2) / 2, c.getCoverage(), 0.001);
    }

    @Test
    public void testCompositionalGetFitnessForSeveralFunctions() {
        Properties.ALGORITHM = Algorithm.MONOTONICGA;
        TestSuiteChromosome c = new TestSuiteChromosome();
        LineCoverageSuiteFitness f1 = new LineCoverageSuiteFitness();
        c.addFitness(f1);
        c.setFitness(f1, ANY_DOUBLE_1);
        MethodNoExceptionCoverageSuiteFitness f2 = new MethodNoExceptionCoverageSuiteFitness();
        c.addFitness(f2);
        c.setFitness(f2, ANY_DOUBLE_2);
        OutputCoverageSuiteFitness f3 = new OutputCoverageSuiteFitness();
        c.addFitness(f3);
        c.setFitness(f3, ANY_DOUBLE_3);
        ExceptionCoverageSuiteFitness f4 = new ExceptionCoverageSuiteFitness();
        c.addFitness(f4);
        c.setFitness(f4, ANY_DOUBLE_4);
        double sum = ANY_DOUBLE_1 + ANY_DOUBLE_2 + ANY_DOUBLE_3 + ANY_DOUBLE_4;
        assertEquals(sum, c.getFitness(), 0.001);
    }
}
