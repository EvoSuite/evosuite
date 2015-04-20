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
		Collection<Constraint<?>> constraints;
		List<BranchCondition> pc = getPathCondition(tc);
	
		constraints = new LinkedList<Constraint<?>>();
		for (BranchCondition condition : pc) {
			constraints.addAll(condition.getReachingConstraints());
			Constraint<?> constraint = condition.getLocalConstraint();
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
