package de.unisb.cs.st.evosuite.coverage.concurrency;

import de.unisb.cs.st.evosuite.testcase.StatementInterface;

/**
 * Wraps a statement and redefines the equals method
 * @author steenbuck
 *
 */
public class SameWrapper {
	public final StatementInterface wrapped;
	public SameWrapper(StatementInterface st){
		assert(st!=null);
		wrapped=st;
	}
	
	@Override
	public boolean equals(Object obj){
		assert(obj!=null);
		assert(obj instanceof SameWrapper) : "We expect a sameWrapper, not an " + obj.getClass();
		if(obj instanceof SameWrapper){
			SameWrapper other = (SameWrapper)obj;
			return wrapped.same(other.wrapped);
		}
		return false;
	}
	
	@Override 
	public int hashCode(){
		return 0; //#TODO this should be more complex ;)
	}
}
