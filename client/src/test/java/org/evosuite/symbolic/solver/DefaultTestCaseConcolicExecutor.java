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
package org.evosuite.symbolic.solver;

import static org.evosuite.symbolic.SymbolicObserverTest.printConstraints;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.ConcolicExecution;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.testcase.DefaultTestCase;

public abstract class DefaultTestCaseConcolicExecutor {

	
	public static Collection<Constraint<?>> execute(DefaultTestCase tc) {
		List<BranchCondition> pc = getPathCondition(tc);
	
		Collection<Constraint<?>> constraints = new LinkedList<Constraint<?>>();
		for (BranchCondition condition : pc) {
			constraints.addAll(condition.getSupportingConstraints());
			Constraint<?> constraint = condition.getConstraint();
			constraints.add(constraint);
		}
		return constraints;
	}

	private static List<BranchCondition> getPathCondition(DefaultTestCase tc) {
		Properties.CLIENT_ON_THREAD = true;
		Properties.PRINT_TO_SYSTEM = true;
		Properties.TIMEOUT = 5000;
		Properties.CONCOLIC_TIMEOUT = 5000000;
	
		System.out.println("TestCase=");
		System.out.println(tc.toCode());
	
		List<BranchCondition> branch_conditions = ConcolicExecution
				.executeConcolic(tc);
	
		printConstraints(branch_conditions);
		return branch_conditions;
	}

}
