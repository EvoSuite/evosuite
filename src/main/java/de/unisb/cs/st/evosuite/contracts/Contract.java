/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.Statement;

/**
 * @author Gordon Fraser
 * 
 */
public interface Contract {

	public boolean check(Statement statement, Scope scope, Throwable exception);

}
