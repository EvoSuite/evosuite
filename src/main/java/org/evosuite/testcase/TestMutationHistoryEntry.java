package org.evosuite.testcase;

import org.evosuite.ga.MutationHistoryEntry;

public class TestMutationHistoryEntry implements MutationHistoryEntry {

    public enum TestMutation {
	CHANGE, INSERTION, DELETION
	    };
    
    protected TestMutation mutationType;

    protected StatementInterface statement;

    public TestMutationHistoryEntry(TestMutation type, StatementInterface statement) {
	this.mutationType = type;
	this.statement = statement;
    }

    public TestMutationHistoryEntry(TestMutation type) {
	this.mutationType = type;
	this.statement = null;
    }
    
    public StatementInterface getStatement() {
	return statement;
    }
    
    public TestMutation getMutationType() {
	return mutationType;
    }

    public TestMutationHistoryEntry clone(TestCase newTest) {
	if(statement == null)
	    return new TestMutationHistoryEntry(mutationType);

	return new TestMutationHistoryEntry(mutationType, statement.clone(newTest));
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
	return mutationType + " at " + statement;
    }
}
