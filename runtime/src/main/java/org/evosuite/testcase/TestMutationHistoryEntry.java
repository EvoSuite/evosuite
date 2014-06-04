package org.evosuite.testcase;

import java.io.Serializable;

import org.evosuite.ga.MutationHistoryEntry;

public class TestMutationHistoryEntry implements MutationHistoryEntry, Serializable {

	private static final long serialVersionUID = -4278409687247714553L;

	public enum TestMutation {
		CHANGE, INSERTION, DELETION
	};

	protected TestMutation mutationType;

	protected StatementInterface statement;

	public String whatwasit;

	public TestMutationHistoryEntry(TestMutation type, StatementInterface statement) {
		this.mutationType = type;
		this.statement = statement;
		this.whatwasit = statement.getCode() + " at position " + statement.getPosition();
	}

	public TestMutationHistoryEntry(TestMutation type) {
		this.mutationType = type;
		this.statement = null;
		this.whatwasit = "Deleted some statement";
	}

	public StatementInterface getStatement() {
		return statement;
	}

	public TestMutation getMutationType() {
		return mutationType;
	}

	public TestMutationHistoryEntry clone(TestCase newTest) {
		if (statement == null)
			return new TestMutationHistoryEntry(mutationType);

		return new TestMutationHistoryEntry(mutationType,
		        newTest.getStatement(statement.getPosition()));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return mutationType + " at " + statement;
	}
}
