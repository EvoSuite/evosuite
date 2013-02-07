/**
 * 
 */
package org.evosuite.testcase;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.primitives.ObjectPool;
import org.evosuite.runtime.EvoSuiteFile;
import org.evosuite.setup.TestCluster;
import org.evosuite.testsuite.TestCallStatement;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gordon Fraser
 * 
 */
public class TestFactory {

	private static final Logger logger = LoggerFactory.getLogger(TestFactory.class);

	/**
	 * Keep track of objects we are already trying to generate to avoid cycles
	 */
	private transient Set<AccessibleObject> currentRecursion = new HashSet<AccessibleObject>();

	private static TestFactory instance = null;

	public void reset() {
		// MethodDescriptorReplacement.getInstance().reset();
		currentRecursion.clear();
	}

	public static TestFactory getInstance() {
		if (instance == null)
			instance = new TestFactory();
		return instance;
	}

	public void resetRecursion() {
		currentRecursion.clear();
	}

	/**
	 * Append given call to the test case at given position
	 * 
	 * @param test
	 * @param call
	 * @param position
	 */
	private boolean addCallFor(TestCase test, VariableReference callee,
	        AccessibleObject call, int position) {
		logger.trace("addCallFor " + callee.getName());

		int previousLength = test.size();
		currentRecursion.clear();
		try {
			if (call instanceof Method) {
				addMethodFor(test, callee, (Method) call, position);
			} else if (call instanceof Field) {
				addFieldFor(test, callee, (Field) call, position);
			}
			return true;
		} catch (ConstructionFailedException e) {
			// TODO: Check this!
			logger.debug("Inserting call " + call + " has failed. Removing statements");
			// System.out.println("TG: Failed");
			// TODO: Doesn't work if position != test.size()
			int lengthDifference = test.size() - previousLength;
			for (int i = lengthDifference - 1; i >= 0; i--) { //we need to remove them in order, so that the testcase is at all time consistent 
				logger.debug("  Removing statement: "
				        + test.getStatement(position + i).getCode());
				test.remove(position + i);
			}
			return false;
		}
	}

	/**
	 * Add constructor at given position if max recursion depth has not been
	 * reached
	 * 
	 * @param constructor
	 * @param position
	 * @param recursionDepth
	 * @return
	 * @throws ConstructionFailedException
	 */
	public VariableReference addConstructor(TestCase test, Constructor<?> constructor,
	        int position, int recursionDepth) throws ConstructionFailedException {
		if (recursionDepth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException("Max recursion depth reached");
		}
		logger.debug("Adding constructor " + constructor.toGenericString());

		int length = test.size();
		List<VariableReference> parameters = satisfyParameters(test,
		                                                       null,
		                                                       Arrays.asList(constructor.getGenericParameterTypes()),
		                                                       position,
		                                                       recursionDepth + 1);
		int newLength = test.size();
		position += (newLength - length);

		StatementInterface st = new ConstructorStatement(test, constructor,
		        constructor.getDeclaringClass(), parameters);
		return test.addStatement(st, position);
	}

	/**
	 * Add a field to the test case
	 * 
	 * @param test
	 * @param field
	 * @param position
	 * @return
	 * @throws ConstructionFailedException
	 */
	public VariableReference addField(TestCase test, Field field, int position)
	        throws ConstructionFailedException {
		logger.debug("Adding field " + field.toGenericString());

		VariableReference callee = null;
		int length = test.size();

		if (!Modifier.isStatic(field.getModifiers())) {
			try {
				callee = test.getRandomNonNullObject(field.getDeclaringClass(), position);
			} catch (ConstructionFailedException e) {
				logger.debug("No callee of type " + field.getDeclaringClass().getName()
				        + " found");
				callee = attemptGeneration(test, field.getDeclaringClass(), position, 0,
				                           false);
				position += test.size() - length;
				length = test.size();
			}
		}

		StatementInterface st = new FieldStatement(test, field, callee,
		        field.getGenericType());

		return test.addStatement(st, position);
	}

	/**
	 * Add method at given position if max recursion depth has not been reached
	 * 
	 * @param test
	 * @param method
	 * @param position
	 * @param recursionDepth
	 * @return
	 * @throws ConstructionFailedException
	 */
	public VariableReference addFieldAssignment(TestCase test, Field field, int position,
	        int recursionDepth) throws ConstructionFailedException {
		logger.debug("Recursion depth: " + recursionDepth);
		if (recursionDepth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException("Max recursion depth reached");
		}
		logger.debug("Adding field " + field.toGenericString());

		int length = test.size();
		VariableReference callee = null;
		if (!Modifier.isStatic(field.getModifiers())) { // TODO: Consider reuse
			                                            // probability here?
			try {
				// TODO: Would casting be an option here?
				callee = test.getRandomNonNullObject(field.getDeclaringClass(), position);
				logger.debug("Found callee of type "
				        + field.getDeclaringClass().getName());
			} catch (ConstructionFailedException e) {
				logger.debug("No callee of type " + field.getDeclaringClass().getName()
				        + " found");
				callee = attemptGeneration(test, field.getDeclaringClass(), position,
				                           recursionDepth, false);
				position += test.size() - length;
				length = test.size();
			}
		}

		VariableReference var = createOrReuseVariable(test, field.getGenericType(),
		                                              position, recursionDepth, callee);
		int newLength = test.size();
		position += (newLength - length);

		FieldReference f = new FieldReference(test, field, callee);
		if (f.equals(var))
			throw new ConstructionFailedException("Self assignment");

		StatementInterface st = new AssignmentStatement(test, f, var);
		VariableReference ret = test.addStatement(st, position);
		// logger.info("FIeld assignment: " + st.getCode());
		assert (test.isValid());
		return ret;
	}

	/**
	 * Add reference to a field of variable "callee"
	 * 
	 * @param test
	 * @param callee
	 * @param field
	 * @param position
	 * @return
	 * @throws ConstructionFailedException
	 */
	public VariableReference addFieldFor(TestCase test, VariableReference callee,
	        Field field, int position) throws ConstructionFailedException {
		logger.debug("Adding field " + field.toGenericString() + " for variable "
		        + callee);
		currentRecursion.clear();

		FieldReference fieldVar = new FieldReference(test, field, callee);
		int length = test.size();
		VariableReference value = createOrReuseVariable(test, fieldVar.getType(),
		                                                position, 0, callee);

		int newLength = test.size();
		position += (newLength - length);

		StatementInterface st = new AssignmentStatement(test, fieldVar, value);
		VariableReference ret = test.addStatement(st, position);
		ret.setDistance(callee.getDistance() + 1);

		assert (test.isValid());

		return ret;
	}

	/**
	 * Add method at given position if max recursion depth has not been reached
	 * 
	 * @param test
	 * @param method
	 * @param position
	 * @param recursionDepth
	 * @return
	 * @throws ConstructionFailedException
	 */
	public VariableReference addMethod(TestCase test, Method method, int position,
	        int recursionDepth) throws ConstructionFailedException {

		logger.debug("Recursion depth: " + recursionDepth);
		if (recursionDepth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException("Max recursion depth reached");
		}
		logger.debug("Adding method " + method.toGenericString());
		int length = test.size();
		VariableReference callee = null;
		List<VariableReference> parameters = null;
		try {
			if (!Modifier.isStatic(method.getModifiers())) { // TODO: Consider reuse
				                                             // probability here?
				try {
					// TODO: Would casting be an option here?
					callee = test.getRandomNonNullNonPrimitiveObject(method.getDeclaringClass(),
					                                                 position);
					logger.debug("Found callee of type "
					        + method.getDeclaringClass().getName() + ": "
					        + callee.getName());
				} catch (ConstructionFailedException e) {
					logger.debug("No callee of type "
					        + method.getDeclaringClass().getName() + " found");
					Set<AccessibleObject> recursion = new HashSet<AccessibleObject>(
					        currentRecursion);
					callee = attemptGeneration(test, method.getDeclaringClass(),
					                           position, recursionDepth, false);
					currentRecursion = recursion;
					position += test.size() - length;
					length = test.size();
				}
			}
			parameters = satisfyParameters(test,
			                               callee,
			                               Arrays.asList(method.getGenericParameterTypes()),
			                               position, recursionDepth + 1);

		} catch (ConstructionFailedException e) {
			// TODO: Re-insert in new test cluster
			// TestCluster.getInstance().checkDependencies(method);
			throw e;
		}

		int newLength = test.size();
		position += (newLength - length);

		Type retValType = method.getGenericReturnType();

		StatementInterface st = new MethodStatement(test, method, callee, retValType,
		        parameters);
		VariableReference ret = test.addStatement(st, position);
		if (callee != null)
			ret.setDistance(callee.getDistance() + 1);
		return ret;
	}

	/**
	 * Add a call on the method for the given callee at position
	 * 
	 * @param test
	 * @param callee
	 * @param method
	 * @param position
	 * @return
	 * @throws ConstructionFailedException
	 */
	public VariableReference addMethodFor(TestCase test, VariableReference callee,
	        Method method, int position) throws ConstructionFailedException {
		logger.debug("Adding method " + method.toGenericString());
		currentRecursion.clear();
		int length = test.size();
		List<VariableReference> parameters = null;
		parameters = satisfyParameters(test, callee,
		                               Arrays.asList(method.getGenericParameterTypes()),
		                               position, 1);
		int newLength = test.size();
		position += (newLength - length);
		// VariableReference ret_val = new
		// VariableReference(method.getGenericReturnType(), position);

		Type retVal = method.getGenericReturnType();
		StatementInterface st = new MethodStatement(test, method, callee, retVal,
		        parameters);
		VariableReference ret = test.addStatement(st, position);
		ret.setDistance(callee.getDistance() + 1);
		logger.debug("Success: Adding method " + method);
		return ret;
	}

	/**
	 * Add primitive statement at position
	 * 
	 * @param test
	 * @param old
	 * @param position
	 * @return
	 * @throws ConstructionFailedException
	 */
	private VariableReference addPrimitive(TestCase test, PrimitiveStatement<?> old,
	        int position) throws ConstructionFailedException {
		logger.debug("Adding primitive");
		StatementInterface st = old.clone(test);
		return test.addStatement(st, position);
	}

	/**
	 * Append statement s, trying to satisfy parameters
	 * 
	 * Called from TestChromosome when doing crossover
	 * 
	 * @param test
	 * @param s
	 */
	public void appendStatement(TestCase test, StatementInterface statement)
	        throws ConstructionFailedException {
		currentRecursion.clear();

		if (statement instanceof ConstructorStatement) {
			addConstructor(test, ((ConstructorStatement) statement).getConstructor(),
			               test.size(), 0);
		} else if (statement instanceof MethodStatement) {
			addMethod(test, ((MethodStatement) statement).getMethod(), test.size(), 0);
		} else if (statement instanceof PrimitiveStatement<?>) {
			addPrimitive(test, (PrimitiveStatement<?>) statement, test.size());
			// test.statements.add((PrimitiveStatement) statement);
		} else if (statement instanceof FieldStatement) {
			addField(test, ((FieldStatement) statement).field, test.size());
		}
	}

	/**
	 * Assign a value to an array index
	 * 
	 * @param test
	 * @param array
	 * @param arrayIndex
	 * @param position
	 * @throws ConstructionFailedException
	 */
	public void assignArray(TestCase test, VariableReference array, int arrayIndex,
	        int position) throws ConstructionFailedException {
		List<VariableReference> objects = test.getObjects(array.getComponentType(),
		                                                  position);
		Iterator<VariableReference> iterator = objects.iterator();
		// Remove assignments from the same array
		while (iterator.hasNext()) {
			VariableReference var = iterator.next();
			if (var instanceof ArrayIndex && ((ArrayIndex) var).getArray().equals(array))
				iterator.remove();
		}
		assignArray(test, array, arrayIndex, position, objects);
	}

	/**
	 * Assign a value to an array index for a given set of objects
	 * 
	 * @param test
	 * @param array
	 * @param arrayIndex
	 * @param position
	 * @param objects
	 * @throws ConstructionFailedException
	 */
	protected void assignArray(TestCase test, VariableReference array, int arrayIndex,
	        int position, List<VariableReference> objects)
	        throws ConstructionFailedException {
		assert (array instanceof ArrayReference);
		ArrayReference arrRef = (ArrayReference) array;

		if (!objects.isEmpty()
		        && Randomness.nextDouble() <= Properties.OBJECT_REUSE_PROBABILITY) {
			// Assign an existing value
			// TODO:
			// Do we need a special "[Array]AssignmentStatement"?
			logger.debug("Reusing value");

			ArrayIndex index = new ArrayIndex(test, arrRef, arrayIndex);
			StatementInterface st = new AssignmentStatement(test, index,
			        Randomness.choice(objects));
			test.addStatement(st, position);
		} else {
			// Assign a new value
			// Need a primitive, method, constructor, or field statement where
			// retval is set to index
			// Need a version of attemptGeneration that takes retval as
			// parameter

			// OR: Create a new variablereference and then assign it to array
			// (better!)
			int oldLength = test.size();
			logger.debug("Attempting generation of object of type "
			        + array.getComponentType());
			VariableReference var = attemptGeneration(test, array.getComponentType(),
			                                          position);
			position += test.size() - oldLength;
			ArrayIndex index = new ArrayIndex(test, arrRef, arrayIndex);
			StatementInterface st = new AssignmentStatement(test, index, var);
			test.addStatement(st, position);
		}
	}

	/**
	 * Attempt to generate a non-null object; initialize recursion level to 0
	 * 
	 */
	public VariableReference attemptGeneration(TestCase test, Type type, int position)
	        throws ConstructionFailedException {
		return attemptGeneration(test, type, position, 0, false);
	}

	/**
	 * Attempt to generate an object, initialize recursion level to 0, allow
	 * null
	 */
	public VariableReference attemptGenerationOrNull(TestCase test, Type type,
	        int position) throws ConstructionFailedException {
		return attemptGeneration(test, type, position, 0, true);
	}

	/**
	 * Try to generate an object of a given type
	 * 
	 * @param test
	 * @param type
	 * @param position
	 * @param recursionDepth
	 * @param constraint
	 * @param allowNull
	 * @return
	 * @throws ConstructionFailedException
	 */
	protected VariableReference attemptGeneration(TestCase test, Type type, int position,
	        int recursionDepth, boolean allowNull) throws ConstructionFailedException {
		GenericClass clazz = new GenericClass(type);

		if (clazz.isPrimitive() || clazz.isEnum()
		        || clazz.getRawClass().equals(EvoSuiteFile.class)) {
			return createPrimitive(test, type, position, recursionDepth);
		} else if (clazz.isString()) {
			if (allowNull && Randomness.nextDouble() <= Properties.NULL_PROBABILITY) {
				logger.debug("Using a null reference to satisfy the type: " + type);
				return createNull(test, type, position, recursionDepth);
			} else {
				return createPrimitive(test, type, position, recursionDepth);
			}
		} else if (clazz.isArray()) {
			return createArray(test, type, position, recursionDepth);
		} else {
			if (allowNull && Randomness.nextDouble() <= Properties.NULL_PROBABILITY) {
				logger.debug("Using a null reference to satisfy the type: " + type);
				return createNull(test, type, position, recursionDepth);
			}

			ObjectPool objectPool = ObjectPool.getInstance();
			if (Randomness.nextDouble() <= Properties.OBJECT_POOL
			        && objectPool.hasSequence(type)) {
				logger.debug("Using a sequence from the pool to satisfy the type: "
				        + type);
				TestCase sequence = objectPool.getRandomSequence(type);
				logger.info("Old test: " + test.toCode());
				logger.info("Sequence: " + sequence.toCode());
				for (int i = 0; i < sequence.size(); i++) {
					StatementInterface s = sequence.getStatement(i);
					test.addStatement(s.clone(test), position + i);
				}
				logger.info("New test: " + test.toCode());

			}

			return createObject(test, type, position, recursionDepth);
		}
	}

	/**
	 * Try to generate an object suitable for Object.class
	 * 
	 * @param test
	 * @param type
	 * @param position
	 * @param recursionDepth
	 * @param constraint
	 * @param allowNull
	 * @return
	 * @throws ConstructionFailedException
	 */
	protected VariableReference attemptObjectGeneration(TestCase test, int position,
	        int recursionDepth, boolean allowNull) throws ConstructionFailedException {
		if (allowNull && Randomness.nextDouble() <= Properties.NULL_PROBABILITY) {
			logger.debug("Using a null reference to satisfy the type: " + Object.class);
			return createNull(test, Object.class, position, recursionDepth);
		}

		AccessibleObject o = TestCluster.getInstance().getRandomObjectGenerator();
		// LoggingUtils.getEvoLogger().info("Generator for Object: " + o);

		currentRecursion.add(o);
		if (o == null) {
			if (!TestCluster.getInstance().hasGenerator(Object.class)) {
				logger.debug("We have no generator for Object.class ");
			}
			throw new ConstructionFailedException("Generator is null");
		} else if (o instanceof Field) {
			logger.debug("Attempting generating of Object.class via field of type Object.class");
			VariableReference ret = addField(test, (Field) o, position);
			ret.setDistance(recursionDepth + 1);
			logger.debug("Success in generating type Object.class");
			return ret;
		} else if (o instanceof Method) {
			logger.debug("Attempting generating of Object.class via method "
			        + ((Method) o).getName() + " of type Object.class");
			VariableReference ret = addMethod(test, (Method) o, position,
			                                  recursionDepth + 1);
			logger.debug("Success in generating type Object.class");
			ret.setDistance(recursionDepth + 1);
			return ret;
		} else if (o instanceof Constructor<?>) {
			logger.debug("Attempting generating of Object.class via constructor "
			        + ((Constructor<?>) o).getName() + " of type Object.class");
			VariableReference ret = addConstructor(test, (Constructor<?>) o, position,
			                                       recursionDepth + 1);
			logger.debug("Success in generating Object.class");
			ret.setDistance(recursionDepth + 1);

			return ret;
		} else {
			logger.debug("No generators found for Object.class");
			throw new ConstructionFailedException("No generator found for Object.class");
		}

	}

	/**
	 * Replace the statement with a new statement using given call
	 * 
	 * @param test
	 * @param statement
	 * @param call
	 * @throws ConstructionFailedException
	 */
	public void changeCall(TestCase test, StatementInterface statement,
	        AccessibleObject call) throws ConstructionFailedException {
		int position = statement.getReturnValue().getStPosition();

		logger.debug("Changing call " + test.getStatement(position) + " with " + call);

		if (call instanceof Method) {
			Method method = (Method) call;
			VariableReference retval = statement.getReturnValue();
			VariableReference callee = null;
			if (!Modifier.isStatic(method.getModifiers()))
				callee = test.getRandomNonNullNonPrimitiveObject(method.getDeclaringClass(),
				                                                 position);
			List<VariableReference> parameters = new ArrayList<VariableReference>();
			for (Type type : method.getParameterTypes()) {
				parameters.add(test.getRandomObject(type, position));
			}
			MethodStatement m = new MethodStatement(test, method, callee, retval,
			        parameters);
			logger.debug("Using method " + m.getCode());

			test.setStatement(m, position);

		} else if (call instanceof Constructor<?>) {

			Constructor<?> constructor = (Constructor<?>) call;
			VariableReference retval = statement.getReturnValue();
			List<VariableReference> parameters = new ArrayList<VariableReference>();
			for (Type type : constructor.getGenericParameterTypes()) {
				parameters.add(test.getRandomObject(type, position));
			}
			ConstructorStatement c = new ConstructorStatement(test, constructor, retval,
			        parameters);
			logger.debug("Using constructor " + c.getCode());

			test.setStatement(c, position);

		} else if (call instanceof Field) {
			Field field = (Field) call;
			VariableReference retval = statement.getReturnValue();
			VariableReference source = null;
			if (!Modifier.isStatic(field.getModifiers()))
				source = test.getRandomNonNullNonPrimitiveObject(field.getDeclaringClass(),
				                                                 position);

			try {
				FieldStatement f = new FieldStatement(test, field, source, retval);
				logger.debug("Using field " + f.getCode());

				test.setStatement(f, position);
			} catch (Throwable e) {
				logger.error("Error: " + e + " , Field: " + field + " , Test: " + test);
				throw new Error(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.AbstractTestFactory#changeRandomCall(de.unisb.cs.st.evosuite.testcase.TestCase, de.unisb.cs.st.evosuite.testcase.StatementInterface)
	 */
	public boolean changeRandomCall(TestCase test, StatementInterface statement) {
		logger.debug("Changing statement ", statement.getCode());
		//+ " in test "
		List<VariableReference> objects = test.getObjects(statement.getReturnValue().getStPosition());
		objects.remove(statement.getReturnValue());
		// TODO: replacing void calls with other void calls might not be the best idea
		List<AccessibleObject> calls = getPossibleCalls(statement.getReturnType(),
		                                                objects);

		AccessibleObject ao = statement.getAccessibleObject();
		if (ao != null)
			calls.remove(ao);

		logger.debug("Got " + calls.size() + " possible calls for " + objects.size()
		        + " objects");
		AccessibleObject call = Randomness.choice(calls);
		try {
			if (statement instanceof TestCallStatement)
				logger.info("Changing testcall statement");
			changeCall(test, statement, call);
			//logger.debug("Changed to: " + test.toCode());

			return true;
		} catch (ConstructionFailedException e) {
			// Ignore
			logger.info("Change failed for statement " + statement.getCode() + " -> "
			        + call + ": " + e.getMessage() + " " + test.toCode());
		}
		return false;
	}

	/**
	 * Create a new array in a test case and return the reference
	 * 
	 * @param test
	 * @param type
	 * @param position
	 * @param recursionDepth
	 * @return
	 * @throws ConstructionFailedException
	 */
	private VariableReference createArray(TestCase test, Type type, int position,
	        int recursionDepth) throws ConstructionFailedException {

		logger.debug("Creating array of type " + type);
		// Create array with random size
		ArrayStatement statement = new ArrayStatement(test, type);
		VariableReference reference = test.addStatement(statement, position);
		position++;
		logger.debug("Array length: " + statement.size());

		// For each value of array, call attemptGeneration
		List<VariableReference> objects = test.getObjects(reference.getComponentType(),
		                                                  position);

		// Don't assign values to other values in the same array initially
		Iterator<VariableReference> iterator = objects.iterator();
		while (iterator.hasNext()) {
			VariableReference current = iterator.next();
			if (current instanceof ArrayIndex) {
				ArrayIndex index = (ArrayIndex) current;
				if (index.getArray().equals(statement.retval))
					iterator.remove();
			}
		}
		objects.remove(statement.retval);
		logger.debug("Found assignable objects: " + objects.size());
		for (int i = 0; i < statement.size(); i++) {
			logger.debug("Assigning array index " + i);
			int oldLength = test.size();
			assignArray(test, reference, i, position, objects);
			position += test.size() - oldLength;
		}
		reference.setDistance(recursionDepth);
		return reference;
	}

	/**
	 * Create and return a new primitive variable
	 * 
	 * @param test
	 * @param type
	 * @param position
	 * @param recursionDepth
	 * @return
	 */
	private VariableReference createPrimitive(TestCase test, Type type, int position,
	        int recursionDepth) {
		StatementInterface st = PrimitiveStatement.getRandomStatement(test, type,
		                                                              position, type);
		VariableReference ret = test.addStatement(st, position);
		ret.setDistance(recursionDepth);
		return ret;
	}

	/**
	 * Create and return a new null variable
	 * 
	 * @param test
	 * @param type
	 * @param position
	 * @param recursionDepth
	 * @return
	 */
	private VariableReference createNull(TestCase test, Type type, int position,
	        int recursionDepth) {
		StatementInterface st = new NullStatement(test, type);
		test.addStatement(st, position);
		VariableReference ret = test.getStatement(position).getReturnValue();
		ret.setDistance(recursionDepth);
		return ret;
	}

	/**
	 * Create a new non-null, non-primitive object and return reference
	 * 
	 * @param test
	 * @param type
	 * @param position
	 * @param recursionDepth
	 * @return
	 * @throws ConstructionFailedException
	 */
	public VariableReference createObject(TestCase test, Type type, int position,
	        int recursionDepth) throws ConstructionFailedException {
		GenericClass clazz = new GenericClass(type);
		AccessibleObject o = TestCluster.getInstance().getRandomGenerator(clazz,
		                                                                  currentRecursion);

		currentRecursion.add(o);
		if (o == null) {
			if (!TestCluster.getInstance().hasGenerator(clazz)) {
				logger.debug("We have no generator for class " + type);
			}
			throw new ConstructionFailedException("Generator is null");
		} else if (o instanceof Field) {
			logger.debug("Attempting generating of " + type + " via field of type "
			        + type);
			VariableReference ret = addField(test, (Field) o, position);
			ret.setDistance(recursionDepth + 1);
			logger.debug("Success in generating type " + type);
			return ret;
		} else if (o instanceof Method) {
			logger.debug("Attempting generating of " + type + " via method "
			        + ((Method) o).getName() + " of type " + type);
			VariableReference ret = addMethod(test, (Method) o, position,
			                                  recursionDepth + 1);
			logger.debug("Success in generating type " + type);
			ret.setDistance(recursionDepth + 1);
			return ret;
		} else if (o instanceof Constructor<?>) {
			logger.debug("Attempting generating of " + type + " via constructor "
			        + ((Constructor<?>) o).getName() + " of type " + type);
			VariableReference ret = addConstructor(test, (Constructor<?>) o, position,
			                                       recursionDepth + 1);
			logger.debug("Success in generating type " + type);
			ret.setDistance(recursionDepth + 1);

			return ret;
		} else {
			logger.debug("No generators found for type " + type);
			throw new ConstructionFailedException("No generator found for type " + type);
		}
	}

	/**
	 * Create a new variable or reuse and existing one
	 * 
	 * @param test
	 * @param parameterType
	 * @param position
	 * @param recursionDepth
	 * @param exclude
	 * @return
	 * @throws ConstructionFailedException
	 */
	private VariableReference createOrReuseVariable(TestCase test, Type parameterType,
	        int position, int recursionDepth, VariableReference exclude)
	        throws ConstructionFailedException {

		if (parameterType.equals(Object.class)) {
			return createOrReuseObjectVariable(test, position, recursionDepth, exclude);
		}

		double reuse = Randomness.nextDouble();

		List<VariableReference> objects = test.getObjects(parameterType, position);
		if (exclude != null) {
			objects.remove(exclude);
			if (exclude.getAdditionalVariableReference() != null)
				objects.remove(exclude.getAdditionalVariableReference());
			Iterator<VariableReference> it = objects.iterator();
			while (it.hasNext()) {
				VariableReference v = it.next();
				if (exclude.equals(v.getAdditionalVariableReference()))
					it.remove();
			}
		}

		GenericClass clazz = new GenericClass(parameterType);
		if ((clazz.isPrimitive() || clazz.isEnum()) && !objects.isEmpty()
		        && reuse <= Properties.PRIMITIVE_REUSE_PROBABILITY) {
			logger.debug(" Looking for existing object of type " + parameterType);
			VariableReference reference = Randomness.choice(objects);
			return reference;

		} else if (!clazz.isPrimitive()
		        && !clazz.isEnum()
		        && !objects.isEmpty()
		        && ((reuse <= Properties.OBJECT_REUSE_PROBABILITY) || !TestCluster.getInstance().hasGenerator(parameterType))) {

			logger.debug(" Choosing from " + objects.size() + " existing objects");
			VariableReference reference = Randomness.choice(objects);
			logger.debug(" Using existing object of type " + parameterType + ": "
			        + reference);
			return reference;

		} else {
			logger.debug(" Generating new object of type " + parameterType);
			VariableReference reference = attemptGeneration(test, parameterType,
			                                                position, recursionDepth,
			                                                true);
			return reference;
		}
	}

	/**
	 * Create or reuse a variable that can be assigned to Object.class
	 * 
	 * @param test
	 * @param position
	 * @param recursionDepth
	 * @param exclude
	 * @return
	 */
	private VariableReference createOrReuseObjectVariable(TestCase test, int position,
	        int recursionDepth, VariableReference exclude)
	        throws ConstructionFailedException {
		double reuse = Randomness.nextDouble();

		// Only reuse objects if they are related to a target call
		if (reuse <= Properties.OBJECT_REUSE_PROBABILITY) {

			List<VariableReference> candidates = test.getObjects(Object.class, position);
			filterVariablesByClass(candidates, Object.class);

			if (!candidates.isEmpty())
				return Randomness.choice(candidates);
		}

		return attemptObjectGeneration(test, position, recursionDepth, true);

	}

	/**
	 * Delete the statement at position from the test case and remove all
	 * references to it
	 * 
	 * @param test
	 * @param position
	 * @throws ConstructionFailedException
	 */
	public void deleteStatement(TestCase test, int position)
	        throws ConstructionFailedException {
		logger.debug("Deleting target statement - " + position);
		//logger.info(test.toCode());

		Set<VariableReference> references = new LinkedHashSet<VariableReference>();
		Set<Integer> positions = new LinkedHashSet<Integer>();
		positions.add(position);
		references.add(test.getReturnValue(position));
		for (int i = position; i < test.size(); i++) {
			Set<VariableReference> temp = new LinkedHashSet<VariableReference>();
			for (VariableReference v : references) {
				if (test.getStatement(i).references(v)) {
					temp.add(test.getStatement(i).getReturnValue());
					positions.add(i);
				}
			}
			references.addAll(temp);
		}
		List<Integer> pos = new ArrayList<Integer>(positions);
		Collections.sort(pos, Collections.reverseOrder());
		for (Integer i : pos) {
			logger.debug("Deleting statement: " + i);
			test.remove(i);
		}
	}

	private static void filterVariablesByClass(Collection<VariableReference> variables,
	        Class<?> clazz) {
		// Remove invalid classes if this is an Object.class reference
		Iterator<VariableReference> replacement = variables.iterator();
		while (replacement.hasNext()) {
			VariableReference r = replacement.next();
			if (!r.getVariableClass().equals(clazz))
				replacement.remove();
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.AbstractTestFactory#deleteStatementGracefully(de.unisb.cs.st.evosuite.testcase.TestCase, int)
	 */
	public void deleteStatementGracefully(TestCase test, int position)
	        throws ConstructionFailedException {
		VariableReference var = test.getReturnValue(position);
		if (var instanceof ArrayIndex) {
			deleteStatement(test, position);
			return;
		}

		boolean replacingPrimitive = test.getStatement(position) instanceof PrimitiveStatement;

		// Get possible replacements
		List<VariableReference> alternatives = test.getObjects(var.getType(), position);

		int maxIndex = 0;
		if (var instanceof ArrayReference) {
			maxIndex = ((ArrayReference) var).getMaximumIndex();
		}

		// Remove invalid classes if this is an Object.class reference
		if (test.getStatement(position) instanceof MethodStatement) {
			MethodStatement ms = (MethodStatement) test.getStatement(position);
			if (ms.getReturnClass().equals(Object.class)) {
				//				filterVariablesByClass(alternatives, var.getVariableClass());
				filterVariablesByClass(alternatives, Object.class);
			}
		} else if (test.getStatement(position) instanceof ConstructorStatement) {
			ConstructorStatement cs = (ConstructorStatement) test.getStatement(position);
			if (cs.getReturnClass().equals(Object.class)) {
				filterVariablesByClass(alternatives, Object.class);
			}
		}

		// Remove self, and all field or array references to self
		alternatives.remove(var);
		Iterator<VariableReference> replacement = alternatives.iterator();
		while (replacement.hasNext()) {
			VariableReference r = replacement.next();
			if (var.equals(r.getAdditionalVariableReference()))
				replacement.remove();
			else if (r instanceof ArrayReference) {
				if (maxIndex >= ((ArrayReference) r).getArrayLength())
					replacement.remove();
			} else if (!replacingPrimitive) {
				if (test.getStatement(r.getStPosition()) instanceof PrimitiveStatement) {
					replacement.remove();
				}
			}
		}

		if (!alternatives.isEmpty()) {
			// Change all references to return value at position to something
			// else
			for (int i = position + 1; i < test.size(); i++) {
				StatementInterface s = test.getStatement(i);
				if (s.references(var)) {
					s.replace(var, Randomness.choice(alternatives));
				}
			}
		}

		if (var instanceof ArrayReference) {
			alternatives = test.getObjects(var.getComponentType(), position);
			// Remove self, and all field or array references to self
			alternatives.remove(var);
			replacement = alternatives.iterator();
			while (replacement.hasNext()) {
				VariableReference r = replacement.next();
				if (var.equals(r.getAdditionalVariableReference()))
					replacement.remove();
			}
			if (!alternatives.isEmpty()) {
				// Change all references to return value at position to something
				// else
				for (int i = position; i < test.size(); i++) {
					StatementInterface s = test.getStatement(i);
					for (VariableReference var2 : s.getVariableReferences()) {
						if (var2 instanceof ArrayIndex) {
							ArrayIndex ai = (ArrayIndex) var2;
							if (ai.getArray().equals(var))
								s.replace(var2, Randomness.choice(alternatives));
						}
					}
				}
			}
		}

		// Remove everything else
		deleteStatement(test, position);
	}

	/**
	 * Determine if the set of objects is sufficient to satisfy the set of
	 * dependencies
	 * 
	 * @param dependencies
	 * @param objects
	 * @return
	 */
	private static boolean dependenciesSatisfied(Set<Type> dependencies,
	        List<VariableReference> objects) {
		for (Type type : dependencies) {
			boolean found = false;
			for (VariableReference var : objects) {
				if (var.getType().equals(type)) {
					found = true;
					break;
				}
			}
			if (!found)
				return false;
		}
		return true;
	}

	/**
	 * Retrieve the dependencies for a constructor
	 * 
	 * @param constructor
	 * @return
	 */
	private static Set<Type> getDependencies(Constructor<?> constructor) {
		Set<Type> dependencies = new LinkedHashSet<Type>();
		for (Type type : constructor.getGenericParameterTypes()) {
			dependencies.add(type);
		}

		return dependencies;
	}

	/**
	 * Retrieve the dependencies for a field
	 * 
	 * @param field
	 * @return
	 */
	private static Set<Type> getDependencies(Field field) {
		Set<Type> dependencies = new LinkedHashSet<Type>();
		if (!Modifier.isStatic(field.getModifiers())) {
			dependencies.add(field.getDeclaringClass());
		}

		return dependencies;
	}

	/**
	 * Retrieve the dependencies for a method
	 * 
	 * @param method
	 * @return
	 */
	private static Set<Type> getDependencies(Method method) {
		Set<Type> dependencies = new LinkedHashSet<Type>();
		if (!Modifier.isStatic(method.getModifiers())) {
			dependencies.add(method.getDeclaringClass());
		}
		for (Type type : method.getGenericParameterTypes()) {
			dependencies.add(type);
		}

		return dependencies;
	}

	/**
	 * Retrieve all the replacement calls that can be inserted at this position
	 * without changing the length
	 * 
	 * @param returnType
	 * @param objects
	 * @return
	 */
	private List<AccessibleObject> getPossibleCalls(Type returnType,
	        List<VariableReference> objects) {
		List<AccessibleObject> calls = new ArrayList<AccessibleObject>();
		Set<AccessibleObject> allCalls;

		try {
			allCalls = TestCluster.getInstance().getGenerators(new GenericClass(
			                                                           returnType));
		} catch (ConstructionFailedException e) {
			return calls;
		}

		for (AccessibleObject call : allCalls) {
			Set<Type> dependencies = null;
			if (call instanceof Method) {
				if (!((Method) call).getReturnType().equals(returnType))
					continue;
				dependencies = getDependencies((Method) call);
			} else if (call instanceof Constructor<?>) {
				dependencies = getDependencies((Constructor<?>) call);
			} else if (call instanceof Field) {
				if (!((Field) call).getType().equals(returnType))
					continue;
				dependencies = getDependencies((Field) call);
			} else {
				assert (false);
			}
			if (dependenciesSatisfied(dependencies, objects)) {
				calls.add(call);
			}
		}

		// TODO: What if primitive?

		return calls;
	}

	/**
	 * Insert a random call at given position
	 * 
	 * @param test
	 * @param position
	 */
	public boolean insertRandomCall(TestCase test, int position) {
		int previousLength = test.size();
		String name = "";
		currentRecursion.clear();
		logger.debug("Inserting random call at position " + position);
		AccessibleObject o = TestCluster.getInstance().getRandomTestCall();
		try {
			if (o == null) {
				logger.warn("Have no target methods to test");
			} else if (o instanceof Constructor<?>) {
				Constructor<?> c = (Constructor<?>) o;
				//logger.info("Adding constructor call " + c.getName());
				name = c.getName();
				addConstructor(test, c, position, 0);
			} else if (o instanceof Method) {
				Method m = (Method) o;
				//logger.info("Adding method call " + m.getName());
				name = m.getName();
				if(!Modifier.isStatic(m.getModifiers())) {
					VariableReference callee = null;
					if(!test.hasObject(Properties.getTargetClass(), position)) {
						callee = createObject(test, Properties.getTargetClass(), position, 0);
						position += test.size() - previousLength;
						previousLength = test.size();
					} else {
						callee = test.getRandomNonNullObject(Properties.getTargetClass(),
								position);
						// This may also be an inner class, in this case we can't use a SUT instance
						if (!callee.isAssignableTo(m.getDeclaringClass())) {
							callee = test.getRandomNonNullObject(m.getDeclaringClass(), position);
						}
					}
					addMethodFor(test, callee, m, position);
				} else {
					// We only use this for static methods to avoid using wrong constructors (?)
					addMethod(test, m, position, 0);
				}
			} else if (o instanceof Field) {
				Field f = (Field) o;
				name = f.getName();
				if(Randomness.nextBoolean()) {
					//logger.info("Adding field assignment " + f.getName());
					addFieldAssignment(test, f, position, 0);
				} else {
					//logger.info("Adding field " + f.getName());
					addField(test, f, position);
				}
			} else {
				logger.error("Got type other than method or constructor!");
			}
			
			return true;
		} catch (ConstructionFailedException e) {
			// TODO: Check this! - TestCluster replaced
			// TestCluster.getInstance().checkDependencies(o);
			logger.debug("Inserting statement " + name
			        + " has failed. Removing statements: " + e);
			// System.out.println("TG: Failed");
			// TODO: Doesn't work if position != test.size()
			int lengthDifference = test.size() - previousLength;
			for (int i = lengthDifference - 1; i >= 0; i--) { //we need to remove them in order, so that the testcase is at all time consistent 
				logger.debug("  Removing statement: "
				        + test.getStatement(position + i).getCode());
				test.remove(position + i);
			}
			return false;

			// logger.info("Attempting search");
			// test.chop(previous_length);
		}
	}

	/**
	 * Insert a random call at given position for an object defined before this
	 * position
	 * 
	 * @param test
	 * @param position
	 */
	public boolean insertRandomCallOnObject(TestCase test, int position) {
		// Select a random variable
		VariableReference var = selectVariableForCall(test, position);

		// Add call for this variable at random position
		if (var != null) {
			logger.debug("Inserting call at position " + position + ", chosen var: "
			        + var.getName() + ", distance: " + var.getDistance() + ", class: "
			        + var.getClassName());
			return insertRandomCallOnObjectAt(test, var, position);
		} else {
			logger.debug("Adding new call on UUT");
			return insertRandomCall(test, position);
		}
	}

	public boolean insertRandomCallOnObjectAt(TestCase test, VariableReference var,
	        int position) {
		// Select a random variable
		logger.debug("Chosen object: " + var.getName());
		if (var instanceof ArrayReference) {
			logger.debug("Chosen object is array ");
			ArrayReference array = (ArrayReference) var;
			if (array.getArrayLength() > 0) {
				for (int i = 0; i < array.getArrayLength(); i++) {
					int old_len = test.size();
					try {
						assignArray(test, array, i, position);
						position += test.size() - old_len;
					} catch (ConstructionFailedException e) {

					}
				}
				/*
				int index = Randomness.nextInt(array.getArrayLength());
				try {
					logger.info("Assigning new value to array at position " + index);
					assignArray(test, var, index, position);
				} catch (ConstructionFailedException e) {
					// logger.info("Failed!");
				}
				*/
				return true;
			}
		} else {
			logger.debug("Getting calls for object " + var.toString());
			Set<AccessibleObject> calls = TestCluster.getInstance().getCallsFor(var.getVariableClass());
			if (!calls.isEmpty()) {
				AccessibleObject call = Randomness.choice(calls);
				logger.debug("Chosen call " + call);
				return addCallFor(test, var, call, position);
				// logger.debug("Done adding call " + call);
			}

		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.AbstractTestFactory#insertRandomStatement(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	public int insertRandomStatement(TestCase test) {
		final double P = Properties.INSERTION_SCORE_UUT
		        + Properties.INSERTION_SCORE_OBJECT
		        + Properties.INSERTION_SCORE_PARAMETER;
		final double P_UUT = Properties.INSERTION_SCORE_UUT / P;
		final double P_OBJECT = P_UUT + Properties.INSERTION_SCORE_OBJECT / P;

		double r = Randomness.nextDouble();
		int position = Randomness.nextInt(test.size() + 1);

		for (int i = 0; i < test.size(); i++) {
			logger.debug(test.getStatement(i).getCode() + ": Distance = "
			        + test.getStatement(i).getReturnValue().getDistance());
		}

		//		if (r <= P_UUT) {
		boolean success = false;
		if (r <= 0.5) {
			// add new call of the UUT - only declared in UUT!
			logger.debug("Adding new call on UUT");
			success = insertRandomCall(test, position);
		} else { // if (r <= P_OBJECT) {
			logger.debug("Adding new call on existing object");
			success = insertRandomCallOnObject(test, position);
			//		} else {
			//			logger.debug("Adding new call with existing object as parameter");
			// insertRandomCallWithObject(test, position);
		}
		if(success)
			return position;
		else
			return -1;
	}

	/**
	 * Satisfy a list of parameters by reusing or creating variables
	 * 
	 * @param test
	 * @param parameterTypes
	 * @param position
	 * @param recursionDepth
	 * @param constraints
	 * @return
	 * @throws ConstructionFailedException
	 */
	private List<VariableReference> satisfyParameters(TestCase test,
	        VariableReference callee, List<Type> parameterTypes, int position,
	        int recursionDepth) throws ConstructionFailedException {
		List<VariableReference> parameters = new ArrayList<VariableReference>();
		logger.debug("Trying to satisfy " + parameterTypes.size() + " parameters");
		for (Type parameterType : parameterTypes) {
			int previousLength = test.size();

			VariableReference var = createOrReuseVariable(test, parameterType, position,
			                                              recursionDepth, callee);
			parameters.add(var);

			int currentLength = test.size();
			position += currentLength - previousLength;
		}
		logger.debug("Satisfied " + parameterTypes.size() + " parameters");
		return parameters;
	}

	/**
	 * Randomly select one of the variables in the test defined up to position
	 * to insert a call for
	 * 
	 * @param test
	 * @param position
	 * @return
	 */
	private VariableReference selectVariableForCall(TestCase test, int position) {
		if (test.isEmpty() || position == 0)
			return null;

		double sum = 0.0;
		for (int i = 0; i < position; i++) {
			sum += 1d / (10 * test.getStatement(i).getReturnValue().getDistance() + 1d);
			logger.debug(test.getStatement(i).getCode() + ": Distance = "
			        + test.getStatement(i).getReturnValue().getDistance());
		}

		double rnd = Randomness.nextDouble() * sum;

		for (int i = 0; i < position; i++) {
			double dist = 1d / (test.getStatement(i).getReturnValue().getDistance() + 1d);

			if (dist >= rnd
			        && !(test.getStatement(i).getReturnValue() instanceof NullReference)
			        && !(test.getStatement(i).getReturnValue().isPrimitive())
			        && !(test.getStatement(i).getReturnValue().isVoid())
			        && !(test.getStatement(i) instanceof PrimitiveStatement))
				return test.getStatement(i).getReturnValue();
			else
				rnd = rnd - dist;
		}

		if (position > 0)
			position = Randomness.nextInt(position);

		VariableReference var = test.getStatement(position).getReturnValue();
		if (!(var instanceof NullReference) && !var.isVoid()
		        && !(test.getStatement(position) instanceof PrimitiveStatement)
		        && !var.isPrimitive())
			return var;
		else
			return null;
	}

}
