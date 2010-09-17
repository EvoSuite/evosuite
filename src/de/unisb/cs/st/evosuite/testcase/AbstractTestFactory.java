/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.util.List;

import de.unisb.cs.st.ga.ConstructionFailedException;

/**
 * @author Gordon Fraser
 *
 */
public abstract class AbstractTestFactory {

	public abstract void changeCall(TestCase test, Statement statement, AccessibleObject call) throws ConstructionFailedException;

	public abstract void insertRandomStatement(TestCase test);
	
	public abstract void appendStatement(TestCase test, Statement statement) throws ConstructionFailedException;
	
	public abstract void deleteStatement(TestCase test, int position) throws ConstructionFailedException;
	
	public abstract void deleteStatementGracefully(TestCase test, int position) throws ConstructionFailedException;
	
	public abstract boolean changeRandomCall(TestCase test, Statement statement);
}
