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

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.runtime.util.Inputs;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.mutation.AbstractInsertionStrategy;
import org.evosuite.testcase.mutation.InsertionStrategyFactory;
import org.evosuite.testcase.statements.*;
import org.evosuite.testcase.variable.*;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private AbstractInsertionStrategy insertionStrategy = InsertionStrategyFactory.getStrategy();

	public static TestFactory getInstance() {
		if (instance == null)
			instance = new TestFactory();
		return instance;
	}

	/**
	 * Adds a call of the field or method represented by {@code gao} to the test case
	 * {@code test} at the given {@code position} with {@code callee} as the callee of {@code gao}.
	 * Note that constructor calls are <em>not</em> supported
	 * Returns {@code true} if the operation was successful, {@code false} otherwise.
	 *
	 * @param test the test case the call should be added to
	 * @param callee reference to the owning object of {@code gao}
	 * @param gao the {@code GenericAccessibleObject}
	 * @param position the position within {@code test} at which to add the call
	 * @return {@code true} if successful, {@code false} otherwise
	 */
	protected boolean addCallForMethodOrField(TestCase test, VariableReference callee,
											  GenericAccessibleMember<?> gao, int position) {

		logger.trace("addCallFor {}", callee.getName());

		int previousLength = test.size(); // length of the test case before inserting new statements
		currentRecursion.clear();
		int recursionDepth = 0;

		try {
			if (gao.isMethod()) {
				GenericMethod method = (GenericMethod) gao;
				Class<?> declaringClass = method.getDeclaringClass();
				Class<?> calleeClass = callee.getVariableClass();
				if(gao.isStatic() || !declaringClass.isAssignableFrom(calleeClass)) {
					// Static methods / methods in other classes can be modifiers of the SUT if the SUT depends on static fields
					addMethod(test, method, position, recursionDepth);
				} else {
					method = (GenericMethod) gao.copyWithNewOwner(callee.getGenericClass());
					addMethodFor(test, callee, method, position);
				}
			} else if (gao.isField()) {
				// A modifier for the SUT could also be a static field in another class
				if(gao.isStatic()) {
					addFieldAssignment(test, (GenericField) gao, position, recursionDepth);
				} else {
					addFieldFor(test, callee, (GenericField) gao.copyWithNewOwner(callee.getGenericClass()), position);
				}
			}
			return true;
		} catch (ConstructionFailedException e) {
			// TODO: Check this!
			logger.debug("Inserting call {} has failed: {} Removing statements", gao, e);
			// TODO: Doesn't work if position != test.size()
			int lengthDifference = test.size() - previousLength;

			// Undo the changes made to the test case by removing the statements inserted so far.
			// We need to remove them in order, so that the test case is at all time consistent.
			for (int i = lengthDifference - 1; i >= 0; i--) {
				if(logger.isDebugEnabled()) {
					logger.debug("  Removing statement: " + test.getStatement(position + i).getCode());
				}
				test.remove(position + i);
			}
			if(logger.isDebugEnabled()) {
				logger.debug("Test after removal: " + test.toCode());
			}
			return false;
		}
	}


	public VariableReference addFunctionalMock(TestCase test, Type type, int position, int recursionDepth)
			throws ConstructionFailedException, IllegalArgumentException{

		Inputs.checkNull(test, type);

		if (recursionDepth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException("Max recursion depth reached");
		}

		//TODO this needs to be fixed once we handle Generics in mocks
		FunctionalMockStatement fms = new FunctionalMockStatement(test, type, new GenericClass(type));
		VariableReference ref = test.addStatement(fms, position);

		//note: when we add a new mock, by default it will have no parameter at the beginning

		return ref;
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
	 * @param position the position at which to insert
	 * @param recursionDepth the current recursion depth
	 * @return a reference to the result of the constructor call
	 * @throws ConstructionFailedException if the maximum recursion depth has been reached
	 */
	public VariableReference addConstructor(TestCase test,
	        GenericConstructor constructor, int position, int recursionDepth)
	        throws ConstructionFailedException {
		return addConstructor(test, constructor, null, position, recursionDepth);
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
	 * Adds the given {@code field} to the {@code test} case at the given {@code position}.
	 * <p>
	 * Callers of this method have to supply the current recursion depth. This
	 * allows for better management of test generation resources. If this method is called from
	 * another method that already has a recursion depth as formal parameter, passing that
	 * recursion depth + 1 is appropriate. Otherwise, 0 should be used.
	 * <p>
	 * Returns a reference to the inserted field. If the {@link Properties#MAX_RECURSION maximum
	 * recursion depth} has been reached a {@code ConstructionFailedException} is thrown.
	 *
	 * @param test the test case to which to add
	 * @param field the field to add
	 * @param position the position at which to add the field
	 * @param recursionDepth the current recursion depth
	 * @return a reference to the inserted field
	 * @throws ConstructionFailedException if the maximum recursion depth has been reached
	 */
	public VariableReference addField(TestCase test, GenericField field, int position,
	        int recursionDepth) throws ConstructionFailedException {

		logger.debug("Adding field {}", field);
		if (recursionDepth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException("Max recursion depth reached");
		}

		VariableReference callee = null;
		int length = test.size();

		if (!field.isStatic()) {
			callee = createOrReuseVariable(test, field.getOwnerType(), position,
					recursionDepth, null, false, false, false);
			position += test.size() - length;

			if (!TestUsageChecker.canUse(field.getField(), callee.getVariableClass())) {
				logger.debug("Cannot call field {} with callee of type {}", field, callee.getClassName());
				throw new ConstructionFailedException("Cannot apply field to this callee");
			}

			// TODO: Check if field is still accessible in subclass
			if(!field.getOwnerClass().equals(callee.getGenericClass())) {
				try {
					if(!TestUsageChecker.canUse(callee.getVariableClass().getField(field.getName()))) {
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
	 * @param field
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
					recursionDepth, null, false, false, false);
			position += test.size() - length;
			length = test.size();
			if (!TestUsageChecker.canUse(field.getField(), callee.getVariableClass())) {
				logger.debug("Cannot call field " + field + " with callee of type "
				        + callee.getClassName());
				throw new ConstructionFailedException("Cannot apply field to this callee");
			}

		}

		VariableReference var = createOrReuseVariable(test, field.getFieldType(),
		                                              position, recursionDepth, callee, true, false, false);
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
				position, 0, callee, true, false, true);

		int newLength = test.size();
		position += (newLength - length);

		Statement st = new AssignmentStatement(test, fieldVar, value);
		VariableReference ret = test.addStatement(st, position);
		ret.setDistance(callee.getDistance() + 1);

		assert (test.isValid());

		return ret;
	}

	/**
	 * Adds the given {@code method} call to the {@code test} at the specified {@code position}.
	 * For non-static methods, the callee object of the method is chosen at random.
	 * <p>
	 * Clients have to supply the current recursion depth. This allows for better
	 * management of test generation resources. If this method is called from another method that
	 * already has a recursion depth as formal parameter, passing that recursion depth + 1 is
	 * appropriate. Otherwise, 0 should be used.
	 * <p>
	 * Returns a reference to the return value of the inserted method call. If the
	 * {@link Properties#MAX_RECURSION maximum  recursion depth} has been reached a
	 * {@code ConstructionFailedException} is thrown.
	 *
	 * @param test the test case in which to insert
	 * @param method the method call to insert
	 * @param position the position at which to add the call
	 * @param recursionDepth the current recursion depth (see above)
	 * @return a reference to the return value of the inserted method call
	 * @throws ConstructionFailedException if the maximum recursion depth has been reached
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
						recursionDepth, null, false, false, false);
				//a functional mock can never be a callee
				assert ! (test.getStatement(callee.getStPosition()) instanceof FunctionalMockStatement);

				position += test.size() - length;
				length = test.size();

				logger.debug("Found callee of type " + method.getOwnerType() + ": "
						+ callee.getName());
				if (!TestUsageChecker.canUse(method.getMethod(),
						callee.getVariableClass())) {
					logger.debug("Cannot call method " + method
							+ " with callee of type " + callee.getClassName());
					throw new ConstructionFailedException("Cannot apply method to this callee");
				}
			}

			// Added 'null' as additional parameter - fix for @NotNull annotations issue on evo mailing list
			parameters = satisfyParameters(test, callee,
					                       Arrays.asList(method.getParameterTypes()),
					                       Arrays.asList(method.getMethod().getParameters()),
					                       position, recursionDepth + 1, true, false, true);

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
	 * Adds the given {@code method} call to the {@code test} at the specified {@code position},
	 * using the supplied {@code VariableReference} as {@code callee} object of the {@code method}.
	 * Only intended to be used for <em>non-static</em> methods! If a static {@code method} is
	 * supplied, the behavior is undefined.
	 * <p>
	 * Returns a reference to the return value of the inserted method call. Throws a
	 * {@code ConstructionFailedException} if the given {@code position} is invalid, i.e., if
	 * {@code callee} is undefined (or has not been defined yet) at {@code position}.
	 *
	 * @param test the test case in which to insert
	 * @param callee reference to the object on which to call the {@code method}
	 * @param method the method call to insert
	 * @param position the position at which to add the call
	 * @return a reference to the return value of the inserted method call
	 * @throws ConstructionFailedException if the given position is invalid (see above)
	 */
	public VariableReference addMethodFor(TestCase test, VariableReference callee,
	        GenericMethod method, int position) throws ConstructionFailedException {

		logger.debug("Adding method {} for {} (Generating {})",method,callee,method.getGeneratedClass());

		if(position <= callee.getStPosition()) {
			throw new ConstructionFailedException("Cannot insert call on object before the object is defined");
		}

		currentRecursion.clear();
		int length = test.size();

		boolean allowNull = true;
		Constraints constraints = method.getMethod().getAnnotation(Constraints.class);
		if(constraints!=null && constraints.noNullInputs()){
			allowNull = false;
		}

		// Added 'null' as additional parameter - fix for @NotNull annotations issue on evo mailing list
		List<VariableReference> parameters = satisfyParameters(
				test, callee,
				Arrays.asList(method.getParameterTypes()),
				Arrays.asList(method.getMethod().getParameters()), position, 1, allowNull, false, true);

		int newLength = test.size();
		position += (newLength - length);

		Statement st = new MethodStatement(test, method, callee, parameters);
		VariableReference ret = test.addStatement(st, position);
		ret.setDistance(callee.getDistance() + 1);

		logger.debug("Success: Adding method {}", method);
		return ret;
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
	 * Appends the given {@code statement} at the end of the test case {@code test}, trying to
	 * satisfy parameters.
	 *
	 * Called from TestChromosome when doing crossover
	 *
	 * @param test
	 * @param statement
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
	 */
	public VariableReference attemptGeneration(TestCase test, Type type, int position)
	        throws ConstructionFailedException {
		return attemptGeneration(test, type, position, 0, false, null, true, true);
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
	        int recursionDepth, boolean allowNull, VariableReference generatorRefToExclude,
												  boolean canUseMocks, boolean canReuseExistingVariables)
			throws ConstructionFailedException {

		GenericClass clazz = new GenericClass(type);

		if (clazz.isEnum()) {

			if (!TestUsageChecker.canUse(clazz.getRawClass()))
				throw new ConstructionFailedException("Cannot generate unaccessible enum " + clazz);
			return createPrimitive(test, clazz, position, recursionDepth);

		} else if (clazz.isPrimitive() || clazz.isClass()
		        || EnvironmentStatements.isEnvironmentData( clazz.getRawClass())) {

			return createPrimitive(test, clazz, position, recursionDepth);

		} else if (clazz.isString()) {

			if (allowNull && Randomness.nextDouble() <= Properties.NULL_PROBABILITY) {
				logger.debug("Using a null reference to satisfy the type: {}", type);
				return createNull(test, type, position, recursionDepth);
			} else {
				return createPrimitive(test, clazz, position, recursionDepth);
			}

		} else if (clazz.isArray()) {

			if (allowNull && Randomness.nextDouble() <= Properties.NULL_PROBABILITY) {
				logger.debug("Using a null reference to satisfy the type: {}", type);
				return createNull(test, type, position, recursionDepth);
			} else {
				return createArray(test, clazz, position, recursionDepth);
			}

		} else {

			if (allowNull && Randomness.nextDouble() <= Properties.NULL_PROBABILITY) {
				logger.debug("Using a null reference to satisfy the type: {}", type);
				return createNull(test, type, position, recursionDepth);
			}

			ObjectPoolManager objectPool = ObjectPoolManager.getInstance();
			if (Randomness.nextDouble() <= Properties.P_OBJECT_POOL
			        && objectPool.hasSequence(clazz)) {

				TestCase sequence = objectPool.getRandomSequence(clazz);
				logger.debug("Using a sequence from the object pool to satisfy the type: {}", type);
				VariableReference targetObject = sequence.getLastObject(type);
				int returnPos = position + targetObject.getStPosition();

				for (int i = 0; i < sequence.size(); i++) {
					Statement s = sequence.getStatement(i);
					test.addStatement(s.copy(test, position), position + i);
				}

				logger.debug("Return type of object sequence: {}",
				         test.getStatement(returnPos).getReturnValue().getClassName());

				return test.getStatement(returnPos).getReturnValue();
			}

			logger.debug("Creating new object for type {}",type);
			return createObject(test, type, position, recursionDepth,
					generatorRefToExclude, allowNull, canUseMocks,canReuseExistingVariables);
		}
	}

	/**
	 * In the given test case {@code test}, tries to generate an object at the specified {@code
     * position} suitable to serve as instance for the class {@code java.lang.Object}. This might
	 * be useful when generating tests for "legacy code" before the advent of generics in Java.
	 * Such code is likely to use (unsafe) down-casts from {@code Object} to some other subclass.
	 * Since {@code Object} is at the root of the type hierarchy the information that something is
	 * of type {@code Object} is essentially as valuable as no type information at all. For this
	 * reason, this method scans the byte code of the UUT for subsequent down-casts and tries to
	 * generate an instance of the subclass being cast to. If {@code allowNull} is {@code true} it
	 * is also possible to assign the {@code null} reference.
     * <p>
     * Clients have to supply the current recursion depth. This allows for better
     * management of test generation resources. If this method is called from another method that
     * already has a recursion depth as formal parameter, passing that recursion depth + 1 is
     * appropriate. Otherwise, 0 should be used.
     * <p>
     * Returns a reference to the created object of type {@code java.lang.Object}, or throws a
     * {@code ConstructionFailedException} if an error occurred.
	 *
	 * @param test the test case in which to insert
	 * @param position the position at which to insert
	 * @param recursionDepth the current recursion depth (see above)
	 * @param allowNull whether to allow the creation of  the {@code null} reference
	 * @return a reference to the created object
	 * @throws ConstructionFailedException if creation fails
	 */
	protected VariableReference attemptInstantiationOfObjectClass(TestCase test, int position,
																  int recursionDepth, boolean allowNull) throws ConstructionFailedException {

		if (allowNull && Randomness.nextDouble() <= Properties.NULL_PROBABILITY) {
			logger.debug("Using a null reference to satisfy the type: {}", Object.class);
			return createNull(test, Object.class, position, recursionDepth);
		}

		Set<GenericClass> castClasses = new LinkedHashSet<>(CastClassManager.getInstance().getCastClasses());
		//needed a copy because hasGenerator(c) does modify that set...
		List<GenericClass> classes = castClasses.stream()
				.filter(c -> TestCluster.getInstance().hasGenerator(c) || c.isString())
				.collect(Collectors.toList());
		classes.add(new GenericClass(Object.class));

		//TODO if classes is empty, should we use FM here?

		GenericClass choice = Randomness.choice(classes);
		logger.debug("Chosen class for Object: {}", choice);
		if(choice.isString()) {
			return createOrReuseVariable(test, String.class, position,
                    recursionDepth, null, allowNull, false, false);
		}

		GenericAccessibleObject<?> o = TestCluster.getInstance().getRandomGenerator(choice);
		currentRecursion.add(o);

		if (o == null) {

			if (!TestCluster.getInstance().hasGenerator(Object.class)) {
				logger.debug("We have no generator for Object.class ");
			}
			throw new ConstructionFailedException("Generator is null");

		} else if (o.isField()) {

			logger.debug("Attempting generating of Object.class via field of type Object.class");
			VariableReference ret = addField(test, (GenericField) o, position, recursionDepth + 1);
			ret.setDistance(recursionDepth + 1);
			logger.debug("Success in generating type Object.class");
			return ret;

		} else if (o.isMethod()) {

			logger.debug("Attempting generating of Object.class via method {} of type Object.class", o);
			VariableReference ret = addMethod(test, (GenericMethod) o, position, recursionDepth + 1);
			logger.debug("Success in generating type Object.class");
			ret.setDistance(recursionDepth + 1);
			return ret;

		} else if (o.isConstructor()) {

			logger.debug("Attempting generating of Object.class via constructor {} of type Object.class", o);
			VariableReference ret = addConstructor(test, (GenericConstructor) o, position, recursionDepth + 1);
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
		logger.debug("Changing statement {}", statement.getCode());

		List<VariableReference> objects = test.getObjects(statement.getReturnValue().getStPosition());
		objects.remove(statement.getReturnValue());

		Iterator<VariableReference> iter = objects.iterator();
		while(iter.hasNext()){
			VariableReference ref = iter.next();
			//do not use FM as possible callees
			if(test.getStatement(ref.getStPosition()) instanceof FunctionalMockStatement){
				iter.remove();
				continue;
			}

			int boundPosition = ConstraintHelper.getLastPositionOfBounded(ref, test);
			if(boundPosition >= 0 && boundPosition >= statement.getPosition()){
				// if bounded variable, cannot add methods before its initialization, and so cannot be
				// used as a callee
				iter.remove();
			}
		}

		// TODO: replacing void calls with other void calls might not be the best idea
		List<GenericAccessibleObject<?>> calls = getPossibleCalls(statement.getReturnType(), objects);

		GenericAccessibleObject<?> ao = statement.getAccessibleObject();
		if (ao != null && ao.getNumParameters() > 0) {
			calls.remove(ao);
		}

		if(ConstraintHelper.getLastPositionOfBounded(statement.getReturnValue(),test) >= 0){
			//if the return variable is bounded, we can only use a constructor on the right hand-side
			calls.removeIf(k -> !(k instanceof GenericConstructor));
		}

		logger.debug("Got {} possible calls for {} objects",calls.size(),objects.size());

		//calls.clear();
		if (calls.isEmpty()) {
			logger.debug("No replacement calls");
			return false;
		}

		GenericAccessibleObject<?> call = Randomness.choice(calls);
		try {
			changeCall(test, statement, call);
			return true;
		} catch (ConstructionFailedException e) {
			// Ignore
			logger.info("Change failed for statement " + statement.getCode() + " -> "
			        + call + ": " + e.getMessage() + " " + test.toCode());
		}
		return false;
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

		if(! ConstraintVerifier.canDelete(test, position)){
			return false;
		}

		logger.debug("Deleting target statement - {}", position);

		Set<Integer> toDelete = new LinkedHashSet<>();
		recursiveDeleteInclusion(test,toDelete,position);

		List<Integer> pos = new ArrayList<>(toDelete);
		pos.sort(Collections.reverseOrder());

		for (int i : pos) {
			logger.debug("Deleting statement: {}", i);
			test.remove(i);
		}

		return true;
	}

	private void recursiveDeleteInclusion(TestCase test, Set<Integer> toDelete, int position){

		if(toDelete.contains(position)){
			return; //end of recursion
		}

		toDelete.add(position);

		Set<Integer> references = getReferencePositions(test, position);

		/*
			it can happen that we can delete the target statements but, when we look at
			the other statements using it, then we could not delete them :(
			in those cases, we have to recursively look at all their dependencies.
		 */

		for (Integer i : references) {

			Set<Integer> constraintDependencies = ConstraintVerifier.dependentPositions(test, i);
			if(constraintDependencies!=null){
				for(Integer j : constraintDependencies){
					recursiveDeleteInclusion(test,toDelete,j);
				}
			}

			recursiveDeleteInclusion(test,toDelete,i);
		}
	}

	private Set<Integer> getReferencePositions(TestCase test, int position) {
		Set<VariableReference> references = new LinkedHashSet<>();
		Set<Integer> positions = new LinkedHashSet<>();
		references.add(test.getReturnValue(position));

		for (int i = position; i < test.size(); i++) {
			Set<VariableReference> temp = new LinkedHashSet<>();
			for (VariableReference v : references) {
				if (test.getStatement(i).references(v)) {
					temp.add(test.getStatement(i).getReturnValue());
					positions.add(i);
				}
			}
			references.addAll(temp);
		}
		return positions;
	}

	private static void filterVariablesByClass(Collection<VariableReference> variables, Class<?> clazz) {
		// Remove invalid classes if this is an Object.class reference
		variables.removeIf(r -> !r.getVariableClass().equals(clazz));
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
		VariableReference var = test.getReturnValue(position);

		if (var instanceof ArrayIndex) {
			return deleteStatement(test, position);
		}

		boolean changed = false;

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
			if(test.getStatement(r.getStPosition()) instanceof FunctionalMockStatement){
				// we should ensure that a FM should never be a callee
				replacement.remove();
			} else if (var.equals(r.getAdditionalVariableReference())) {
				replacement.remove();
			} else if(var.isFieldReference()) {
				FieldReference fref = (FieldReference)var;
				if(fref.getField().isFinal()) {
					replacement.remove();
				}
			} else if (r instanceof ArrayReference) {
				if (maxIndex >= ((ArrayReference) r).getArrayLength())
					replacement.remove();
			} else if (!replacingPrimitive) {
				if (test.getStatement(r.getStPosition()) instanceof PrimitiveStatement) {
					replacement.remove();
				}
			}
		}

		if (!alternatives.isEmpty()) {
			// Change all references to return value at position to something else
			for (int i = position + 1; i < test.size(); i++) {
				Statement s = test.getStatement(i);
				if (s.references(var)) {
					if (s.isAssignmentStatement()) {
						AssignmentStatement assignment = (AssignmentStatement) s;
						if (assignment.getValue() == var) {
							VariableReference replacementVar = Randomness.choice(alternatives);
							if (assignment.getReturnValue().isAssignableFrom(replacementVar)) {
								s.replace(var, replacementVar);
								changed = true;
							}
						} else if (assignment.getReturnValue() == var) {
							VariableReference replacementVar = Randomness.choice(alternatives);
							if (replacementVar.isAssignableFrom(assignment.getValue())) {
								s.replace(var, replacementVar);
								changed = true;
							}
						}
					} else {
						/*
							if 'var' is a bounded variable used in 's', then it should not be
							replaced with another one. should be left as it is, as to make it
							deletable
						 */
						boolean bounded = false;
						if(s instanceof EntityWithParametersStatement){
							EntityWithParametersStatement es = (EntityWithParametersStatement) s;
							bounded = es.isBounded(var);
						}

						if(!bounded) {
							s.replace(var, Randomness.choice(alternatives));
							changed = true;
						}
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
				// Change all references to return value at position to something else
				for (int i = position; i < test.size(); i++) {
					Statement s = test.getStatement(i);
					for (VariableReference var2 : s.getVariableReferences()) {
						if (var2 instanceof ArrayIndex) {
							ArrayIndex ai = (ArrayIndex) var2;
							if (ai.getArray().equals(var)) {
								s.replace(var2, Randomness.choice(alternatives));
								changed = true;
							}
						}
					}
				}
			}
		}

		// Remove everything else
		boolean deleted = deleteStatement(test, position);
		return  deleted || changed;
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
		return new LinkedHashSet<>(Arrays.asList(constructor.getParameterTypes()));
	}

	/**
	 * Retrieve the dependencies for a field
	 *
	 * @param field
	 * @return
	 */
	private static Set<Type> getDependencies(GenericField field) {
		Set<Type> dependencies = new LinkedHashSet<>();
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
		Set<Type> dependencies = new LinkedHashSet<>();
		if (!method.isStatic()) {
			dependencies.add(method.getOwnerType());
		}
		dependencies.addAll(Arrays.asList(method.getParameterTypes()));

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
		List<GenericAccessibleObject<?>> calls = new ArrayList<>();
		Set<GenericAccessibleObject<?>> allCalls;

		try {
			allCalls = TestCluster.getInstance().getGenerators(new GenericClass(
			                                                           returnType));
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
	 * Inserts one or perhaps multiple random statements into the given {@code test}. Callers
	 * have to specify the position of the last valid statement of {@code test} by supplying an
	 * appropriate index {@code lastPosition}. After a successful insertion, returns the updated
	 * position of the last valid statement (which is always non-negative), or if there was an error
	 * the constant {@link AbstractInsertionStrategy#INSERTION_ERROR
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
	// DELEGATES
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

	public final void attemptGeneration(TestCase newTest, Type returnType, int statement) throws ConstructionFailedException {
		insertionStrategy.attemptGeneration(newTest, returnType, statement);
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
