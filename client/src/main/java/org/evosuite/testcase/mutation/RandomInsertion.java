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
import org.evosuite.testcase.statements.FunctionalMockStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.variable.NullReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.ListUtil;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * An insertion strategy that allows for modification of test cases by inserting random statements.
 */
public class RandomInsertion implements InsertionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(RandomInsertion.class);

    @Override
    public int insertStatement(TestCase test, int lastPosition) {
        double r = Randomness.nextDouble();
        int oldSize = test.size();

		/*
			TODO: if allow inserting a UUT method in the middle of a test,
			 we need to handle case of not breaking any initializing bounded variable
		 */

        int position = 0;

        assert Properties.INSERTION_UUT + Properties.INSERTION_ENVIRONMENT + Properties.INSERTION_PARAMETER == 1.0;

        // Whether to insert a call on the unit under test at the end of the given test case.
        final boolean insertUUT = Properties.INSERTION_UUT > 0
                && r <= Properties.INSERTION_UUT
                && TestCluster.getInstance().getNumTestCalls() > 0;

        /*
         * Whether to insert a call on the environment of the unit under test at the end of the
         * given test case. The environment of a test case are external resources for the test case
         * such as handles to files on the file system, sockets that open network connections, etc.
         */
        final boolean insertEnv = !insertUUT
                && Properties.INSERTION_ENVIRONMENT > 0
                && r > Properties.INSERTION_UUT && r <= Properties.INSERTION_UUT + Properties.INSERTION_ENVIRONMENT
                && TestCluster.getInstance().getNumOfEnvironmentCalls() > 0;

        boolean insertParam = !insertUUT && !insertEnv;

        boolean success = false;
        if (insertUUT) {
            // Insert a call to the UUT at the end
            position = test.size();
            success = TestFactory.getInstance().insertRandomCall(test, lastPosition + 1);
        } else if (insertEnv) {
			/*
				Insert a call to the environment, i.e., external resources for the test case such
				as handles to files on the file system, sockets that open network connections, etc.
				As such call is likely to depend on many constraints, we do not specify here the
				position of where it ll happen.
			 */
            position = TestFactory.getInstance().insertRandomCallOnEnvironment(test, lastPosition);
            success = (position >= 0);
        } else if (insertParam) {
            // Insert a call to a variable (one that is used as a parameter for some function call
            // in the test case). The idea is to mutate the parameter so that new program states
            // can be reached in the function call.
            VariableReference var = selectRandomVariableForCall(test, lastPosition);
            if (var != null) {
                // find the last position where the selected variable is used in the test case
                final int lastUsage = test.getReferences(var).stream()
                        .mapToInt(VariableReference::getStPosition)
                        .max().orElse(var.getStPosition());

                if (lastUsage > var.getStPosition() + 1) {
                    // If there is more than 1 statement where it is used, we randomly choose a position
                    position = Randomness.nextInt(var.getStPosition() + 1, // call has to be after the object is created
                            lastUsage                // but before the last usage
                    );
                } else if (lastUsage == var.getStPosition()) {
                    // The variable isn't used
                    position = lastUsage + 1;
                } else {
                    // The variable is used at only one position, we insert at exactly that position
                    position = lastUsage;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Inserting call at position " + position + ", chosen var: "
                            + var.getName() + ", distance: " + var.getDistance() + ", class: "
                            + var.getClassName());
                }

                success = TestFactory.getInstance().insertRandomCallOnObjectAt(test, var, position);
            }

            if (!success && TestCluster.getInstance().getNumTestCalls() > 0) {
                logger.debug("Adding new call on UUT because var was null");
                //Why was it different from UUT insertion? ie, in random position instead of last
                //position = Randomness.nextInt(max);
                position = test.size();
                success = TestFactory.getInstance().insertRandomCall(test, position);
            }
        }

        // This can happen if insertion had side effect of adding further previous statements in the
        // test, e.g., to handle input parameters.
        if (test.size() - oldSize > 1) {
            position += (test.size() - oldSize - 1);
        }

        if (success) {
            return position;
        } else {
            return -1;
        }
    }

    /**
     * In the given test case {@code test}, returns a random variable up to the specified {@code
     * position} for a subsequent call. If the test case is empty or the position is {@code 0},
     * {@code null} is returned.
     *
     * @param test     the test case from which to select the variable
     * @param position the position in the test case up to which a variable shoulb be selected
     * @return the selected variable or {@code null} (see above)
     */
    private VariableReference selectRandomVariableForCall(TestCase test, int position) {
        if (test.isEmpty() || position == 0)
            return null;

        List<VariableReference> allVariables = test.getObjects(position);
        List<VariableReference> candidateVariables = new ArrayList<>();

        for (VariableReference var : allVariables) {

            if (!(var instanceof NullReference) &&
                    !var.isVoid() &&
                    !var.getGenericClass().isObject() &&
                    !(test.getStatement(var.getStPosition()) instanceof PrimitiveStatement) &&
                    !var.isPrimitive() &&
                    !var.isWrapperType() &&
                    !var.isString() &&
                    (test.hasReferences(var) || var.getVariableClass().equals(Properties.getInitializedTargetClass())) &&
					/* Note: this check has been added only recently,
						to avoid having added calls to UUT in the middle of the test
					 */
					/*
					   Commented this out again, as it would mean that methods of the SUT class
					   that are declared in a superclass would not be inserted at all, but now
					   this may break some constraints.
					 */
//					!var.getVariableClass().equals(Properties.getTargetClass()) &&
                    //do not directly call methods on mock objects
                    !(test.getStatement(var.getStPosition()) instanceof FunctionalMockStatement)) {

                candidateVariables.add(var);
            }
        }

        if (candidateVariables.isEmpty()) {
            return null;
        } else if (Properties.SORT_OBJECTS) {
            candidateVariables = candidateVariables.stream()
                    .sorted(Comparator.comparingInt(VariableReference::getDistance))
                    .collect(toList());
            return ListUtil.selectRankBiased(candidateVariables);
        } else {
            return Randomness.choice(candidateVariables);
        }
    }

}
