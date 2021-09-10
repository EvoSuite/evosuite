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
package org.evosuite.testcase.mutation;

import org.evosuite.Properties;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.variable.NullReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class LegacyInsertion implements InsertionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(LegacyInsertion.class);

    /**
     * Randomly select one of the variables in the test defined up to position
     * to insert a call for
     *
     * @param test
     * @param position
     * @return
     */
    private VariableReference selectVariableForCall(TestCase test, int position) {
        if (test.isEmpty() || position == 0)
            return null;

        double sum = 0.0;
        for (int i = 0; i < position; i++) {
            //			sum += 1d / (10 * test.getStatement(i).getReturnValue().getDistance() + 1d);
            sum += 1d / (test.getStatement(i).getReturnValue().getDistance() + 1d);
            if (logger.isDebugEnabled()) {
                logger.debug(test.getStatement(i).getCode() + ": Distance = "
                        + test.getStatement(i).getReturnValue().getDistance());
            }
        }

        double rnd = Randomness.nextDouble() * sum;

        for (int i = 0; i < position; i++) {
            double dist = 1d / (test.getStatement(i).getReturnValue().getDistance() + 1d);

            if (dist >= rnd
                    && !(test.getStatement(i).getReturnValue() instanceof NullReference)
                    && !(test.getStatement(i).getReturnValue().isPrimitive())
                    && !(test.getStatement(i).getReturnValue().isVoid())
                    && !(test.getStatement(i) instanceof PrimitiveStatement))
                return test.getStatement(i).getReturnValue();
            else
                rnd = rnd - dist;
        }

        if (position > 0)
            position = Randomness.nextInt(position);

        VariableReference var = test.getStatement(position).getReturnValue();
        if (!(var instanceof NullReference) && !var.isVoid()
                && !(test.getStatement(position) instanceof PrimitiveStatement)
                && !var.isPrimitive())
            return var;
        else
            return null;
    }

    private VariableReference selectRandomVariableForCall(TestCase test, int position) {
        if (test.isEmpty() || position == 0)
            return null;

        List<VariableReference> allVariables = test.getObjects(position);
        Set<VariableReference> candidateVariables = new LinkedHashSet<>();
        for (VariableReference var : allVariables) {
            if (!(var instanceof NullReference) &&
                    !var.isVoid() &&
                    !(test.getStatement(var.getStPosition()) instanceof PrimitiveStatement) &&
                    !var.isPrimitive())
                candidateVariables.add(var);
        }
        if (candidateVariables.isEmpty()) {
            return null;
        } else {
            VariableReference choice = Randomness.choice(candidateVariables);
            return choice;
        }
    }

    /**
     * Insert a random call at given position for an object defined before this
     * position
     *
     * @param test
     * @param position
     */
    public boolean insertRandomCallOnObject(TestCase test, int position) {
        // Select a random variable
        VariableReference var = selectVariableForCall(test, position);
//		VariableReference var = selectRandomVariableForCall(test, position);

        boolean success = false;

        // Add call for this variable at random position
        if (var != null) {
            logger.debug("Inserting call at position " + position + ", chosen var: "
                    + var.getName() + ", distance: " + var.getDistance() + ", class: "
                    + var.getClassName());
            success = TestFactory.getInstance().insertRandomCallOnObjectAt(test, var, position);
        }

        if (!success) {
            if (TestCluster.getInstance().getNumTestCalls() > 0) {
                logger.debug("Adding new call on UUT because var was null");
                success = TestFactory.getInstance().insertRandomCall(test, position);
            }
        }
        return success;
    }


    @Override
    public int insertStatement(TestCase test, int lastPosition) {
        //final double P = Properties.INSERTION_SCORE_UUT
        //        + Properties.INSERTION_SCORE_OBJECT
        //        + Properties.INSERTION_SCORE_PARAMETER;
        // final double P_UUT = Properties.INSERTION_SCORE_UUT / P;
        // final double P_OBJECT = P_UUT + Properties.INSERTION_SCORE_OBJECT / P;

        int oldSize = test.size();
        double r = Randomness.nextDouble();
        //		int position = Randomness.nextInt(test.size() + 1);
        int max = lastPosition;
        if (max == test.size())
            max += 1;

        if (max <= 0)
            max = 1;

        int position = Randomness.nextInt(max);

        if (logger.isDebugEnabled()) {
            //for (int i = 0; i < test.size(); i++) {
            //	logger.debug(test.getStatement(i).getCode() + ": Distance = "
            //	        + test.getStatement(i).getReturnValue().getDistance());
            //}
            logger.debug(test.toCode());
        }

        //		if (r <= P_UUT) {
        boolean success = false;
        if (r <= Properties.INSERTION_UUT && TestCluster.getInstance().getNumTestCalls() > 0) {
            // add new call of the UUT - only declared in UUT!
            logger.debug("Adding new call on UUT");
            success = TestFactory.getInstance().insertRandomCall(test, position);
            if (test.size() - oldSize > 1) {
                position += (test.size() - oldSize - 1);
            }
        } else { // if (r <= P_OBJECT) {
            logger.debug("Adding new call on existing object");
            success = insertRandomCallOnObject(test, position);
            if (test.size() - oldSize > 1) {
                position += (test.size() - oldSize - 1);
            }
            //		} else {
            //			logger.debug("Adding new call with existing object as parameter");
            // insertRandomCallWithObject(test, position);
        }
        if (success)
            return position;
        else
            return -1;
    }

}
