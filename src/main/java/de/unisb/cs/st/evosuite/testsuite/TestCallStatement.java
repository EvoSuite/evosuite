/**
 * 
 */
package de.unisb.cs.st.evosuite.testsuite;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.commons.GeneratorAdapter;

import de.unisb.cs.st.evosuite.ga.stoppingconditions.MaxStatementsStoppingCondition;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.Statement;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author Gordon Fraser
 * 
 */
public class TestCallStatement extends Statement {

	private final TestCallObject testCall;

	public TestCallStatement(TestCallObject call, VariableReference retval) {
		this.testCall = call;
		this.retval = retval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#adjustVariableReferences(int,
	 * int)
	 */
	@Override
	public void adjustVariableReferences(int position, int delta) {
		retval.adjust(delta, position);
	}

	/**
	 * Execute a test case
	 * 
	 * @param test
	 *            The test case to execute
	 * @param mutant
	 *            The mutation to active (null = no mutation)
	 * 
	 * @return Result of the execution
	 */
	public Object runTest(TestCase test) {

		TestCaseExecutor executor = TestCaseExecutor.getInstance();
		try {
			Scope scope = new Scope();
			// logger.info("Starting test call " + test.toCode());
			// logger.info("Original test was: " + testCall.testCase.toCode());
			executor.execute(test, scope);
			executor.setLogging(true);

			// TODO: Count as 1 or length?
			int num = test.size();
			MaxStatementsStoppingCondition.statementsExecuted(num);
			// logger.info("Finished test call. Getting variables of type: "
			// + testCall.getReturnType());
			List<VariableReference> variables = scope.getElements(testCall.getReturnType());

			// logger.info("Got " + variables.size());
			if (!variables.isEmpty()) {
				Collections.sort(variables, Collections.reverseOrder());
				// logger.info("Return value is good: "
				// + scope.get(variables.get(0)).getClass());
				return scope.get(variables.get(0));
			}

		} catch (Exception e) {
			System.out.println("TestCallStatement: Exception caught: " + e);
			try {
				Thread.sleep(1000);
			} catch (Exception e1) {
				e.printStackTrace();
				// TODO: Do some error recovery?
				System.exit(1);
			}

		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#execute(de.unisb.cs.st.evosuite
	 * .testcase.Scope, java.io.PrintStream)
	 */
	@Override
	public Throwable execute(Scope scope, PrintStream out)
	        throws InvocationTargetException, IllegalArgumentException,
	        IllegalAccessException, InstantiationException {

		TestCase test = testCall.getTest();
		if (test != null && !test.hasCalls()) {
			Object value = runTest(test);
			scope.set(retval, value);
		} else {
			scope.set(retval, null);
		}

		return null; // TODO: Pass on any of the exceptions?
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#getCode(java.lang.Throwable)
	 */
	@Override
	public String getCode(Throwable exception) {

		TestCase test = testCall.getTest();
		if (test == null || test.hasCalls()) {
			return retval.getSimpleClassName() + " " + retval.getName()
			        + " = call to null";
		}
		int num = 0;
		for (TestCase other : testCall.getSuite().getTests()) {
			if (test.equals(other))
				return retval.getSimpleClassName() + " " + retval.getName()
				        + " = Call to test case: " + num + "...\n" + test.toCode()
				        + "...\n";
			num++;
		}

		return retval.getSimpleClassName() + " " + retval.getName()
		        + " = Call to test case (null) ";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#getBytecode(org.objectweb.
	 * asm.commons.GeneratorAdapter, java.util.Map, java.lang.Throwable)
	 */
	@Override
	public void getBytecode(GeneratorAdapter mg, Map<Integer, Integer> locals,
	        Throwable exception) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.evosuite.testcase.Statement#getVariableReferences()
	 */
	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> vars = new HashSet<VariableReference>();
		vars.add(retval);
		return vars;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#replace(de.unisb.cs.st.evosuite
	 * .testcase.VariableReference,
	 * de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void replace(VariableReference old_var, VariableReference new_var) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.unisb.cs.st.evosuite.testcase.Statement#clone()
	 */
	@Override
	public StatementInterface clone() {
		TestCallStatement statement = new TestCallStatement(testCall, retval.clone());
		return statement;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((testCall == null) ? 0 : testCall.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;
		TestCallStatement other = (TestCallStatement) s;
		if (testCall == null) {
			if (other.testCall != null)
				return false;
		} else if (!testCall.equals(other.testCall))
			return false;
		return true;
	}

	public TestCase getTest() {
		return testCall.getTest();
	}

	public int getTestNum() {
		return testCall.getNum();
	}

	public void setTestNum(int num) {
		testCall.setNum(num);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#getUniqueVariableReferences()
	 */
	@Override
	public List<VariableReference> getUniqueVariableReferences() {
		return new ArrayList<VariableReference>(getVariableReferences());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.Statement#replaceUnique(de.unisb.cs.
	 * st.evosuite.testcase.VariableReference,
	 * de.unisb.cs.st.evosuite.testcase.VariableReference)
	 */
	@Override
	public void replaceUnique(VariableReference old_var, VariableReference new_var) {
		// TODO Auto-generated method stub

	}

}
