package org.evosuite.testcase;

import org.evosuite.ga.MutationHistoryEntry;

public class TestMutationHistoryEntry implements MutationHistoryEntry {

	private StatementInterface statement;
	
	private int originalNum = 0;
	
	protected enum TestMutation {
		CHANGE,
		INSERTION,
		DELETION
		};
		
	protected TestMutation mutationType;
	
	public TestMutationHistoryEntry(StatementInterface statement, TestMutation type) {
		this.statement = statement;
		this.mutationType = type;
		originalNum = statement.getPosition();
	}
	
	public StatementInterface getStatement() {
		return statement;
	}
	
	public int getStatementPosition() {
		return originalNum;
	}

	
	public TestMutation getMutationType() {
		return mutationType;
	}
}
