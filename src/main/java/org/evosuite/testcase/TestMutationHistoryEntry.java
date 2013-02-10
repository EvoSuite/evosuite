package org.evosuite.testcase;

import org.evosuite.ga.MutationHistoryEntry;
import org.evosuite.utils.LoggingUtils;

public class TestMutationHistoryEntry implements MutationHistoryEntry {

    public enum TestMutation {
	CHANGE, INSERTION, DELETION
	    };
    
    protected TestMutation mutationType;

    protected StatementInterface statement;
    
    public String whatwasit;

    public TestMutationHistoryEntry(TestMutation type, StatementInterface statement) {
    	this.mutationType = type;
    	this.statement = statement;
    	this.whatwasit = statement.getCode() +" at position "+statement.getPosition();
    	LoggingUtils.getEvoLogger().info("Created new mutation "+whatwasit+" / "+statement.getReturnValue().getStPosition());
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
	if(statement == null)
	    return new TestMutationHistoryEntry(mutationType);

	return new TestMutationHistoryEntry(mutationType, newTest.getStatement(statement.getPosition()));
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
	return mutationType + " at " + statement;
    }
}
