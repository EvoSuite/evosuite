/**
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
package org.evosuite.coverage.io.input;

import static org.evosuite.coverage.io.IOCoverageConstants.CHAR_ALPHA;
import static org.evosuite.coverage.io.IOCoverageConstants.CHAR_DIGIT;
import static org.evosuite.coverage.io.IOCoverageConstants.CHAR_OTHER;
import static org.evosuite.coverage.io.IOCoverageConstants.NUM_NEGATIVE;
import static org.evosuite.coverage.io.IOCoverageConstants.NUM_POSITIVE;
import static org.evosuite.coverage.io.IOCoverageConstants.NUM_ZERO;
import java.util.Set;
import org.evosuite.Properties;
import org.evosuite.ga.archive.Archive;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.objectweb.asm.Type;

/**
 * @author Jose Miguel Rojas
 */
public class InputCoverageTestFitness extends TestFitnessFunction {

    private static final long serialVersionUID = 6630097528288524492L;

    /**
     * Target goal
     */
    private final InputCoverageGoal goal;

    /**
     * Constructor - fitness is specific to a method
     *
     * @param goal the coverage goal
     * @throws IllegalArgumentException
     */
    public InputCoverageTestFitness(InputCoverageGoal goal) throws IllegalArgumentException {
        if (goal == null) {
            throw new IllegalArgumentException("goal cannot be null");
        }
        this.goal = goal;
        // add the observer to TestCaseExecutor if it is not included yet
        boolean hasObserver = false;
        TestCaseExecutor executor = TestCaseExecutor.getInstance();
        for (ExecutionObserver ob : executor.getExecutionObservers()){
        	if (ob instanceof  InputObserver){
        		hasObserver = true;
        		break;
        	}
        }
        if (!hasObserver){
        	InputObserver observer = new InputObserver();
			executor.addObserver(observer);
			logger.info("Added observer for input coverage");
        }
    }

    /**
     * <p>
     * getClassName
     * </p>
     *
     * @return a {@link String} object.
     */
    public String getClassName() {
        return goal.getClassName();
    }

    /**
     * <p>
     * getMethod
     * </p>
     *
     * @return a {@link String} object.
     */
    public String getMethod() {
        return goal.getMethodName();
    }

    /**
     * <p>
     * getValue
     * </p>
     *
     * @return a {@link String} object.
     */
    public Type getType() {
        return goal.getType();
    }

    /**
     * <p>
     * getValue
     * </p>
     *
     * @return a {@link String} object.
     */
    public String getValueDescriptor() {
        return goal.getValueDescriptor();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Calculate fitness
     *
     * @param individual a {@link org.evosuite.testcase.ExecutableChromosome} object.
     * @param result     a {@link ExecutionResult} object.
     * @return a double.
     */
    @Override
    public double getFitness(TestChromosome individual, ExecutionResult result) {
        double fitness = 1.0;

        for(Set<InputCoverageGoal> coveredGoals : result.getInputGoals().values()) {
            if (!coveredGoals.contains(this.goal)) {
                continue;
            }

            for (InputCoverageGoal coveredGoal : coveredGoals) {
                if (coveredGoal.equals(this.goal)) {
                    double distance = this.calculateDistance(coveredGoal);
                    if (distance < 0.0) {
                        continue;
                    } else {
                        fitness = distance;
                        break;
                    }
                }
            }
        }

        assert fitness >= 0.0;
        updateIndividual(this, individual, fitness);

        if (fitness == 0.0) {
            individual.getTestCase().addCoveredGoal(this);
        }

        if (Properties.TEST_ARCHIVE) {
            Archive.getArchiveInstance().updateArchive(this, individual, fitness);
        }

        return fitness;
    }

    private double calculateDistance(InputCoverageGoal coveredGoal) {
      Number argValue = coveredGoal.getNumericValue();
      switch (coveredGoal.getType().getSort()) {
          case Type.BYTE:
          case Type.SHORT:
          case Type.INT:
          case Type.FLOAT:
          case Type.LONG:
          case Type.DOUBLE:
              assert (argValue != null);
              assert (argValue instanceof Number);
              // TODO: ideally we should be able to tell between Number as an object, and primitive numeric types
              double doubleValue = ((Number) argValue).doubleValue();
              if (Double.isNaN(doubleValue)) { // EvoSuite generates Double.NaN
                  return -1;
              }

              double distanceToNegative = 0.0;
              double distanceToZero = 0.0;
              double distanceToPositive = 0.0;

              if (doubleValue < 0) {
                  distanceToNegative = 0;
                  distanceToZero = Math.abs(doubleValue);
                  distanceToPositive = Math.abs(doubleValue) + 1;
              } else if (doubleValue == 0) {
                  distanceToNegative = 1;
                  distanceToZero = 0;
                  distanceToPositive = 1;
              } else {
                  distanceToNegative = doubleValue + 1;
                  distanceToZero = doubleValue;
                  distanceToPositive = 0;
              }

              if (coveredGoal.getValueDescriptor().equals(NUM_NEGATIVE)) {
                  return distanceToNegative;
              } else if (coveredGoal.getValueDescriptor().equals(NUM_ZERO)) {
                  return distanceToZero;
              } else if (coveredGoal.getValueDescriptor().equals(NUM_POSITIVE)) {
                  return distanceToPositive;
              }

              break;
          case Type.CHAR:
              char charValue = (char)((Number) argValue).intValue();

              double distanceToAlpha = 0.0;
              if (charValue < 'A') {
                  distanceToAlpha = 'A' - charValue;
              } else if (charValue > 'z') {
                  distanceToAlpha = charValue - 'z';
              } else if (charValue < 'a' && charValue > 'Z') {
                  distanceToAlpha = Math.min('a' - charValue, charValue - 'Z');
              }

              double distanceToDigit = 0.0;
              if (charValue < '0') {
                  distanceToDigit = '0' - charValue;
              } else if(charValue > '9') {
                  distanceToDigit = charValue - '9';
              }

              double distanceToOther = 0.0; // TODO distanceToOther is never used!
              if (charValue > '0' && charValue < '9') {
                  distanceToAlpha = Math.min(charValue - '0', '9' - charValue);
              } else if (charValue > 'A' && charValue < 'Z') {
                  distanceToAlpha = Math.min(charValue - 'A', 'Z' - charValue);
              } else if (charValue > 'a' && charValue < 'z') {
                  distanceToAlpha = Math.min(charValue - 'A', 'Z' - charValue);
              }

              if (coveredGoal.getValueDescriptor().equals(CHAR_ALPHA)) {
                  return distanceToAlpha;
              } else if (coveredGoal.getValueDescriptor().equals(CHAR_DIGIT)) {
                  return distanceToDigit;
              } else if (coveredGoal.getValueDescriptor().equals(CHAR_OTHER)) {
                  return distanceToOther;
              }

              break;
          default:
              return 0.0;
      }

      return 0.0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[Input]: "+goal.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int iConst = 13;
        return 51 * iConst + goal.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InputCoverageTestFitness other = (InputCoverageTestFitness) obj;
        return this.goal.equals(other.goal);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#compareTo(org.evosuite.testcase.TestFitnessFunction)
     */
    @Override
    public int compareTo(TestFitnessFunction other) {
        if (other instanceof InputCoverageTestFitness) {
            InputCoverageTestFitness otherInputFitness = (InputCoverageTestFitness) other;
            return goal.compareTo(otherInputFitness.goal);
        }
        return compareClassName(other);
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetClass()
     */
    @Override
    public String getTargetClass() {
        return getClassName();
    }

    /* (non-Javadoc)
     * @see org.evosuite.testcase.TestFitnessFunction#getTargetMethod()
     */
    @Override
    public String getTargetMethod() {
        return getMethod();
    }

}
