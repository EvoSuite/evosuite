/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcase;

import org.apache.commons.lang3.ClassUtils;
import org.evosuite.Properties;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.runtime.javaee.injection.Injector;
import org.evosuite.runtime.javaee.javax.servlet.EvoServletState;
import org.evosuite.runtime.util.Inputs;
import org.evosuite.setup.TestUsageChecker;
import org.evosuite.testcase.jee.InjectionSupport;
import org.evosuite.testcase.jee.InstanceOnlyOnce;
import org.evosuite.testcase.jee.ServletSupport;
import org.evosuite.testcase.mutation.change.ModificationStrategy;
import org.evosuite.testcase.mutation.change.RandomModification;
import org.evosuite.testcase.mutation.deletion.DefaultDeletion;
import org.evosuite.testcase.mutation.deletion.DeletionStrategy;
import org.evosuite.testcase.mutation.insertion.AbstractInsertion;
import org.evosuite.testcase.mutation.insertion.InsertionStrategyFactory;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.statements.environment.EnvironmentStatements;
import org.evosuite.testcase.variable.*;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author Gordon Fraser
 */
public class TestFactory {

	private static final Logger logger = LoggerFactory.getLogger(TestFactory.class);

	/**
	 * Singleton instance
	 */
	private static TestFactory instance = null;

    private AbstractInsertion insertionStrategy = InsertionStrategyFactory.getStrategy();
    private ModificationStrategy modificationStrategy = RandomModification.getInstance();
    private DeletionStrategy deletionStrategy = DefaultDeletion.getInstance();

	public static TestFactory getInstance() {
		if (instance == null)
			instance = new TestFactory();
		return instance;
	}


	public VariableReference addFunctionalMockForAbstractClass(TestCase test, Type type, int position, int recursionDepth)
			throws ConstructionFailedException, IllegalArgumentException{

		Inputs.checkNull(test, type);

		if (recursionDepth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException("Max recursion depth reached");
		}

		//TODO this needs to be fixed once we handle Generics in mocks
		FunctionalMockForAbstractClassStatement fms = new FunctionalMockForAbstractClassStatement(test, type, new GenericClass(type));
		VariableReference ref = test.addStatement(fms, position);

		//note: when we add a new mock, by default it will have no parameter at the beginning

		return ref;
	}

	/**
	 * Inserts a call to the given {@code constructor} into the {@code test} case at the specified
	 * {@code position}.
	 * <p>
	 * Callers of this method have to supply the current recursion depth. This
	 * allows for better management of test generation resources. If this method is called from
	 * another method that already has a recursion depth as formal parameter, passing that
	 * recursion depth + 1 is appropriate. Otherwise, 0 should be used.
	 * <p>
	 * Returns a reference to the return value of the constructor call. If the
	 * {@link Properties#MAX_RECURSION maximum recursion depth} has been reached a
	 * {@code ConstructionFailedException} is thrown.
	 *
	 * @param test the test case in which to insert
	 * @param constructor the constructor for which to add the call
	 * @param exactType
	 * @param position the position at which to insert
	 * @param recursionDepth the current recursion depth
	 * @return a reference to the result of the constructor call
	 * @throws ConstructionFailedException if the maximum recursion depth has been reached
	 */
	public VariableReference addConstructor(TestCase test,
	        GenericConstructor constructor, Type exactType, int position,
	        int recursionDepth) throws ConstructionFailedException {

		if (recursionDepth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException("Max recursion depth reached");
		}

		Class<?> klass = constructor.getRawGeneratedType();

		if(Properties.JEE && InstanceOnlyOnce.canInstantiateOnlyOnce(klass) && ConstraintHelper.countNumberOfNewInstances(test,klass) != 0){
			throw new ConstructionFailedException("Class "+klass.getName()+" can only be instantiated once");
		}

		int length = test.size();

		try {
			//first be sure if parameters can be satisfied
			List<VariableReference> parameters = satisfyParameters(test,
																   null,
																   Arrays.asList(constructor.getParameterTypes()),
					                                               Arrays.asList(constructor.getConstructor().getParameters()),
					                                               position,
					                                               recursionDepth + 1,
					                                               true, false, true);
			int newLength = test.size();
			position += (newLength - length);

			//create a statement for the constructor
			Statement st = new ConstructorStatement(test, constructor, parameters);
			VariableReference ref =  test.addStatement(st, position);

			if(Properties.JEE) {
				int injectPosition = doInjection(test, position, klass, ref, recursionDepth);

				if(Properties.HANDLE_SERVLETS) {
					if (HttpServlet.class.isAssignableFrom(klass)) {
						//Servlets are treated specially, as part of JEE
						if (ConstraintHelper.countNumberOfMethodCalls(test, EvoServletState.class, "initServlet") == 0) {
							Statement ms = new MethodStatement(test, ServletSupport.getServletInit(), null,
									Collections.singletonList(ref));
							test.addStatement(ms, injectPosition++);
						}
					}
				}
			}

			return ref;
		} catch (Exception e) {
			throw new ConstructionFailedException("Failed to add constructor for "+klass.getName()+
					" due to "+e.getClass().getCanonicalName()+": "+e.getMessage());
		}
	}

	private int doInjection(TestCase test, int position, Class<?> klass, VariableReference ref,
							int recursionDepth) throws ConstructionFailedException {

		int injectPosition = position + 1;
		int startPos = injectPosition;

		//check if this object needs any dependency injection

		Class<?> target = klass;

		while(target != null) {
			VariableReference classConstant = new ConstantValue(test, new GenericClass(Class.class), target);

			//first check all special fields
			if (Injector.hasEntityManager(target)) {
				Statement ms = new MethodStatement(test, InjectionSupport.getInjectorForEntityManager(), null,
						Arrays.asList(ref, classConstant));
				test.addStatement(ms, injectPosition++);
			}
			if (Injector.hasEntityManagerFactory(target)) {
				Statement ms = new MethodStatement(test, InjectionSupport.getInjectorForEntityManagerFactory(), null,
						Arrays.asList(ref, classConstant));
				test.addStatement(ms, injectPosition++);
			}
			if (Injector.hasUserTransaction(target)) {
				Statement ms = new MethodStatement(test, InjectionSupport.getInjectorForUserTransaction(), null,
						Arrays.asList(ref, classConstant));
				test.addStatement(ms, injectPosition++);
			}
			if (Injector.hasEvent(target)) {
				Statement ms = new MethodStatement(test, InjectionSupport.getInjectorForEvent(), null,
						Arrays.asList(ref, classConstant));
				test.addStatement(ms, injectPosition++);
			}

			//then do the non-special fields that need injection
			for (Field f : Injector.getGeneralFieldsToInject(target)) {

				/*
					Very tricky: if we allow to reuse a variable X, it might end up
					that X is a bounded variable previously created for injection
					but where the initialization calls have not been added yet to the test.
					Handling it "properly" would be far too complicated :(
					So we just avoid reusing existing variables in a recursive call, as
					anyway we can always rely on FM to "save the day"
				 */
				boolean reuseVariables  = recursionDepth == 0;

				int beforeLength = test.size();
				VariableReference valueToInject = satisfyParameters(
						test,
						ref, // avoid calling methods of bounded variables
						Collections.singletonList(f.getType()),
						null, //Added 'null' as additional parameter - fix for @NotNull annotations issue on evo mailing list
						injectPosition,
						recursionDepth +1,
						false, true, reuseVariables).get(0);
				int afterLength = test.size();
				injectPosition += (afterLength - beforeLength);

				VariableReference fieldName = new ConstantValue(test, new GenericClass(String.class), f.getName());
				Statement ms = new MethodStatement(test, InjectionSupport.getInjectorForGeneralField(), null,
						Arrays.asList(ref, classConstant, fieldName, valueToInject));
				test.addStatement(ms, injectPosition++);
			}

			target = target.getSuperclass();
		}

		if(injectPosition != startPos) {
			//validate the bean, but only if there was any injection
			VariableReference classConstant = new ConstantValue(test, new GenericClass(Class.class), klass);
			Statement ms = new MethodStatement(test, InjectionSupport.getValidateBean(), null, Arrays.asList(ref, classConstant));
			test.addStatement(ms, injectPosition++);
		}

		/*
			finally, call the the postConstruct (if any), but be sure the ones in
			 superclass(es) are called first
		 */
		int pos = injectPosition;
		target = klass;

		while(target != null) {
			if (Injector.hasPostConstruct(target)) {
				VariableReference classConstant = new ConstantValue(test, new GenericClass(Class.class), target);
				Statement ms = new MethodStatement(test, InjectionSupport.getPostConstruct(), null,
						Arrays.asList(ref,classConstant));
				test.addStatement(ms, pos);
				injectPosition++;
			}
			target = target.getSuperclass();
		}
		return injectPosition;
	}

	/**
	 * Adds the given primitive {@code statement} at the specified {@code position} to the test
	 * case {@code test}.
	 *
	 * @param test the test case to which to add the statement
	 * @param statement the primitive statement to add
	 * @param position the position in {@code test} at which to add the {@code statement}
	 * @return a reference to the return value of the added statement
	 */
	private VariableReference addPrimitive(TestCase test, PrimitiveStatement<?> statement,
	        int position) {
		logger.debug("Adding primitive");
		Statement st = statement.clone(test);
		return test.addStatement(st, position);
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

		logger.debug("Changing call {} with {}",test.getStatement(position), call);

		if (call.isMethod()) {
			GenericMethod method = (GenericMethod) call;
			if (method.hasTypeParameters())
				throw new ConstructionFailedException("Cannot handle generic methods properly");

			VariableReference retval = statement.getReturnValue();
			VariableReference callee = null;
			if (!method.isStatic()) {
				callee = getRandomNonNullNonPrimitiveObject(test, method.getOwnerType(), position);
			}

			List<VariableReference> parameters = new ArrayList<>();
			for (Type type : method.getParameterTypes()) {
				parameters.add(test.getRandomObject(type, position));
			}
			MethodStatement m = new MethodStatement(test, method, callee, parameters, retval);
			test.setStatement(m, position);
			logger.debug("Using method {}", m.getCode());

		} else if (call.isConstructor()) {

			GenericConstructor constructor = (GenericConstructor) call;
			VariableReference retval = statement.getReturnValue();
			List<VariableReference> parameters = new ArrayList<>();
			for (Type type : constructor.getParameterTypes()) {
				parameters.add(test.getRandomObject(type, position));
			}
			ConstructorStatement c = new ConstructorStatement(test, constructor, retval, parameters);

			test.setStatement(c, position);
			logger.debug("Using constructor {}", c.getCode());

		} else if (call.isField()) {
			GenericField field = (GenericField) call;
			VariableReference retval = statement.getReturnValue();
			VariableReference source = null;
			if (!field.isStatic())
				source = getRandomNonNullNonPrimitiveObject(test,field.getOwnerType(), position);

			try {
				FieldStatement f = new FieldStatement(test, field, source, retval);
				test.setStatement(f, position);
				logger.debug("Using field {}", f.getCode());
			} catch (Throwable e) {
				logger.error("Error: " + e + " , Field: " + field + " , Test: " + test);
				throw new Error(e);
			}
		}
	}

	private VariableReference getRandomNonNullNonPrimitiveObject(TestCase tc, Type type, int position)
			throws ConstructionFailedException {
		Inputs.checkNull(type);

		List<VariableReference> variables = tc.getObjects(type, position);
		variables.removeIf(var -> var instanceof NullReference
				|| tc.getStatement(var.getStPosition()) instanceof PrimitiveStatement
				|| var.isPrimitive()
				|| var.isWrapperType()
				|| tc.getStatement(var.getStPosition()) instanceof FunctionalMockStatement
				|| ConstraintHelper.getLastPositionOfBounded(var, tc) >= position);

		if (variables.isEmpty()) {
			throw new ConstructionFailedException("Found no variables of type " + type
					+ " at position " + position);
		}

		return Randomness.choice(variables);
	}

	public boolean changeRandomCall(TestCase test, Statement statement) {
		return modificationStrategy.changeRandomCall(test, statement);
	}

	/**
	 * Delete the statement at position from the test case and remove all
	 * references to it
	 *
	 * @param test
	 * @param position
	 * @return false if it was not possible to delete the statement
	 * @throws ConstructionFailedException
	 */
	public boolean deleteStatement(TestCase test, int position)
	        throws ConstructionFailedException {
		return deletionStrategy.deleteStatement(test, position);
	}

	/**
	 *
	 * @param test
	 * @param position
	 * @return true if statements was deleted or any dependency was modified
	 * @throws ConstructionFailedException
	 */
	public boolean deleteStatementGracefully(TestCase test, int position)
	        throws ConstructionFailedException {
		return deletionStrategy.deleteStatementGracefully(test, position);
	}

	/**
	 * Inserts one or perhaps multiple random statements into the given {@code test}. Callers
	 * have to specify the position of the last valid statement of {@code test} by supplying an
	 * appropriate index {@code lastPosition}. After a successful insertion, returns the updated
	 * position of the last valid statement (which is always non-negative), or if there was an error
	 * the constant {@link AbstractInsertion#INSERTION_ERROR
	 * INSERTION_ERROR}.
	 *
	 * @param test the test case in which to insert
	 * @param lastPosition the position of the last valid statement of {@code test} before insertion
	 * @return the position of the last valid statement after insertion, or {@code INSERTION_ERROR}
	 * (see above)
	 */
	public int insertRandomStatement(TestCase test, int lastPosition) {
		return insertionStrategy.insertStatement(test, lastPosition);
	}


	// -------------------------------------------------------------------
	// FIXME DELEGATES for backwards compatibility
	// The system tests have not been restructured yet.
	// In order to prevent them from failing, the methods below this
	// comment were added to the interface of TestFactory.
	// In an effort to refactor the TestFactory, they were moved to their
	// own class AbstractInsertion. However, the system tests still expect
	// them to be in the TestFactory class. In the future, the system
	// tests should be restructured as well so that we can remove the
	// delegates.
	// -------------------------------------------------------------------


	public final void appendStatement(TestCase test, Statement statement) throws ConstructionFailedException {
		insertionStrategy.appendStatement(test, statement);
	}

	public final List<VariableReference> satisfyParameters(TestCase test, VariableReference callee,
														   List<Type> parameterTypes,
														   List<Parameter> parameterList, int position, int recursionDepth, boolean allowNull,
														   boolean excludeCalleeGenerators, boolean canReuseExistingVariables) throws ConstructionFailedException {
		return insertionStrategy.satisfyParameters(test, callee, parameterTypes, parameterList,
				position, recursionDepth, allowNull, excludeCalleeGenerators, canReuseExistingVariables);
	}

	public final VariableReference attemptGeneration(TestCase newTest, Type returnType,
									 int statement) throws ConstructionFailedException {
		return insertionStrategy.attemptGeneration(newTest, returnType, statement);
	}

	public final void resetContext() {
		insertionStrategy.reset();
	}

	public final void insertRandomCallOnObjectAt(TestCase testCase, VariableReference var, int i) {
		insertionStrategy.insertRandomCallOnObjectAt(testCase, var, i);
	}

	public final VariableReference createObject(TestCase testCase, Type type, int statement, int i,
										  VariableReference o) throws ConstructionFailedException {
		return insertionStrategy.createObject(testCase, type, statement, i, o);
	}

	public final VariableReference addMethod(TestCase test, GenericMethod call, int size, int i) throws ConstructionFailedException {
		return insertionStrategy.addMethod(test, call, size, i);
	}

	public final VariableReference addConstructor(TestCase test, GenericConstructor call, int size, int i) throws ConstructionFailedException {
		return insertionStrategy.addConstructor(test, call, size, i);
	}

	public void addMethodFor(TestCase tc, VariableReference genericClass, GenericMethod gm, int i) throws ConstructionFailedException {
		insertionStrategy.addMethodFor(tc, genericClass, gm, i);
	}

	public VariableReference addField(TestCase test, GenericField field, int position,
									   int recursionDepth) throws ConstructionFailedException {
		return insertionStrategy.addField(test, field, position, recursionDepth);
	}

	public VariableReference addFieldFor(TestCase test, VariableReference callee,
										 GenericField field, int position) throws ConstructionFailedException {
		return insertionStrategy.addFieldFor(test, callee, field, position);
	}

	public VariableReference addFieldAssignment(TestCase test, GenericField field,
												 int position, int recursionDepth) throws ConstructionFailedException {
		return insertionStrategy.addFieldAssignment(test, field, position, recursionDepth);
	}
}
