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
package org.evosuite.testcase.mutation;

import java.util.*;
import java.util.stream.Collectors;

import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.ConstraintHelper;
import org.evosuite.testcase.ConstraintVerifier;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.statements.FunctionalMockStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.variable.NullReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.ListUtil;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericAccessibleObject;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		int max = lastPosition;
		if (max == test.size())
			max += 1;

		if (max <= 0)
			max = 1;

		int position = 0;

		assert Properties.INSERTION_UUT + Properties.INSERTION_ENVIRONMENT + Properties.INSERTION_PARAMETER == 1.0;

		boolean insertUUT = Properties.INSERTION_UUT > 0 &&
				r <= Properties.INSERTION_UUT && TestCluster.getInstance().getNumTestCalls() > 0 ;

		boolean insertEnv = !insertUUT && Properties.INSERTION_ENVIRONMENT > 0 &&
				r > Properties.INSERTION_UUT && r <= Properties.INSERTION_UUT+Properties.INSERTION_ENVIRONMENT &&
				TestCluster.getInstance().getNumOfEnvironmentCalls() > 0;

		boolean insertParam = !insertUUT && !insertEnv;

		boolean success = false;
		if (insertUUT) {
			// Insert a call to the UUT at the end
			position = test.size();
			success = TestFactory.getInstance().insertRandomCall(test, lastPosition + 1);
		} else if (insertEnv) {
			/*
				Insert a call to the environment. As such call is likely to depend on many constraints,
				we do not specify here the position of where it ll happen.
			 */
			position = TestFactory.getInstance().insertRandomCallOnEnvironment(test,lastPosition);
			success = (position >= 0);
		} else if (insertParam){
			// Insert a call to a parameter
			VariableReference var = selectRandomVariableForCall(test, lastPosition);
			if (var != null) {
				int lastUsage = var.getStPosition();

				for (VariableReference usage : test.getReferences(var)) {
					if (usage.getStPosition() > lastUsage)
						lastUsage = usage.getStPosition();
				}

				int boundPosition = ConstraintHelper.getLastPositionOfBounded(var, test);
				if(boundPosition >= 0 ){
					// if bounded variable, cannot add methods before its initialization
					position = boundPosition + 1;
				} else {

					if (lastUsage > var.getStPosition() + 1) {
						// If there is more than 1 statement where it is used, we randomly choose a position
						position = Randomness.nextInt(var.getStPosition() + 1, // call has to be after the object is created
								lastUsage                // but before the last usage
						);
					} else if(lastUsage == var.getStPosition()) {
						// The variable isn't used
						position = lastUsage + 1;
					} else {
						// The variable is used at only one position, we insert at exactly that position
						position = lastUsage;
					}
				}

				if(logger.isDebugEnabled()) {
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

		//this can happen if insertion had side effect of adding further previous statements in the test,
		//eg to handle input parameters
		if (test.size() - oldSize > 1) {
			position += (test.size() - oldSize - 1);
		}

		if (success) {
			assert ConstraintVerifier.verifyTest(test);
			assert ! ConstraintVerifier.hasAnyOnlyForAssertionMethod(test);

			return position;
		} else {
			return -1;
		}
	}
	
	private VariableReference selectRandomVariableForCall(TestCase test, int position) {
		if (test.isEmpty() || position == 0)
			return null;

		List<VariableReference> allVariables = test.getObjects(position);
		List<VariableReference> candidateVariables = new ArrayList<>();

		for(VariableReference var : allVariables) {

			if (!(var instanceof NullReference) &&
					!var.isVoid() &&
					!var.getGenericClass().isObject() &&
					!(test.getStatement(var.getStPosition()) instanceof PrimitiveStatement) &&
					!var.isPrimitive() &&
					(test.hasReferences(var) || var.getVariableClass().equals(Properties.getInitializedTargetClass()))&&
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
					! (test.getStatement(var.getStPosition()) instanceof FunctionalMockStatement) ){

				candidateVariables.add(var);
			}
		}

		if(candidateVariables.isEmpty()) {
			return null;
		} else if(Properties.SORT_OBJECTS) {
			candidateVariables = candidateVariables.stream().sorted(Comparator.comparingInt(item -> item.getDistance())).collect(Collectors.toList());
			return ListUtil.selectRankBiased(candidateVariables);
		} else {
			return Randomness.choice(candidateVariables);
		}
	}

}
