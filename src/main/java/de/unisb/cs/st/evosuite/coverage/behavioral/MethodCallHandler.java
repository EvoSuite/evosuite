package de.unisb.cs.st.evosuite.coverage.behavioral;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unisaarland.cs.st.adabu.core.state.Value;
import de.unisaarland.cs.st.adabu.trans.model.MethodInvocation;
import de.unisaarland.cs.st.adabu.trans.model.TransitiveObjectState;
import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * This class provides the methods to handle the assignment of a generated transition
 * (method call) in the object behavior model mined by <tt>ADABU</tt> to the
 * corresponding test-case leading to this transition.
 * 
 * <p>In the process of generating and exploring the object behavior model of the class
 * under test a transition is learned via observing the execution of a certain test-case.
 * This test-case is a list of statements, whereby one statement (probably the last)
 * is the constructor or method call statement that creates the transition.</br>
 * As the representation of the transition in the object behavior model mined by <tt>ADABU</tt>
 * does only contain the information of the call leading to this transition, the information
 * about the statements initializing the call are lost.</p>
 * 
 * <p>The <tt>MethodCallHandler</tt> makes now available the possibility of storing
 * a test-case with its unique method call signature in the corresponding object state
 * where the method was invoked. It is important to store the object state, as the call
 * signature is only unique in each object state. This way each test-case can be
 * retrieved given the object state and the transition of the object behavior model.</p>
 * 
 * @see BehavioralCoverage
 */
public class MethodCallHandler {
	
	/** The mapping holding the data of the method calls. */
	private Map<TransitiveObjectState,Map<String,TestCase>> map;
	
	/** The instance of an empty object state used for initializing and default operations. */
	private final TransitiveObjectState emptyObjectState = new TransitiveObjectState(0);
	
	/** The default signature for an object. */
	public static final String objectSignature = "object";
	
	/**
	 * Creates a new <tt>MethodCallHandler</tt> with default
	 * data, i.e. an empty mapping to method calls in the empty object state.
	 */
	public MethodCallHandler() {
		map = new HashMap<TransitiveObjectState,Map<String,TestCase>>();
		
		// initialize the empty object state mapping
		map.put(emptyObjectState, new HashMap<String,TestCase>());
	}
	
	/**
	 * Adds a given test-case to this handler according to
	 * the given object state.</br>
	 * If the given object state is <tt>null</tt> then the
	 * empty object state is used as default state.</p>
	 * 
	 * @param state - the object state at invocation time.
	 * @param test - the test-case to add.
	 */
	public void addTestCase(TransitiveObjectState state, TestCase test) {
		// check whether state is null
		if (state != null) {
			Map<String,TestCase> sigToTest = map.get(state); // mapping signature to test-case
			
			// check whether state has a mapping
			if (sigToTest == null) { // create a new state mapping
				sigToTest = new HashMap<String,TestCase>();
				map.put(state, sigToTest);
			}
			sigToTest.put(getSignature(test), test);
		} else { // use empty object state
			Map<String,TestCase> sigToTest = map.get(emptyObjectState);
			sigToTest.put(getSignature(test), test);
		}
	}
	
	/**
	 * Returns the test-case associated with given object state
	 * and method invocation or <tt>null</tt> if no test-case
	 * can be associated, i.e. when the given object state is
	 * <tt>null</tt> or the corresponding state mapping is empty.</p>
	 * 
	 * @param state - the object state at invocation time.
	 * @param invocation - the method invocation.
	 * 
	 * @return the associated test-case.
	 */
	public TestCase getTestCase(TransitiveObjectState state, MethodInvocation invocation) {
		// check whether state is null
		if (state != null) {
			Map<String,TestCase> sigToTest = map.get(state); // mapping signature to test-case
			
			// check whether state has a mapping
			if (sigToTest == null) { // no mapping
				return null;
			}
			return sigToTest.get(getSignature(invocation));
		} else { // no mapping
			return null;
		}
	}
	
	/**
	 * Creates the unique signature (whereby the uniqueness is state dependent)
	 * of a given test-case.</br>
	 * An empty signature is returned if the given test is empty
	 * or the last statement is not a constructor or method statement.</p>
	 * 
	 * @param test - the test-case to create the signature for.
	 * 
	 * @return the signature of the given test-case.
	 */
	public static String getSignature(TestCase test) {
		assert (test != null);
		if (test.isEmpty())
			return "";
		
		StringBuffer result = new StringBuffer();
		try {
			// execute the test to learn the parameters values
			Scope scope = new Scope();
			for (int i = 0; i < test.size(); i++) {
				test.getStatement(i).execute(scope, System.out);
			}
			
			// compute signature for last statement
			StatementInterface statement = test.getStatement(test.size()-1);
			// check for constructor statement
			if (statement instanceof ConstructorStatement) {
				ConstructorStatement constructorStatement = (ConstructorStatement) statement;
				Constructor<?> constructor = constructorStatement.getConstructor();
				
				// append the constructor name
				String name = constructor.getName();
				name = name.substring(name.indexOf(".")+1);
				result.append(name);
				
				// append the parameters of the constructor call
				List<VariableReference> var_refs = constructorStatement.getParameterReferences();
				result.append("(");
				for (int i = 0; i < var_refs.size(); i++) {
					Object object = var_refs.get(i).getObject(scope);
					if (object == null 
							|| var_refs.get(i).isPrimitive()
							|| var_refs.get(i).isEnum()) {
						result.append(object);
					} else {
						result.append(objectSignature);
					}
					
					if (i < var_refs.size()-1)
						result.append(", ");
				}
				result.append(")");
			} else if (statement instanceof MethodStatement) { // check for method statement
				MethodStatement methodStatement = (MethodStatement) statement;
				Method method = methodStatement.getMethod();
				
				// append the method name
				result.append(method.getName());
				
				// append the parameters of the constructor call
				List<VariableReference> var_refs = methodStatement.getParameterReferences();
				result.append("(");
				for (int i = 0; i < var_refs.size(); i++) {
					Object object = var_refs.get(i).getObject(scope);
					if (object == null 
							|| var_refs.get(i).isPrimitive()
							|| var_refs.get(i).isEnum()) {
						result.append(object);
					} else {
						result.append(objectSignature);
					}
					
					if (i < var_refs.size()-1)
						result.append(", ");
				}
				result.append(")");
			}
		} catch (Exception e) {
			System.out.print("* Warning: Error occurred during signature computation of a test: " + e.getMessage());
		}
		System.out.println("* Created Test-Signature: " + result.toString()); // for debugging TODO delete
		return result.toString();
	}
	
	/**
	 * Creates the unique signature (whereby the uniqueness is state dependent)
	 * of a given method invocation.</p>
	 * 
	 * @param invocation - the invocation to create the signature for.
	 * 
	 * @return the signature of the given method invocation.
	 */
	public static String getSignature(MethodInvocation invocation) {
		assert (invocation != null);
		
		StringBuffer result = new StringBuffer();
		
		// check whether the method invocation is a constructor call
		if (invocation.getIdentifier().isConstructor()) {
			String call = invocation.getIdentifier().getClassName();
			if (call.contains("/")) { // need simple name for constructor call
				call = call.substring(call.lastIndexOf("/")+1);
				result.append(call);
			}
		} else { // append method name
			result.append(invocation.getIdentifier().getMethodName());
		}
		
		// append the parameters of the invocation
		Map<Integer,Value> parameters = invocation.getParameters();
		result.append("(");
		for (int i = 1; i < parameters.keySet().size(); i++) { // key 0 is implicit object reference
			Value value = parameters.get(i);
			Object data = value.getData();
			
			// null and objects are integers
			if (value.getType().isComplexType() || value.getType().isArrayType()) {
				if (data == null || value.isNullReference())
					result.append("null");
				else if (data.getClass().isEnum())
					result.append(data);
				else
					result.append(objectSignature);
			} else {
				result.append(value.toString());
			}
			
			if (i < parameters.keySet().size()-1)
				result.append(", ");
		}
		result.append(")");
		System.out.println("* Created Invocation-Signature: " + result.toString()); // for debugging TODO delete
		return result.toString();
	}
}
