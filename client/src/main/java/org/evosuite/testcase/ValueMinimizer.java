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
import org.evosuite.lm.StringLMOptimizer;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * ValueMinimizer class.
 * </p>
 *
 * @author Gordon Fraser
 */
public class ValueMinimizer extends TestVisitor {

    private static final Logger logger = LoggerFactory.getLogger(ValueMinimizer.class);

    public interface Minimization {
        boolean isNotWorse();
    }

    private static class TestMinimization implements Minimization {

        private final TestFitnessFunction fitness;

        private final TestChromosome individual;

        private double lastFitness;

        public TestMinimization(TestFitnessFunction fitness, TestChromosome test) {
            this.fitness = fitness;
            this.individual = test;
            this.lastFitness = test.getFitness(fitness);
        }

        /* (non-Javadoc)
         * @see org.evosuite.ga.LocalSearchObjective#hasImproved(org.evosuite.ga.Chromosome)
         */
        @Override
        public boolean isNotWorse() {
            ExecutionResult lastResult = individual.getLastExecutionResult();
            individual.setChanged(true);
            individual.getTestCase().clearCoveredGoals();
            double newFitness = fitness.getFitness(individual);
            boolean worse = false;
            if (fitness.isMaximizationFunction()) {
                if (newFitness < lastFitness)
                    worse = true;
            } else {
                if (newFitness > lastFitness)
                    worse = true;
            }

            if (!worse) {
                lastFitness = newFitness;
                individual.setFitness(fitness, lastFitness);
                return true;
            } else {
                individual.setFitness(fitness, lastFitness);
                individual.setLastExecutionResult(lastResult);
                return false;
            }
        }
    }

    private static class SuiteMinimization implements Minimization {

        private final TestSuiteFitnessFunction fitness;

        private final TestSuiteChromosome suite;

        private final TestChromosome individual;

        private final int testIndex;

        private double lastFitness;

        private double lastCoverage;

        public SuiteMinimization(TestSuiteFitnessFunction fitness,
                                 TestSuiteChromosome suite, int index) {
            this.fitness = fitness;
            this.suite = suite;
            this.individual = suite.getTestChromosome(index);
            this.testIndex = index;
            this.lastFitness = suite.getFitness(fitness);
            this.lastCoverage = suite.getCoverage();
        }

        /* (non-Javadoc)
         * @see org.evosuite.ga.LocalSearchObjective#hasImproved(org.evosuite.ga.Chromosome)
         */
        @Override
        public boolean isNotWorse() {
            ExecutionResult lastResult = individual.getLastExecutionResult().clone();
            individual.setChanged(true);
            suite.setTestChromosome(testIndex, individual);
            double newFitness = fitness.getFitness(suite);
            // individual.setChanged(true);
            boolean worse = false;
            if (fitness.isMaximizationFunction()) {
                if (newFitness < lastFitness)
                    worse = true;
            } else {
                if (newFitness > lastFitness)
                    worse = true;
            }
            if (!worse) {
                logger.debug("Fitness changed from " + lastFitness + " to " + newFitness);
                lastFitness = newFitness;
                lastCoverage = suite.getCoverage();
                suite.setFitness(fitness, lastFitness);
                return true;
            } else {
                individual.setLastExecutionResult(lastResult);
                suite.setFitness(fitness, lastFitness);
                suite.setCoverage(fitness, lastCoverage);
                return false;
            }
        }
    }

    private Minimization objective;

    /**
     * <p>
     * minimize
     * </p>
     *
     * @param test      a {@link org.evosuite.testcase.TestChromosome} object.
     * @param objective a {@link org.evosuite.testcase.TestFitnessFunction} object.
     */
    public void minimize(TestChromosome test, TestFitnessFunction objective) {
        this.objective = new TestMinimization(objective, test);
        test.test.accept(this);
    }

    /**
     * <p>
     * minimize
     * </p>
     *
     * @param suite     a {@link org.evosuite.testsuite.TestSuiteChromosome} object.
     * @param objective a {@link org.evosuite.testsuite.TestSuiteFitnessFunction}
     *                  object.
     */
    public void minimize(TestSuiteChromosome suite, TestSuiteFitnessFunction objective) {
        int i = 0;
        objective.getFitness(suite); // Ensure all tests have an execution result cached
        for (TestChromosome test : suite.getTestChromosomes()) {
            this.objective = new SuiteMinimization(objective, suite, i);
            test.test.accept(this);
            i++;
        }

    }

    /* Generics, blargh */
    @SuppressWarnings("unchecked")
    private <N extends Number> N increment(N n, int x) {
        if (n instanceof Double) {
            return (N) (Double) (((Double) n) + x);
        } else if (n instanceof Float) {
            return (N) (Float) (((Float) n) + x);
        } else if (n instanceof Integer) {
            return (N) (Integer) (((Integer) n) + x);
        } else if (n instanceof Long) {
            return (N) (Long) (((Long) n) + x);
        } else if (n instanceof Short) {
            return (N) (Short) (short) (((Short) n) + (short) x);
        } else if (n instanceof Byte) {
            return (N) (Byte) (byte) (((Byte) n) + (byte) x);
        } else if (n == null) {
            throw new NullPointerException();
        } else {
            throw new IllegalArgumentException("Unexpected number type: " + n.getClass());
        }
    }

    /* Generics, blargh
     * TODO: Why are we not going min + max / 2 ?
     */
    @SuppressWarnings("unchecked")
    private <N extends Number> N getMid(N min, N max) {
        if (min instanceof Double) {
            return (N) (Double) (((Double) min) + (((Double) max - (Double) min) / 2.0));
        } else if (min instanceof Float) {
            return (N) (Float) (((Float) min) + (((Float) max - (Float) min) / 2F));
        } else if (min instanceof Integer) {
            return (N) (Integer) (((Integer) min) + (((Integer) max - (Integer) min) / 2));
        } else if (min instanceof Long) {
            return (N) (Long) (((Long) min) + (((Long) max - (Long) min) / 2L));
        } else if (min instanceof Short) {
            return (N) (Short) (short) (((Short) min) + (((Short) max - (Short) min) / (short) 2));
        } else if (min instanceof Byte) {
            return (N) (Byte) (byte) (((Byte) min) + (((Byte) max - (Byte) min) / (byte) 2));
        } else if (min == null) {
            throw new NullPointerException();
        } else {
            throw new IllegalArgumentException("Unexpected number type: " + min.getClass());
        }
    }

    /* Generics, blargh
     */
    @SuppressWarnings("unchecked")
    private <N extends Number> N getZero(N n) {
        if (n instanceof Double) {
            return (N) (Double.valueOf(0.0));
        } else if (n instanceof Float) {
            return (N) (Float.valueOf(0F));
        } else if (n instanceof Integer) {
            return (N) (Integer.valueOf(0));
        } else if (n instanceof Long) {
            return (N) Long.valueOf(0L);
        } else if (n instanceof Short) {
            return (N) Short.valueOf((short) 0);
        } else if (n instanceof Byte) {
            return (N) Byte.valueOf((byte) 0);
        } else if (n == null) {
            throw new NullPointerException();
        } else {
            throw new IllegalArgumentException("Unexpected number type: " + n.getClass());
        }
    }

    private <T extends Number> void binarySearch(ConstantValue constantValue, T number) {

        T min = getZero(number);
        T max = number;

        boolean positive = number.doubleValue() >= 0.0;
        T lastValue = null;
        boolean done = false;
        while (!done) {
            Object oldValue = constantValue.getValue();
            constantValue.setValue(getMid(min, max));
            T newValue = (T) constantValue.getValue();
            if (oldValue.equals(newValue)) {
                break;
            }
            if (lastValue != null && lastValue.equals(newValue)) {
                break;
            }
            if (lastValue instanceof Double) {
                double oldVal = Math.abs((Double) lastValue);
                if (oldVal < 1.0) {
                    newValue = (T) new Double(0.0);
                    constantValue.setValue(newValue);
                    if (!objective.isNotWorse()) {
                        constantValue.setValue(lastValue);
                    }
                    break;
                }
            }
            if (lastValue instanceof Float) {
                double oldVal = Math.abs((Float) lastValue);
                if (oldVal < 1.0F) {
                    newValue = (T) new Float(0.0F);
                    constantValue.setValue(newValue);
                    if (!objective.isNotWorse()) {
                        constantValue.setValue(lastValue);
                    }
                    break;
                }
            }

            lastValue = newValue;
            logger.info("Trying " + constantValue.getValue() + " " + min + "/" + max + " - "
                    + constantValue.getSimpleClassName());

            if (min.equals(max) || constantValue.getValue().equals(min)
                    || constantValue.getValue().equals(max)) {
                done = true;
                logger.info("Fixpoint.");
                //assert (objective.isNotWorse());
            }
            if (objective.isNotWorse()) {
                logger.info("Fitness hasn't decreased");
                // If fitness has not decreased, new max is new value
                max = (T) constantValue.getValue();
            } else {
                logger.info("Fitness has decreased!");
                // Else has to be larger
                if (positive) {
                    min = increment((T) constantValue.getValue(), 1);
                } else {
                    min = increment((T) constantValue.getValue(), -1);
                }
                constantValue.setValue(max);
                System.out.println("Setting value back to " + max);
                constantValue.getTestCase().clearCoveredGoals();
            }
        }
        constantValue.getTestCase().clearCoveredGoals();

    }

    /**
     * Shorten the string as much as possible, until the objective value is affected.
     *
     * @param constantValue StringPrimitiveStatement containing a string to be minimised.
     */
    private void removeCharacters(ConstantValue constantValue) {

        assert (constantValue.getValue() instanceof String);

        String oldString = (String) constantValue.getValue();

        for (int i = oldString.length() - 1; i >= 0; i--) {
            String newString = oldString.substring(0, i) + oldString.substring(i + 1);
            constantValue.setValue(newString);
            //logger.info(" " + i + " " + oldValue + "/" + oldValue.length() + " -> "
            //        + newString + "/" + newString.length());
            if (objective.isNotWorse()) {
                oldString = (String) constantValue.getValue();
            } else {
                constantValue.setValue(oldString);
            }
        }
    }

    /**
     * Try to remove non-ASCII characters
     * <p/>
     * Try to shorten the string.
     * Performs several transformations on the string:
     * 1. Strip ASCII and control characters
     * 2. Strip any non-alphanumerics
     * <p/>
     * If any transformation negatively impacts the objective function value, then
     * the transformation is reversed and the next one tried.
     */
    private void cleanString(ConstantValue constantValue) {
        assert (constantValue.getValue() instanceof String);

        String oldString = (String) constantValue.getValue();
        String newString = oldString.replaceAll("[^\\p{ASCII}]", "").replaceAll("\\p{Cntrl}",
                "");
        constantValue.setValue(newString);
        if (!objective.isNotWorse()) {
            constantValue.setValue(oldString);
            newString = oldString;
        }

        oldString = newString;
        newString = newString.replaceAll("[^\\p{L}\\p{N}]", "");
        constantValue.setValue(newString);
        if (!objective.isNotWorse()) {
            constantValue.setValue(oldString);
        }
    }

    /**
     * Attempt to use the language model to improve the string constant.
     * If a better string is found that doesn't negatively impact the fitness value,
     * statement will be overwritten to use the new improved value.
     *
     * @param constantValue
     */
    private void replaceWithLanguageModel(ConstantValue constantValue) {
        assert (constantValue.getValue() instanceof String);

        String oldString = (String) constantValue.getValue();
        StringLMOptimizer slmo = new StringLMOptimizer(constantValue, objective);
        String newString = slmo.optimize();
        constantValue.setValue(newString);
        if (!objective.isNotWorse()) {
            constantValue.setValue(oldString);
        }
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitTestCase(org.evosuite.testcase.TestCase)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitTestCase(TestCase test) {
    }

    @Override
    public void visitStatement(Statement statement) {
        for (VariableReference var : statement.getVariableReferences()) {
            if (var instanceof ConstantValue) {
                ConstantValue constantValue = (ConstantValue) var;
                Object value = constantValue.getValue();
                if (value instanceof String) {
                    logger.info("Statement before minimization: " + statement.getCode());

                    cleanString(constantValue);
                    removeCharacters(constantValue);

                    if (Properties.LM_STRINGS) {
                        replaceWithLanguageModel(constantValue);
                    }
                    logger.info("Statement after minimization: " + statement.getCode());
                    // TODO: Try to delete characters, or at least replace non-ascii characters with ascii characters

                } else if (value instanceof Number) {
                    logger.info("Statement before minimization: " + statement.getCode());
                    binarySearch(constantValue, (Number) constantValue.getValue());
                    logger.info("Statement after minimization: " + statement.getCode());

                }
            }
        }
    }


    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitPrimitiveStatement(org.evosuite.testcase.PrimitiveStatement)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitPrimitiveStatement(PrimitiveStatement<?> statement) {
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitFieldStatement(org.evosuite.testcase.FieldStatement)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitFieldStatement(FieldStatement statement) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitMethodStatement(org.evosuite.testcase.MethodStatement)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitMethodStatement(MethodStatement statement) {

    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitConstructorStatement(org.evosuite.testcase.ConstructorStatement)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitConstructorStatement(ConstructorStatement statement) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitArrayStatement(org.evosuite.testcase.ArrayStatement)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitArrayStatement(ArrayStatement statement) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitAssignmentStatement(org.evosuite.testcase.AssignmentStatement)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitAssignmentStatement(AssignmentStatement statement) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestVisitor#visitNullStatement(org.evosuite.testcase.NullStatement)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitNullStatement(NullStatement statement) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitPrimitiveExpression(PrimitiveExpression primitiveExpression) {
        // TODO-JRO Implement method visitPrimitiveExpression
        logger.warn("Method visitPrimitiveExpression not implemented!");

    }

    @Override
    public void visitFunctionalMockStatement(FunctionalMockStatement functionalMockStatement) {

    }

}
