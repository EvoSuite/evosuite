/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.concurrency;

import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * @author x3k6a2
 *
 */
public class ConcurrentScope extends Scope{

	private final Object objectToTest;

	public ConcurrentScope(Object objectToTest){
		super();
		assert(objectToTest!=null);
		this.objectToTest=objectToTest;
	}

	@Override
	public synchronized Object get(VariableReference reference){
		//#TODO steenbuck short term
		if(reference.statement==-1){
			//#TODO types should be checked
			return objectToTest;
		}else{
			return super.get(reference);
		}
	}



}
