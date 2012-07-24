package org.evosuite.symbolic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.IntegerUnaryExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealComparison;
import org.evosuite.symbolic.expr.RealUnaryExpression;
import org.junit.Test;

public class ConcolicExecutionTest {

	@Test
	public void test_TestCase0() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase0",
						System.getProperty("java.class.path"));
		assertEquals(1, branch_conditions.size());
		BranchCondition bc = branch_conditions.get(0);
		assertEquals(1, bc.localConstraints.size());
		assertTrue(bc.reachingConstraints.isEmpty());
	}

	@Test
	public void test_TestCase1() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase1",
						System.getProperty("java.class.path"));
		assertEquals(1, branch_conditions.size());
		BranchCondition bc = branch_conditions.get(0);
		assertEquals(3, bc.localConstraints.size());
		assertTrue(bc.reachingConstraints.isEmpty());
	}

	@Test
	public void test_TestCase2() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase2",
						System.getProperty("java.class.path"));
	}

	@Test
	public void test_TestCase3() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase3",
						System.getProperty("java.class.path"));
		assertTrue(!branch_conditions.isEmpty());
	}

	@Test
	public void test_TestCase4() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase4",
						System.getProperty("java.class.path"));
		assertTrue(!branch_conditions.isEmpty());
		assertEquals(2, branch_conditions.size());
	}

	@Test
	public void test_TestCase5() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase5",
						System.getProperty("java.class.path"));
		assertTrue(!branch_conditions.isEmpty());
		assertEquals(2, branch_conditions.size());
	}

	@Test
	public void test_TestCase6() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase6",
						System.getProperty("java.class.path"));
		assertTrue(!branch_conditions.isEmpty());
		assertEquals(2, branch_conditions.size());
	}

	@Test
	public void test_TestCase7() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase7",
						System.getProperty("java.class.path"));
		assertTrue(branch_conditions.isEmpty());
	}

	@Test
	public void test_TestCase8() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase8",
						System.getProperty("java.class.path"));
		assertEquals(1, branch_conditions.size());
	}

	@Test
	public void test_TestCase9() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase9",
						System.getProperty("java.class.path"));
		assertEquals(0, branch_conditions.size());
	}

	@Test
	public void test_TestCase10() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase10",
						System.getProperty("java.class.path"));
		assertEquals(2, branch_conditions.size());
	}

	@Test
	public void test_TestCase11() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase11",
						System.getProperty("java.class.path"));
	}

	@Test
	public void test_TestCase12() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase12",
						System.getProperty("java.class.path"));
	}

	@Test
	public void test_TestCase13() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase13",
						System.getProperty("java.class.path"));
		assertEquals(1, branch_conditions.size());
	}

	@Test
	public void test_TestCase14() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase14",
						System.getProperty("java.class.path"));
		assertEquals(22, branch_conditions.size());

		BranchCondition bc0 = branch_conditions.get(0);
		assertTrue(bc0.reachingConstraints.isEmpty());
		assertEquals(2, bc0.localConstraints.size());

		int reachingConstraints = 2;
		for (int i = 1; i < branch_conditions.size(); i++) {
			BranchCondition bc = branch_conditions.get(i);
			assertEquals(reachingConstraints, bc.reachingConstraints.size());
			assertEquals(1, bc.localConstraints.size());
			reachingConstraints++;
		}

	}

	@Test
	public void test_TestCase15() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase15",
						System.getProperty("java.class.path"));

		assertEquals(4, branch_conditions.size());

		BranchCondition bc0 = branch_conditions.get(0);
		BranchCondition bc1 = branch_conditions.get(1);
		BranchCondition bc2 = branch_conditions.get(2);
		BranchCondition bc3 = branch_conditions.get(3);

		assertEquals(0, bc0.reachingConstraints.size());
		assertEquals(2, bc0.localConstraints.size());

		assertEquals(2, bc1.reachingConstraints.size());
		assertEquals(1, bc1.localConstraints.size());

		assertEquals(3, bc2.reachingConstraints.size());
		assertEquals(1, bc2.localConstraints.size());

		assertEquals(4, bc3.reachingConstraints.size());
		assertEquals(1, bc3.localConstraints.size());

	}

	@Test
	public void test_TestCase16() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase16",
						System.getProperty("java.class.path"));

		assertEquals(4, branch_conditions.size());
		{
			BranchCondition b0 = branch_conditions.get(0);
			List<Constraint<?>> local_constraints_b0 = b0
					.listOfLocalConstraints();
			IntegerConstraint int_comparison = (IntegerConstraint) local_constraints_b0
					.get(2);
			IntegerUnaryExpression abs_expr = (IntegerUnaryExpression) int_comparison
					.getRightOperand();
			assertEquals(Operator.ABS, abs_expr.getOperator());
		}
		{
			BranchCondition b1 = branch_conditions.get(1);
			List<Constraint<?>> local_constraints_b1 = b1
					.listOfLocalConstraints();
			IntegerConstraint int_comparison = (IntegerConstraint) local_constraints_b1
					.get(2);
			IntegerUnaryExpression abs_expr = (IntegerUnaryExpression) int_comparison
					.getRightOperand();
			assertEquals(Operator.ABS, abs_expr.getOperator());
		}
		{
			BranchCondition b2 = branch_conditions.get(2);
			List<Constraint<?>> local_constraints_b2 = b2
					.listOfLocalConstraints();
			IntegerConstraint int_constraint = (IntegerConstraint) local_constraints_b2
					.get(2);

			RealComparison realComparison = (RealComparison) int_constraint
					.getRightOperand();

			RealUnaryExpression unaryFunction = (RealUnaryExpression) realComparison
					.getRightOperant();

			assertEquals(Operator.ABS, unaryFunction.getOperator());

		}
		{
			BranchCondition b3 = branch_conditions.get(3);
			List<Constraint<?>> local_constraints_b3 = b3
					.listOfLocalConstraints();
			IntegerConstraint int_constraint = (IntegerConstraint) local_constraints_b3
					.get(2);

			RealComparison realComparison = (RealComparison) int_constraint
					.getRightOperand();

			RealUnaryExpression unaryFunction = (RealUnaryExpression) realComparison
					.getRightOperant();

			assertEquals(Operator.ABS, unaryFunction.getOperator());
		}
	}

	@Test
	public void test_TestCase17() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase17",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}
	
	@Test
	public void test_TestCase18() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase18",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}
	
	@Test
	public void test_TestCase19() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase19",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}
	
	@Test
	public void test_TestCase20() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase20",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}
	
	@Test
	public void test_TestCase21() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase21",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}
	
	@Test
	public void test_TestCase22() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase22",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}
	
	@Test
	public void test_TestCase23() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase23",
						System.getProperty("java.class.path"));

		assertEquals(1, branch_conditions.size());

	}
	
	@Test
	public void test_TestCase24() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase24",
						System.getProperty("java.class.path"));

		assertEquals(3, branch_conditions.size());

	}
	
	@Test
	public void test_TestCase25() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase25",
						System.getProperty("java.class.path"));

		assertEquals(8, branch_conditions.size());

	}
	
	@Test
	public void test_TestCase26() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase26",
						System.getProperty("java.class.path"));

		assertEquals(1, branch_conditions.size());

	}
	
	@Test
	public void test_TestCase27() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase27",
						System.getProperty("java.class.path"));

		assertEquals(8, branch_conditions.size());

	}
	
	@Test
	public void test_TestCase28() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase28",
						System.getProperty("java.class.path"));

		assertEquals(5, branch_conditions.size());

	}
	
	@Test
	public void test_TestCase29() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase29",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}
	
	@Test
	public void test_TestCase30() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase30",
						System.getProperty("java.class.path"));

		assertEquals(1, branch_conditions.size());

	}
	
	@Test
	public void test_TestCase31() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase31",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}
	
	@Test
	public void test_TestCase32() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase32",
						System.getProperty("java.class.path"));

		assertEquals(1, branch_conditions.size());

	}
}
