package de.unisb.cs.st.evosuite.coverage.behavioral;

import java.util.LinkedList;

import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.DefaultTestCase;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/** Class that represents a transition sequence alpha. */
public class TransitionSequence extends LinkedList<TestCase> {
	
	private static final long serialVersionUID = -717835075340339395L;
	
	/**
	 * Creates a new transition sequence that is empty.
	 */
	public TransitionSequence() {
		super();
	}
	
	/**
	 * Creates a new transition sequence with given alpha
	 * or an empty one if alpha is <tt>null</tt>.</p>
	 * 
	 * @param alpha - the transition sequence to be added.
	 */
	public TransitionSequence(TransitionSequence alpha) {
		super();
		
		if (alpha != null)
			this.addAll(alpha);
	}
	
	/**
	 * Creates a new test-case from the transition sequence,
	 * i.e. a new <tt>DefaultTestCase</tt> containing all statements
	 * of all test-cases in the transition sequence.
	 * The statements are added in the order they occur in the
	 * single test-cases.</p>
	 * 
	 * @return the test-case represented by this transition sequence.
	 */
	public TestCase getTestCase() {
		TestCase result = new DefaultTestCase();
		VariableReference callee = null;
		
		// for all test-cases ...
		for (int i = 0; i < size(); i++) {
			TestCase test = get(i);
			// ... add all statements
			int offset = result.size();
			for (int j = 0; j < test.size(); j++) {
				StatementInterface statement = test.getStatement(j);
				
				// need to copy the statement
				result.addStatement(statement); // need to add the old statement first due to inconsistency
				StatementInterface copy = statement.copy(result, offset);
				result.remove(result.size()-1); // remove old statement before adding the copy
				result.addStatement(copy);
				copy.SetRetval(statement.getReturnValue().copy(result, offset));
				copy.setAssertions(statement.copyAssertions(result, offset));
				
				// update the calling variable reference for last method statement
				if (j == test.size()-1) {
					if (i == 0) {
						assert (copy instanceof ConstructorStatement)
							: "The last statement is no constructor statement: " + test;
						callee = copy.getReturnValue();
					} else {
						assert (copy instanceof MethodStatement)
							: "The last statement is no method statement: " + test;
						MethodStatement methodStatement = (MethodStatement) copy;
						
						if (callee != null && !methodStatement.isStatic())
							methodStatement.setCallee(callee);
					}
				}
			}
			
			// add all covered goals
			for (TestFitnessFunction goal : test.getCoveredGoals()) {
				result.addCoveredGoal(goal);
			}
		}
		return result;
	}
}
