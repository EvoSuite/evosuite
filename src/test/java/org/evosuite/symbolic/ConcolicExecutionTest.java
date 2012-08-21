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
		assertEquals(1, bc.localConstraints.size());
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
		assertEquals(1, branch_conditions.size());
	}

	@Test
	public void test_TestCase6() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase6",
						System.getProperty("java.class.path"));
		assertTrue(!branch_conditions.isEmpty());
		assertEquals(1, branch_conditions.size());
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
		assertEquals(0, branch_conditions.size());
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
		assertEquals(1, branch_conditions.size());
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
		assertEquals(1, bc0.localConstraints.size());

		int reachingConstraints = 1;
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
		assertEquals(1, bc0.localConstraints.size());

		assertEquals(1, bc1.reachingConstraints.size());
		assertEquals(1, bc1.localConstraints.size());

		assertEquals(2, bc2.reachingConstraints.size());
		assertEquals(1, bc2.localConstraints.size());

		assertEquals(3, bc3.reachingConstraints.size());
		assertEquals(1, bc3.localConstraints.size());

	}

	@Test
	public void test_TestCase16() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase16",
						System.getProperty("java.class.path"));

		assertEquals(4, branch_conditions.size());
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

		assertEquals(2, branch_conditions.size());

	}

	@Test
	public void test_TestCase33() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase33",
						System.getProperty("java.class.path"));

		assertEquals(1, branch_conditions.size());

	}

	@Test
	public void test_TestCase34() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase34",
						System.getProperty("java.class.path"));

		assertEquals(1, branch_conditions.size());

	}

	@Test
	public void test_TestCase35() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase35",
						System.getProperty("java.class.path"));

		assertEquals(3, branch_conditions.size());

	}

	@Test
	public void test_TestCase36() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase36",
						System.getProperty("java.class.path"));

		assertEquals(3, branch_conditions.size());

	}

	@Test
	public void test_TestCase37() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase37",
						System.getProperty("java.class.path"));

		assertEquals(1, branch_conditions.size());

	}

	@Test
	public void test_TestCase38() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase38",
						System.getProperty("java.class.path"));

		assertEquals(7, branch_conditions.size());

	}

	@Test
	public void test_TestCase39() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase39",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}

	@Test
	public void test_TestCase40() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase40",
						System.getProperty("java.class.path"));

		assertEquals(1, branch_conditions.size());

	}

	@Test
	public void test_TestCase41() {

		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase41",
						System.getProperty("java.class.path"));

		assertEquals(9, branch_conditions.size());

	}

	@Test
	public void test_TestCase42() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase42",
						System.getProperty("java.class.path"));

		assertEquals(1, branch_conditions.size());

	}

	@Test
	public void test_TestCase43() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase43",
						System.getProperty("java.class.path"));

		assertEquals(1, branch_conditions.size());

	}

	@Test
	public void test_TestCase44() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase44",
						System.getProperty("java.class.path"));

		assertEquals(10, branch_conditions.size());

	}

	@Test
	public void test_TestCase45() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase45",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}

	@Test
	public void test_TestCase46() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase46",
						System.getProperty("java.class.path"));

		assertEquals(22, branch_conditions.size());

	}

	@Test
	public void test_TestCase47() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase47",
						System.getProperty("java.class.path"));

	}

	@Test
	public void test_TestCase48() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase48",
						System.getProperty("java.class.path"));

	}

	@Test
	public void test_TestCase49() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase49",
						System.getProperty("java.class.path"));

	}

	@Test
	public void test_TestCase50() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase50",
						System.getProperty("java.class.path"));

	}

	@Test
	public void test_TestCase51() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase51",
						System.getProperty("java.class.path"));

	}

	@Test
	public void test_TestCase52() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase52",
						System.getProperty("java.class.path"));

	}

	@Test
	public void test_TestCase53() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase53",
						System.getProperty("java.class.path"));

	}

	@Test
	public void test_TestCase54() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase54",
						System.getProperty("java.class.path"));

	}

	@Test
	public void test_TestCase55() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase55",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}

	@Test
	public void test_TestCase56() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase56",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}

	@Test
	public void test_TestCase57() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase57",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}

	@Test
	public void test_TestCase58() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase58",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}

	@Test
	public void test_TestCase59() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase59",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}

	@Test
	public void test_TestCase60() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase60",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}

	@Test
	public void test_TestCase61() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase61",
						System.getProperty("java.class.path"));

		assertEquals(5, branch_conditions.size());

	}

	@Test
	public void test_TestCase62() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase62",
						System.getProperty("java.class.path"));

		assertEquals(5, branch_conditions.size());

	}

	@Test
	public void test_TestCase63() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase63",
						System.getProperty("java.class.path"));

		assertEquals(8, branch_conditions.size());

	}

	@Test
	public void test_TestCase64() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase64",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}

	@Test
	public void test_TestCase65() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase65",
						System.getProperty("java.class.path"));

		assertEquals(5, branch_conditions.size());

	}

	@Test
	public void test_TestCase66() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase66",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}

	@Test
	public void test_TestCase67() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase67",
						System.getProperty("java.class.path"));

		assertEquals(2, branch_conditions.size());

	}

	@Test
	public void test_TestCase68() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase68",
						System.getProperty("java.class.path"));

		assertEquals(4, branch_conditions.size());

	}

	@Test
	public void test_TestCase69() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase69",
						System.getProperty("java.class.path"));

		assertEquals(264, branch_conditions.size());

	}

	@Test
	public void test_TestCase70() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase70",
						System.getProperty("java.class.path"));

	}

	@Test
	public void test_TestCase71() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase71",
						System.getProperty("java.class.path"));

	}

	@Test
	public void test_TestCase72() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase72",
						System.getProperty("java.class.path"));

		for (BranchCondition branchCondition : branch_conditions) {
			for (Constraint<?> constr : branchCondition
					.listOfLocalConstraints()) {
				System.out.println(constr.toString());
			}
		}
		assertEquals(163, branch_conditions.size());
	}

	@Test
	public void test_TestCase73() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase73",
						System.getProperty("java.class.path"));

		for (BranchCondition branchCondition : branch_conditions) {
			for (Constraint<?> constr : branchCondition
					.listOfLocalConstraints()) {
				System.out.println(constr.toString());
			}
		}
		assertEquals(1, branch_conditions.size());
	}

	@Test
	public void test_TestCase74() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase74",
						System.getProperty("java.class.path"));

		for (BranchCondition branchCondition : branch_conditions) {
			for (Constraint<?> constr : branchCondition
					.listOfLocalConstraints()) {
				System.out.println(constr.toString());
			}
		}
		assertEquals(4, branch_conditions.size());
	}

	@Test
	public void test_TestCase75() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase75",
						System.getProperty("java.class.path"));

		for (BranchCondition branchCondition : branch_conditions) {
			for (Constraint<?> constr : branchCondition
					.listOfLocalConstraints()) {
				System.out.println(constr.toString());
			}
		}
		assertEquals(3, branch_conditions.size());
	}

	@Test
	public void test_TestCase76() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase76",
						System.getProperty("java.class.path"));

		for (BranchCondition branchCondition : branch_conditions) {
			for (Constraint<?> constr : branchCondition
					.listOfLocalConstraints()) {
				System.out.println(constr.toString());
			}
		}
		assertEquals(1, branch_conditions.size());
	}

	@Test
	public void test_TestCase77() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase77",
						System.getProperty("java.class.path"));

		for (BranchCondition branchCondition : branch_conditions) {
			for (Constraint<?> constr : branchCondition
					.listOfLocalConstraints()) {
				System.out.println(constr.toString());
			}
		}
		assertEquals(3, branch_conditions.size());
	}

	@Test
	public void test_TestCase78() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase78",
						System.getProperty("java.class.path"));

		for (BranchCondition branchCondition : branch_conditions) {
			for (Constraint<?> constr : branchCondition
					.listOfLocalConstraints()) {
				System.out.println(constr.toString());
			}
		}
		assertEquals(1, branch_conditions.size());
	}

	@Test
	public void test_TestCase79() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase79",
						System.getProperty("java.class.path"));

		for (BranchCondition branchCondition : branch_conditions) {
			for (Constraint<?> constr : branchCondition
					.listOfLocalConstraints()) {
				System.out.println(constr.toString());
			}
		}
		assertEquals(1, branch_conditions.size());
	}

	@Test
	public void test_TestCase80() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase80",
						System.getProperty("java.class.path"));

		for (BranchCondition branchCondition : branch_conditions) {
			for (Constraint<?> constr : branchCondition
					.listOfLocalConstraints()) {
				System.out.println(constr.toString());
			}
		}
		assertEquals(1, branch_conditions.size());
	}

	@Test
	public void test_TestCase81() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase81",
						System.getProperty("java.class.path"));

		for (BranchCondition branchCondition : branch_conditions) {
			for (Constraint<?> constr : branchCondition
					.listOfLocalConstraints()) {
				System.out.println(constr.toString());
			}
		}
		assertEquals(1, branch_conditions.size());
	}

	@Test
	public void test_TestCase82() {
		ConcolicExecution concolicExecutor = new ConcolicExecution();
		List<BranchCondition> branch_conditions = concolicExecutor
				.executeConcolic("org.evosuite.symbolic.TestCase82",
						System.getProperty("java.class.path"));

		for (BranchCondition branchCondition : branch_conditions) {
			for (Constraint<?> constr : branchCondition
					.listOfLocalConstraints()) {
				System.out.println(constr.toString());
			}
		}
		assertEquals(2, branch_conditions.size());
	}
}
