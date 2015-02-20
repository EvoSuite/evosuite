/**
 * 
 */
package org.evosuite.testcase;

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

import org.apache.commons.lang3.ClassUtils;
import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.seeding.CastClassManager;
import org.evosuite.seeding.ObjectPoolManager;
import org.evosuite.setup.TestCluster;
import org.evosuite.setup.TestClusterGenerator;
import org.evosuite.testcase.statements.environment.EnvironmentStatements;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.FieldStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.NullStatement;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.utils.GenericAccessibleObject;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericConstructor;
import org.evosuite.utils.GenericField;
import org.evosuite.utils.GenericMethod;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.gentyref.CaptureType;
import com.googlecode.gentyref.GenericTypeReflector;

/**
 * @author Gordon Fraser
 * 
 */
public class TestFactory {

	private static final Logger logger = LoggerFactory.getLogger(TestFactory.class);

	/**
	 * Keep track of objects we are already trying to generate to avoid cycles
	 */
	private transient Set<GenericAccessibleObject<?>> currentRecursion = new HashSet<GenericAccessibleObject<?>>();

	/** Singleton instance */
	private static TestFactory instance = null;

	/**
	 * We keep track of calls already attempted to avoid infinite recursion
	 */
	public void reset() {
		currentRecursion.clear();
	}

	public static TestFactory getInstance() {
		if (instance == null)
			instance = new TestFactory();
		return instance;
	}

	/**
	 * Append given call to the test case at given position
	 * 
	 * @param test
	 * @param call
	 * @param position
	 */
	private boolean addCallFor(TestCase test, VariableReference callee,
	        GenericAccessibleObject<?> call, int position) {
		logger.trace("addCallFor " + callee.getName());

		int previousLength = test.size();
		currentRecursion.clear();
		try {
			if (call.isMethod()) {
				addMethodFor(test,
				             callee,
				             (GenericMethod) call.copyWithNewOwner(callee.getGenericClass()),
				             position);
			} else if (call.isField()) {
				addFieldFor(test,
				            callee,
				            (GenericField) call.copyWithNewOwner(callee.getGenericClass()),
				            position);
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
			logger.debug("Test after removal: " + test.toCode());
			return false;
		}
	}

	public VariableReference addConstructor(TestCase test,
	        GenericConstructor constructor, int position, int recursionDepth)
	        throws ConstructionFailedException {
		return addConstructor(test, constructor, null, position, recursionDepth);
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
	public VariableReference addConstructor(TestCase test,
	        GenericConstructor constructor, Type exactType, int position,
	        int recursionDepth) throws ConstructionFailedException {
		if (recursionDepth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException("Max recursion depth reached");
		}

		int length = test.size();

		try {
			List<VariableReference> parameters = satisfyParameters(test,
			                                                       null,
			                                                       Arrays.asList(constructor.getParameterTypes()),
			                                                       position,
			                                                       recursionDepth + 1);
			int newLength = test.size();
			position += (newLength - length);

			Statement st = new ConstructorStatement(test, constructor,
			        parameters);
			return test.addStatement(st, position);
		} catch (Exception e) {
			throw new ConstructionFailedException(e.getMessage());
		}
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
	public VariableReference addField(TestCase test, GenericField field, int position,
	        int recursionDepth) throws ConstructionFailedException {
		logger.debug("Adding field " + field);
		if (recursionDepth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException("Max recursion depth reached");
		}

		VariableReference callee = null;
		int length = test.size();

		if (!field.isStatic()) {
			callee = createOrReuseVariable(test, field.getOwnerType(), position,
					recursionDepth, null, false);
			position += test.size() - length;
			length = test.size();

			if (!TestClusterGenerator.canUse(field.getField(),
					callee.getVariableClass())) {
				logger.debug("Cannot call field " + field + " with callee of type "
						+ callee.getClassName());
				throw new ConstructionFailedException(
						"Cannot apply field to this callee");
			}

			// TODO: Check if field is still accessible in subclass
			if(!field.getOwnerClass().equals(callee.getGenericClass())) {
				try {
					if(!TestClusterGenerator.canUse(callee.getVariableClass().getField(field.getName()))) {
						throw new ConstructionFailedException("Cannot access field in subclass");
					}
				} catch(NoSuchFieldException fe) {
					throw new ConstructionFailedException("Cannot access field in subclass");
				}
			}
		}

		Statement st = new FieldStatement(test, field, callee);

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
	public VariableReference addFieldAssignment(TestCase test, GenericField field,
	        int position, int recursionDepth) throws ConstructionFailedException {
		logger.debug("Recursion depth: " + recursionDepth);
		if (recursionDepth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException("Max recursion depth reached");
		}
		logger.debug("Adding field " + field);

		int length = test.size();
		VariableReference callee = null;
		if (!field.isStatic()) { 
			callee = createOrReuseVariable(test, field.getOwnerType(), position,
					recursionDepth, null, false);
			position += test.size() - length;
			length = test.size();
			if (!TestClusterGenerator.canUse(field.getField(), callee.getVariableClass())) {
				logger.debug("Cannot call field " + field + " with callee of type "
				        + callee.getClassName());
				throw new ConstructionFailedException("Cannot apply field to this callee");
			}

		}

		VariableReference var = createOrReuseVariable(test, field.getFieldType(),
		                                              position, recursionDepth, callee, true);
		int newLength = test.size();
		position += (newLength - length);

		FieldReference f = new FieldReference(test, field, callee);
		if (f.equals(var))
			throw new ConstructionFailedException("Self assignment");

		Statement st = new AssignmentStatement(test, f, var);
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
	        GenericField field, int position) throws ConstructionFailedException {
		logger.debug("Adding field " + field + " for variable " + callee);
		if(position <= callee.getStPosition())
			throw new ConstructionFailedException("Cannot insert call on object before the object is defined");

		currentRecursion.clear();

		FieldReference fieldVar = new FieldReference(test, field, callee);
		int length = test.size();
		VariableReference value = createOrReuseVariable(test, fieldVar.getType(),
		                                                position, 0, callee, true);

		int newLength = test.size();
		position += (newLength - length);

		Statement st = new AssignmentStatement(test, fieldVar, value);
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
	public VariableReference addMethod(TestCase test, GenericMethod method, int position,
	        int recursionDepth) throws ConstructionFailedException {

		logger.debug("Recursion depth: " + recursionDepth);
		if (recursionDepth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException("Max recursion depth reached");
		}
		logger.debug("Adding method " + method);
		int length = test.size();
		VariableReference callee = null;
		List<VariableReference> parameters = null;
		try {
			if (!method.isStatic()) {
				callee = createOrReuseVariable(test, method.getOwnerType(), position,
						recursionDepth, null, false);
				position += test.size() - length;
				length = test.size();

				logger.debug("Found callee of type " + method.getOwnerType() + ": "
						+ callee.getName());
				if (!TestClusterGenerator.canUse(method.getMethod(),
						callee.getVariableClass())) {
					logger.debug("Cannot call method " + method
							+ " with callee of type " + callee.getClassName());
					throw new ConstructionFailedException(
							"Cannot apply method to this callee");
				}
			}

			parameters = satisfyParameters(test, callee,
			                               Arrays.asList(method.getParameterTypes()),
			                               position, recursionDepth + 1);

		} catch (ConstructionFailedException e) {
			// TODO: Re-insert in new test cluster
			// TestCluster.getInstance().checkDependencies(method);
			throw e;
		}

		int newLength = test.size();
		position += (newLength - length);

		Statement st = new MethodStatement(test, method, callee, parameters);
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
	        GenericMethod method, int position) throws ConstructionFailedException {
		logger.debug("Adding method " + method + " for " + callee + "(Generating "+method.getGeneratedClass()+")");
		if(position <= callee.getStPosition())
			throw new ConstructionFailedException("Cannot insert call on object before the object is defined");
		
		currentRecursion.clear();
		int length = test.size();
		List<VariableReference> parameters = null;
		parameters = satisfyParameters(test, callee,
		                               Arrays.asList(method.getParameterTypes()),
		                               position, 1);

		int newLength = test.size();
		position += (newLength - length);

		Statement st = new MethodStatement(test, method, callee, parameters);
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
		Statement st = old.clone(test);
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
	public void appendStatement(TestCase test, Statement statement)
	        throws ConstructionFailedException {
		currentRecursion.clear();

		if (statement instanceof ConstructorStatement) {
			addConstructor(test, ((ConstructorStatement) statement).getConstructor(),
			               test.size(), 0);
		} else if (statement instanceof MethodStatement) {
			GenericMethod method = ((MethodStatement) statement).getMethod();
			addMethod(test, method, test.size(), 0);
		} else if (statement instanceof PrimitiveStatement<?>) {
			addPrimitive(test, (PrimitiveStatement<?>) statement, test.size());
			// test.statements.add((PrimitiveStatement) statement);
		} else if (statement instanceof FieldStatement) {
			addField(test, ((FieldStatement) statement).getField(), test.size(), 0);
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
		GenericClass componentClass = new GenericClass(array.getComponentType());
		// Remove assignments from the same array
		while (iterator.hasNext()) {
			VariableReference var = iterator.next();
			if (var instanceof ArrayIndex) {
				if (((ArrayIndex) var).getArray().equals(array))
					iterator.remove();
				// Do not assign values of same type as array to elements
				// This may e.g. happen if we have Object[], we could otherwise assign Object[] as values
				else if (((ArrayIndex) var).getArray().getType().equals(array.getType()))
					iterator.remove();
			}
			if (componentClass.isWrapperType()) {
				Class<?> rawClass = ClassUtils.wrapperToPrimitive(componentClass.getRawClass());
				if (!var.getVariableClass().equals(rawClass)
				        && !var.getVariableClass().equals(componentClass.getRawClass())) {
					iterator.remove();
				}
			}

		}
		logger.debug("Reusable objects: " + objects);
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
			VariableReference choice = Randomness.choice(objects);
			logger.debug("Reusing value: " + choice);

			ArrayIndex index = new ArrayIndex(test, arrRef, arrayIndex);
			Statement st = new AssignmentStatement(test, index, choice);
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
			// Generics instantiation may lead to invalid types, so better double check
			if(!var.isAssignableTo(arrRef.getComponentType())) {
				throw new ConstructionFailedException("Error");
			}

			position += test.size() - oldLength;
			ArrayIndex index = new ArrayIndex(test, arrRef, arrayIndex);
			Statement st = new AssignmentStatement(test, index, var);
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
	 * @param allowNull
	 * @return
	 * @throws ConstructionFailedException
	 */
	protected VariableReference attemptGeneration(TestCase test, Type type, int position,
	        int recursionDepth, boolean allowNull) throws ConstructionFailedException {
		GenericClass clazz = new GenericClass(type);

		if (clazz.isEnum()) {
			if (!TestClusterGenerator.canUse(clazz.getRawClass()))
				throw new ConstructionFailedException(
				        "Cannot generate unaccessible enum " + clazz);
			return createPrimitive(test, clazz, position, recursionDepth);
		} else if (clazz.isPrimitive() || clazz.isClass()
		        || EnvironmentStatements.isEnvironmentData( clazz.getRawClass())) {
			return createPrimitive(test, clazz, position, recursionDepth);
		} else if (clazz.isString()) {
			if (allowNull && Randomness.nextDouble() <= Properties.NULL_PROBABILITY) {
				logger.debug("Using a null reference to satisfy the type: " + type);
				return createNull(test, type, position, recursionDepth);
			} else {
				return createPrimitive(test, clazz, position, recursionDepth);
			}
		} else if (clazz.isArray()) {
			return createArray(test, clazz, position, recursionDepth);
		} else {
			if (allowNull && Randomness.nextDouble() <= Properties.NULL_PROBABILITY) {
				logger.debug("Using a null reference to satisfy the type: " + type);
				return createNull(test, type, position, recursionDepth);
			}

			ObjectPoolManager objectPool = ObjectPoolManager.getInstance();
			if (Randomness.nextDouble() <= Properties.P_OBJECT_POOL
			        && objectPool.hasSequence(clazz)) {
				TestCase sequence = objectPool.getRandomSequence(clazz);
				logger.debug("Using a sequence from the object pool to satisfy the type: "
				        + type);
				VariableReference targetObject = sequence.getLastObject(type);
				int returnPos = position + targetObject.getStPosition();
				for (int i = 0; i < sequence.size(); i++) {
					Statement s = sequence.getStatement(i);
					test.addStatement(s.copy(test, position), position + i);
				}
				logger.debug("Return type of object sequence: "
				        + test.getStatement(returnPos).getReturnValue().getClassName());
				return test.getStatement(returnPos).getReturnValue();
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
		
		List<GenericClass> classes = new ArrayList<GenericClass>(
		        CastClassManager.getInstance().getCastClasses());
		classes.add(new GenericClass(Object.class));
		GenericClass choice = Randomness.choice(classes);
		logger.debug("Chosen class for Object: "+choice);
		if(choice.isString()) {
			return createOrReuseVariable(test, String.class, position,
                    recursionDepth, null, true);			
		}
		GenericAccessibleObject<?> o = TestCluster.getInstance().getRandomGenerator(choice);
		// LoggingUtils.getEvoLogger().info("Generator for Object: " + o);

		currentRecursion.add(o);
		if (o == null) {
			if (!TestCluster.getInstance().hasGenerator(Object.class)) {
				logger.debug("We have no generator for Object.class ");
			}
			throw new ConstructionFailedException("Generator is null");
		} else if (o.isField()) {
			logger.debug("Attempting generating of Object.class via field of type Object.class");
			VariableReference ret = addField(test, (GenericField) o, position,
			                                 recursionDepth + 1);
			ret.setDistance(recursionDepth + 1);
			logger.debug("Success in generating type Object.class");
			return ret;
		} else if (o.isMethod()) {
			logger.debug("Attempting generating of Object.class via method " + (o)
			        + " of type Object.class");
			VariableReference ret = addMethod(test, (GenericMethod) o, position,
			                                  recursionDepth + 1);
			logger.debug("Success in generating type Object.class");
			ret.setDistance(recursionDepth + 1);
			return ret;
		} else if (o.isConstructor()) {
			logger.debug("Attempting generating of Object.class via constructor " + (o)
			        + " of type Object.class");
			VariableReference ret = addConstructor(test, (GenericConstructor) o,
			                                       position, recursionDepth + 1);
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
	public void changeCall(TestCase test, Statement statement,
	        GenericAccessibleObject<?> call) throws ConstructionFailedException {
		int position = statement.getReturnValue().getStPosition();

		logger.debug("Changing call " + test.getStatement(position) + " with " + call);

		if (call.isMethod()) {
			GenericMethod method = (GenericMethod) call;
			if (method.hasTypeParameters())
				throw new ConstructionFailedException(
				        "Cannot handle generic methods properly");

			VariableReference retval = statement.getReturnValue();
			VariableReference callee = null;
			if (!method.isStatic())
				callee = test.getRandomNonNullNonPrimitiveObject(method.getOwnerType(),
				                                                 position);
			List<VariableReference> parameters = new ArrayList<VariableReference>();
			for (Type type : method.getParameterTypes()) {
				parameters.add(test.getRandomObject(type, position));
			}
			MethodStatement m = new MethodStatement(test, method, callee, parameters,
			        retval);
			test.setStatement(m, position);
			logger.debug("Using method " + m.getCode());

		} else if (call.isConstructor()) {

			GenericConstructor constructor = (GenericConstructor) call;
			VariableReference retval = statement.getReturnValue();
			List<VariableReference> parameters = new ArrayList<VariableReference>();
			for (Type type : constructor.getParameterTypes()) {
				parameters.add(test.getRandomObject(type, position));
			}
			ConstructorStatement c = new ConstructorStatement(test, constructor, retval,
			        parameters);

			test.setStatement(c, position);
			logger.debug("Using constructor " + c.getCode());

		} else if (call.isField()) {
			GenericField field = (GenericField) call;
			VariableReference retval = statement.getReturnValue();
			VariableReference source = null;
			if (!field.isStatic())
				source = test.getRandomNonNullNonPrimitiveObject(field.getOwnerType(),
				                                                 position);

			try {
				FieldStatement f = new FieldStatement(test, field, source, retval);

				test.setStatement(f, position);
				logger.debug("Using field " + f.getCode());
			} catch (Throwable e) {
				logger.error("Error: " + e + " , Field: " + field + " , Test: " + test);
				throw new Error(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.AbstractTestFactory#changeRandomCall(de.unisb.cs.st.evosuite.testcase.TestCase, de.unisb.cs.st.evosuite.testcase.StatementInterface)
	 */
	public boolean changeRandomCall(TestCase test, Statement statement) {
		logger.debug("Changing statement ", statement.getCode());
		//+ " in test "
		List<VariableReference> objects = test.getObjects(statement.getReturnValue().getStPosition());
		objects.remove(statement.getReturnValue());
		// TODO: replacing void calls with other void calls might not be the best idea
		List<GenericAccessibleObject<?>> calls = getPossibleCalls(statement.getReturnType(),
		                                                          objects);

		GenericAccessibleObject<?> ao = statement.getAccessibleObject();
		if (ao != null)
			calls.remove(ao);

		logger.debug("Got " + calls.size() + " possible calls for " + objects.size()
		        + " objects");
		calls.clear();
		if (calls.isEmpty())
			return false;
		GenericAccessibleObject<?> call = Randomness.choice(calls);
		try {
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
	private VariableReference createArray(TestCase test, GenericClass arrayClass,
	        int position, int recursionDepth) throws ConstructionFailedException {

		logger.debug("Creating array of type " + arrayClass.getTypeName());
		if (arrayClass.hasWildcardOrTypeVariables()) {
			//if (arrayClass.getComponentClass().isClass()) {
			//	arrayClass = arrayClass.getWithWildcardTypes();
			//} else {
			arrayClass = arrayClass.getGenericInstantiation();
			logger.debug("Setting generic array to type " + arrayClass.getTypeName());
			//}
		}
		// Create array with random size
		ArrayStatement statement = new ArrayStatement(test, arrayClass.getType());
		VariableReference reference = test.addStatement(statement, position);
		position++;
		logger.debug("Array length: " + statement.size());
		logger.debug("Array component type: " + reference.getComponentType());

		// For each value of array, call attemptGeneration
		List<VariableReference> objects = test.getObjects(reference.getComponentType(),
		                                                  position);

		// Don't assign values to other values in the same array initially
		Iterator<VariableReference> iterator = objects.iterator();
		while (iterator.hasNext()) {
			VariableReference current = iterator.next();
			if (current instanceof ArrayIndex) {
				ArrayIndex index = (ArrayIndex) current;
				if (index.getArray().equals(statement.getReturnValue()))
					iterator.remove();
				// Do not assign values of same type as array to elements
				// This may e.g. happen if we have Object[], we could otherwise assign Object[] as values
				else if (index.getArray().getType().equals(arrayClass.getType()))
					iterator.remove();

			}
		}
		objects.remove(statement.getReturnValue());
		logger.debug("Found assignable objects: " + objects.size());
		Set<GenericAccessibleObject<?>> currentArrayRecursion = new HashSet<GenericAccessibleObject<?>>(
		        currentRecursion);
		for (int i = 0; i < statement.size(); i++) {
			currentRecursion.clear();
			currentRecursion.addAll(currentArrayRecursion);
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
	 * @throws ConstructionFailedException
	 */
	private VariableReference createPrimitive(TestCase test, GenericClass clazz,
	        int position, int recursionDepth) throws ConstructionFailedException {
		// Special case: we cannot instantiate Class<Class<?>>
		if (clazz.isClass()) {
			if (clazz.hasWildcardOrTypeVariables()) {
				logger.debug("Getting generic instantiation of class");
				clazz = clazz.getGenericInstantiation();
				logger.debug("Chosen: " + clazz);
			}
			Type parameterType = clazz.getParameterTypes().get(0);
			if (GenericTypeReflector.erase(parameterType).equals(Class.class)) {
				throw new ConstructionFailedException(
				        "Cannot instantiate a class with a class");
			}
		}
		Statement st = PrimitiveStatement.getRandomStatement(test, clazz,
		                                                              position);
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
	 * @throws ConstructionFailedException
	 */
	private VariableReference createNull(TestCase test, Type type, int position,
	        int recursionDepth) throws ConstructionFailedException {
		GenericClass genericType = new GenericClass(type);

		// For example, HashBasedTable.Factory in Guava is private but used as a parameter
		// in a public method. This would lead to compile errors 
		if (!TestClusterGenerator.canUse(genericType.getRawClass())) {
			throw new ConstructionFailedException("Cannot use class " + type);
		}
		if (genericType.hasWildcardOrTypeVariables()) {
			type = genericType.getGenericInstantiation().getType();
		}
		Statement st = new NullStatement(test, type);
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
		GenericAccessibleObject<?> o = TestCluster.getInstance().getRandomGenerator(clazz,
		                                                                            currentRecursion);

		currentRecursion.add(o);
		if (o == null) {
			if (!TestCluster.getInstance().hasGenerator(clazz)) {
				logger.debug("We have no generator for class " + type);
			}
			throw new ConstructionFailedException("Generator is null");
		} else if (o.isField()) {
			logger.debug("Attempting generating of " + type + " via field of type "
			        + type);
			VariableReference ret = addField(test, (GenericField) o, position,
			                                 recursionDepth + 1);
			ret.setDistance(recursionDepth + 1);
			logger.debug("Success in generating type " + type);
			return ret;
		} else if (o.isMethod()) {
			logger.debug("Attempting generating of " + type + " via method " + (o)
			        + " of type " + type);
			VariableReference ret = addMethod(test, (GenericMethod) o, position,
			                                  recursionDepth + 1);

			// TODO: Why are we doing this??
			//if (o.isStatic()) {
			//	ret.setType(type);
			//}
			logger.debug("Success in generating type " + type);
			ret.setDistance(recursionDepth + 1);
			return ret;
		} else if (o.isConstructor()) {
			logger.debug("Attempting generating of " + type + " via constructor " + (o)
			        + " of type " + type + ", with constructor type " + o.getOwnerType());
			VariableReference ret = addConstructor(test, (GenericConstructor) o, type,
			                                       position, recursionDepth + 1);
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
	        int position, int recursionDepth, VariableReference exclude, boolean allowNull)
	        throws ConstructionFailedException {

		if (Properties.SEED_TYPES && parameterType.equals(Object.class)) {
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
		boolean isPrimitiveOrSimilar = clazz.isPrimitive() || clazz.isWrapperType() || clazz.isEnum() || clazz.isClass() || clazz.isString(); 
		if (isPrimitiveOrSimilar && !objects.isEmpty() && reuse <= Properties.PRIMITIVE_REUSE_PROBABILITY) {
			logger.debug(" Looking for existing object of type " + parameterType);
			VariableReference reference = Randomness.choice(objects);
			return reference;

		} else if (!isPrimitiveOrSimilar && !objects.isEmpty() && (reuse <= Properties.OBJECT_REUSE_PROBABILITY)) {

			logger.debug(" Choosing from " + objects.size() + " existing objects");
			VariableReference reference = Randomness.choice(objects);
			logger.debug(" Using existing object of type " + parameterType + ": "
			        + reference);
			return reference;

		} else {
			if (clazz.hasWildcardOrTypeVariables()) {
				logger.debug("Getting generic instantiation of "+clazz);
				if(exclude != null)
					clazz = clazz.getGenericInstantiation(exclude.getGenericClass().getTypeVariableMap());
				else
					clazz = clazz.getGenericInstantiation();
				parameterType = clazz.getType();
			}

			if(clazz.isEnum() || clazz.isPrimitive() || clazz.isWrapperType() || clazz.isObject() || 
					clazz.isClass() || EnvironmentStatements.isEnvironmentData(clazz.getRawClass()) ||
					clazz.isString() || clazz.isArray() || TestCluster.getInstance().hasGenerator(parameterType)) {
				logger.debug(" Generating new object of type " + parameterType);

                VariableReference reference = attemptGeneration(test, parameterType,
				                                                position, recursionDepth,
				                                                allowNull);
				return reference;
			} else {
				if (objects.isEmpty())
					throw new ConstructionFailedException(
					        "Have no objects and generators");

				logger.debug(" Choosing from " + objects.size() + " existing objects");
				VariableReference reference = Randomness.choice(objects);
				logger.debug(" Using existing object of type " + parameterType + ": "
				        + reference);
				return reference;
			}
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
		if (reuse <= Properties.PRIMITIVE_REUSE_PROBABILITY) {

			List<VariableReference> candidates = test.getObjects(Object.class, position);
			filterVariablesByCastClasses(candidates);
			//filterVariablesByClass(candidates, Object.class);
			logger.debug("Choosing object from: "+candidates);
			if (!candidates.isEmpty())
				return Randomness.choice(candidates);
		}
		logger.debug("Attempting object generation");

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

	private static void filterVariablesByCastClasses(Collection<VariableReference> variables) {
		// Remove invalid classes if this is an Object.class reference
		Set<GenericClass> castClasses = CastClassManager.getInstance().getCastClasses();
		Iterator<VariableReference> replacement = variables.iterator();
		while (replacement.hasNext()) {
			VariableReference r = replacement.next();
			boolean isAssignable = false;
			for(GenericClass clazz : castClasses) {
				if(r.isPrimitive())
					continue;
				if(clazz.isAssignableFrom(r.getVariableClass())) {
					isAssignable = true;
					break;
				}
			}
			if (!isAssignable && !r.getVariableClass().equals(Object.class))
				replacement.remove();
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
			if (ms.getReturnType().equals(Object.class)) {
				//				filterVariablesByClass(alternatives, var.getVariableClass());
				filterVariablesByClass(alternatives, Object.class);
			}
		} else if (test.getStatement(position) instanceof ConstructorStatement) {
			ConstructorStatement cs = (ConstructorStatement) test.getStatement(position);
			if (cs.getReturnType().equals(Object.class)) {
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
				Statement s = test.getStatement(i);
				if (s.references(var)) {
					if (s.isAssignmentStatement()) {
						AssignmentStatement assignment = (AssignmentStatement) s;
						if (assignment.getValue() == var) {
							VariableReference replacementVar = Randomness.choice(alternatives);
							if (assignment.getReturnValue().isAssignableFrom(replacementVar)) {
								s.replace(var, replacementVar);
							}
						} else if (assignment.getReturnValue() == var) {
							VariableReference replacementVar = Randomness.choice(alternatives);
							if (replacementVar.isAssignableFrom(assignment.getValue())) {
								s.replace(var, replacementVar);
							}
						}
					} else {
						s.replace(var, Randomness.choice(alternatives));
					}
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
				else if (r instanceof ArrayReference) {
					if (maxIndex >= ((ArrayReference) r).getArrayLength())
						replacement.remove();
				}
			}
			if (!alternatives.isEmpty()) {
				// Change all references to return value at position to something
				// else
				for (int i = position; i < test.size(); i++) {
					Statement s = test.getStatement(i);
					for (VariableReference var2 : s.getVariableReferences()) {
						if (var2 instanceof ArrayIndex) {
							ArrayIndex ai = (ArrayIndex) var2;
							if (ai.getArray().equals(var)) {
								s.replace(var2, Randomness.choice(alternatives));
							}
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
	private static Set<Type> getDependencies(GenericConstructor constructor) {
		Set<Type> dependencies = new LinkedHashSet<Type>();
		for (Type type : constructor.getParameterTypes()) {
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
	private static Set<Type> getDependencies(GenericField field) {
		Set<Type> dependencies = new LinkedHashSet<Type>();
		if (!field.isStatic()) {
			dependencies.add(field.getOwnerType());
		}

		return dependencies;
	}

	/**
	 * Retrieve the dependencies for a method
	 * 
	 * @param method
	 * @return
	 */
	private static Set<Type> getDependencies(GenericMethod method) {
		Set<Type> dependencies = new LinkedHashSet<Type>();
		if (!method.isStatic()) {
			dependencies.add(method.getOwnerType());
		}
		for (Type type : method.getParameterTypes()) {
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
	private List<GenericAccessibleObject<?>> getPossibleCalls(Type returnType,
	        List<VariableReference> objects) {
		List<GenericAccessibleObject<?>> calls = new ArrayList<GenericAccessibleObject<?>>();
		Set<GenericAccessibleObject<?>> allCalls;

		try {
			allCalls = TestCluster.getInstance().getGenerators(new GenericClass(
			                                                           returnType), true);
		} catch (ConstructionFailedException e) {
			return calls;
		}

		for (GenericAccessibleObject<?> call : allCalls) {
			Set<Type> dependencies = null;
			if (call.isMethod()) {
				GenericMethod method = (GenericMethod) call;
				if (method.hasTypeParameters()) {
					try {
						call = method.getGenericInstantiation(new GenericClass(returnType));
					} catch (ConstructionFailedException e) {
						continue;
					}
				}
				if (!((GenericMethod) call).getReturnType().equals(returnType))
					continue;
				dependencies = getDependencies((GenericMethod) call);
			} else if (call.isConstructor()) {
				dependencies = getDependencies((GenericConstructor) call);
			} else if (call.isField()) {
				if (!((GenericField) call).getFieldType().equals(returnType))
					continue;
				dependencies = getDependencies((GenericField) call);
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
		try {
			GenericAccessibleObject<?> o = TestCluster.getInstance().getRandomTestCall();
			if (o == null) {
				logger.warn("Have no target methods to test");
				return false;
			} else if (o.isConstructor()) {
				GenericConstructor c = (GenericConstructor) o;
				logger.debug("Adding constructor call " + c.getName());
				name = c.getName();
				addConstructor(test, c, position, 0);
			} else if (o.isMethod()) {
				GenericMethod m = (GenericMethod) o;
				logger.debug("Adding method call " + m.getName());
				name = m.getName();
				if (!m.isStatic()) {
					logger.debug("Getting callee of type "
					        + m.getOwnerClass().getTypeName());
					VariableReference callee = null;
					Type target = m.getOwnerType();
					// Class<?> target = Properties.getTargetClass();
					//if (!m.getMethod().getDeclaringClass().isAssignableFrom(Properties.getTargetClass())) {
					// If this is an inner class, we cannot force to use the target class
					// FIXXME - generic
					//target = m.getDeclaringClass();
					//}

					if (!test.hasObject(target, position)) {
						callee = createObject(test, target, position, 0);
						position += test.size() - previousLength;
						previousLength = test.size();
					} else {
						callee = test.getRandomNonNullObject(target, position);
						// This may also be an inner class, in this case we can't use a SUT instance
						//if (!callee.isAssignableTo(m.getDeclaringClass())) {
						//	callee = test.getRandomNonNullObject(m.getDeclaringClass(), position);
						//}
					}
					logger.debug("Got callee of type "
					        + callee.getGenericClass().getTypeName());
					if (!TestClusterGenerator.canUse(m.getMethod(),
					                                 callee.getVariableClass())) {
						logger.debug("Cannot call method " + m + " with callee of type "
						        + callee.getClassName());
						throw new ConstructionFailedException(
						        "Cannot apply method to this callee");
					}

					addMethodFor(test, callee,
					             m.copyWithNewOwner(callee.getGenericClass()), position);
				} else {
					// We only use this for static methods to avoid using wrong constructors (?)
					addMethod(test, m, position, 0);
				}
			} else if (o.isField()) {
				GenericField f = (GenericField) o;
				name = f.getName();
				logger.debug("Adding field " + f.getName());
				if (Randomness.nextBoolean()) {
					//logger.info("Adding field assignment " + f.getName());
					addFieldAssignment(test, f, position, 0);
				} else {
					addField(test, f, position, 0);
				}
			} else {
				logger.error("Got type other than method or constructor!");
				return false;
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
//		VariableReference var = selectRandomVariableForCall(test, position);

		boolean success = false;
		
		// Add call for this variable at random position
		if (var != null) {
			logger.debug("Inserting call at position " + position + ", chosen var: "
			        + var.getName() + ", distance: " + var.getDistance() + ", class: "
			        + var.getClassName());
			success = insertRandomCallOnObjectAt(test, var, position);
		} 
		
		if(!success && TestCluster.getInstance().getNumTestCalls() > 0) {
			logger.debug("Adding new call on UUT because var was null");
			success = insertRandomCall(test, position);
		}
		return success;
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
					logger.debug("Assigning array index " + i);
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
		} else if (var.getGenericClass().hasWildcardOrTypeVariables()) {
			// TODO: If the object is of type Foo<?> then only
			//       methods that don't return / need a type ?
			//       should be called. For now, we just don't call
			//       any methods at all.
			logger.debug("Cannot add calls on unknown type");
		} else {
			logger.debug("Getting calls for object " + var.toString());
			try {
				GenericAccessibleObject<?> call = TestCluster.getInstance().getRandomCallFor(var.getGenericClass());
				logger.debug("Chosen call " + call);
				return addCallFor(test, var, call, position);
			} catch (ConstructionFailedException e) {
				logger.debug("Found no modifier: " + e);
			}
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.AbstractTestFactory#insertRandomStatement(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	public int insertRandomStatement(TestCase test, int lastPosition) {
		final double P = Properties.INSERTION_SCORE_UUT
		        + Properties.INSERTION_SCORE_OBJECT
		        + Properties.INSERTION_SCORE_PARAMETER;
		final double P_UUT = Properties.INSERTION_SCORE_UUT / P;
		final double P_OBJECT = P_UUT + Properties.INSERTION_SCORE_OBJECT / P;

		int oldSize = test.size();
		double r = Randomness.nextDouble();
		//		int position = Randomness.nextInt(test.size() + 1);
		int max = lastPosition;
		if (max == test.size())
			max += 1;

		if (max <= 0)
			max = 1;

		int position = Randomness.nextInt(max);

		if (logger.isDebugEnabled()) {
			//for (int i = 0; i < test.size(); i++) {
			//	logger.debug(test.getStatement(i).getCode() + ": Distance = "
			//	        + test.getStatement(i).getReturnValue().getDistance());
			//}
			logger.debug(test.toCode());
		}

		//		if (r <= P_UUT) {
		boolean success = false;
		if (r <= Properties.INSERTION_UUT && TestCluster.getInstance().getNumTestCalls() > 0) {
			// add new call of the UUT - only declared in UUT!
			logger.debug("Adding new call on UUT");
			success = insertRandomCall(test, position);
			if (test.size() - oldSize > 1) {
				position += (test.size() - oldSize - 1);
			}
		} else { // if (r <= P_OBJECT) {
			logger.debug("Adding new call on existing object");
			success = insertRandomCallOnObject(test, position);
			if (test.size() - oldSize > 1) {
				position += (test.size() - oldSize - 1);
			}
			//		} else {
			//			logger.debug("Adding new call with existing object as parameter");
			// insertRandomCallWithObject(test, position);
		}
		if (success)
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
			logger.debug("Current parameter type: " + parameterType);
			if (parameterType instanceof CaptureType) {
				// TODO: This should not really happen in the first place
				throw new ConstructionFailedException("Cannot satisfy capture type");
			}
			GenericClass parameterClass = new GenericClass(parameterType);
			if (parameterClass.hasTypeVariables()) {
				logger.debug("Parameter has type variables, replacing with wildcard");
				parameterType = parameterClass.getWithWildcardTypes().getType();
			}
			int previousLength = test.size();

			VariableReference var = createOrReuseVariable(test, parameterType, position,
			                                              recursionDepth, callee, true);
			
			// Generics instantiation may lead to invalid types, so better double check
			if(!var.isAssignableTo(parameterType)) {
				throw new ConstructionFailedException("Error");
			}
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
			//			sum += 1d / (10 * test.getStatement(i).getReturnValue().getDistance() + 1d);
			sum += 1d / (test.getStatement(i).getReturnValue().getDistance() + 1d);
			if (logger.isDebugEnabled()) {
				logger.debug(test.getStatement(i).getCode() + ": Distance = "
				        + test.getStatement(i).getReturnValue().getDistance());
			}
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

	private VariableReference selectRandomVariableForCall(TestCase test, int position) {
		if (test.isEmpty() || position == 0)
			return null;

		List<VariableReference> allVariables = test.getObjects(position);
		Set<VariableReference> candidateVariables = new LinkedHashSet<VariableReference>();
		for(VariableReference var : allVariables) {
			if (!(var instanceof NullReference) &&
					!var.isVoid() &&
					!(test.getStatement(var.getStPosition()) instanceof PrimitiveStatement) &&
					!var.isPrimitive())
				candidateVariables.add(var);
		}
		if(candidateVariables.isEmpty()) {
			return null;
		} else {
			VariableReference choice = Randomness.choice(candidateVariables);
			return choice;
		}
	}
}
