/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

import de.unisb.cs.st.evosuite.testcase.Scope;

/**
 * @author Sebastian Steenbuck
 *
 */
public class ConcurrentScope extends Scope{

	private final Object objectToTest;

	public ConcurrentScope(Object objectToTest){
		super();
		assert(objectToTest!=null);
		this.objectToTest=objectToTest;
	}
	
	public synchronized Object getSharedObject(){
		return objectToTest;
	}



}
