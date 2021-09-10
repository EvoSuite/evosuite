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

import org.evosuite.Properties;
import org.evosuite.TimeController;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.SecondaryObjective;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Remove all statements from a test case that do not contribute to the fitness
 *
 * @author Gordon Fraser
 */
public class TestCaseMinimizer {

    private static final Logger logger = LoggerFactory.getLogger(TestCaseMinimizer.class);

    private final TestFitnessFunction fitnessFunction;

    /**
     * Constructor
     *
     * @param fitnessFunction Fitness function with which to measure whether a statement is
     *                        necessary
     */
    public TestCaseMinimizer(TestFitnessFunction fitnessFunction) {
        this.fitnessFunction = fitnessFunction;
    }

    /**
     * Remove all unreferenced variables
     *
     * @param t The test case
     * @return True if something was deleted
     */
    public static boolean removeUnusedVariables(TestCase t) {
        List<Integer> to_delete = new ArrayList<>();
        boolean has_deleted = false;

        int num = 0;
        for (Statement s : t) {
            VariableReference var = s.getReturnValue();
            if (!t.hasReferences(var)) {
                to_delete.add(num);
                has_deleted = true;
            }
            num++;
        }
        to_delete.sort(Collections.reverseOrder());
        for (Integer position : to_delete) {
            t.remove(position);
        }

        return has_deleted;
    }

    private static boolean isWorse(FitnessFunction<TestChromosome> fitness,
                                   TestChromosome oldChromosome, TestChromosome newChromosome) {
        if (fitness.isMaximizationFunction()) {
            if (oldChromosome.getFitness(fitness) > fitness.getFitness(newChromosome))
                return true;
        } else {
            if (fitness.getFitness(newChromosome) > oldChromosome.getFitness(fitness))
                return true;
        }

        for (SecondaryObjective<TestChromosome> objective : TestChromosome.getSecondaryObjectives()) {
            if (objective.compareChromosomes(oldChromosome, newChromosome) < 0)
                return true;
        }

        return false;
    }

    private boolean isTimeoutReached() {
        return !TimeController.getInstance().isThereStillTimeInThisPhase();
    }

    /**
     * Central minimization function. Loop and try to remove until all
     * statements have been checked.
     *
     * @param c a {@link org.evosuite.testcase.TestChromosome} object.
     */
    public void minimize(TestChromosome c) {
        if (!Properties.MINIMIZE) {
            return;
        }
        logger.info("Minimizing test case");


        double fitness = fitnessFunction.getFitness(c);
        if (isTimeoutReached()) {
            return;
        }

        logger.debug("Start fitness values: {}", fitness);

        if (isTimeoutReached()) {
            logger.debug("Timeout reached after verifying test");
            return;
        }

        boolean changed = true;

        while (changed) {
            changed = false;

            for (int i = c.test.size() - 1; i >= 0; i--) {
                if (isTimeoutReached()) {
                    logger.debug("Timeout reached before minimizing statement {}", c.test.getStatement(i).getCode());
                    return;
                }

                logger.debug("Deleting statement {}", c.test.getStatement(i).getCode());
                TestChromosome copy = c.clone();
                boolean modified;
                try {
                    modified = TestFactory.getInstance().deleteStatementGracefully(c.test, i);
                } catch (ConstructionFailedException e) {
                    modified = false;
                }

                if (!modified) {
                    c.setChanged(false);
                    c.test = copy.test;
                    logger.debug("Deleting failed");
                    continue;
                }

                c.setChanged(true);

                if (isTimeoutReached()) {
                    logger.debug("Keeping original version due to timeout");
                    restoreTestCase(c, copy);
                    return;
                }

                if (!isWorse(fitnessFunction, copy, c)) {
                    logger.debug("Keeping shorter version");
                    changed = true;
                    break;
                } else {
                    logger.debug("Keeping original version");
                    restoreTestCase(c, copy);
                }

            }
        }

        //TODO: add back this check
        assert (fitnessFunction.isMaximizationFunction() ?
                fitnessFunction.getFitness(c) >= fitness : fitnessFunction.getFitness(c) <= fitness)
                :
                "Minimization worsened " + fitnessFunction.getClass().getName() + " fitness from " + fitness +
                        " to " + fitnessFunction.getFitness(c) + " on test " + c.getTestCase().toCode();


        if (Properties.MINIMIZE_VALUES) {
            logger.info("Minimizing values of test case");
            ValueMinimizer minimizer = new ValueMinimizer();
            minimizer.minimize(c, fitnessFunction);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Minimized test case: ");
            logger.debug(c.test.toCode());
        }

    }

    private static void restoreTestCase(TestChromosome c, TestChromosome copy) {
        c.test = copy.test;
        c.copyCachedResults(copy);
        //c.setFitness(copy.getFitness());
        c.setFitnessValues(copy.getFitnessValues());
        c.setPreviousFitnessValues(copy.getPreviousFitnessValues());
        c.setChanged(false);
    }

}
