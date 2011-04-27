/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.ga.Randomness;
import de.unisb.cs.st.evosuite.testsuite.CurrentChromosomeTracker;
import de.unisb.cs.st.evosuite.testsuite.TestCallObject;
import de.unisb.cs.st.evosuite.testsuite.TestCallStatement;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

/**
 * Handle test case generation
 * 
 * @author Gordon Fraser
 * 
 */
public class DefaultTestFactory extends AbstractTestFactory {

	private static DefaultTestFactory instance = null;

	private static Logger logger = Logger.getLogger(DefaultTestFactory.class);

	private final static int MAX_RECURSION = Properties.getIntegerValue("max_recursion");

	private final static double OBJECT_REUSE_PROBABILITY = Properties.getDoubleValue("object_reuse_probability");

	private final static double PRIMITIVE_REUSE_PROBABILITY = Properties.getDoubleValue("primitive_reuse_probability");

	private final static double NULL_PROBABILITY = Properties.getDoubleValue("null_probability");

	private final static double CALL_PROBABILITY = Properties.getDoubleValue("call_probability");

	private final Randomness randomness = Randomness.getInstance();

	private final MethodDescriptorReplacement descriptor_replacement = MethodDescriptorReplacement.getInstance();

	/**
	 * Accessor to the test cluster
	 */
	private final TestCluster test_cluster = TestCluster.getInstance();

	/**
	 * Keep track of objects we are already trying to generate
	 */
	private final Set<AccessibleObject> current_recursion = new HashSet<AccessibleObject>();

	private DefaultTestFactory() {
	}

	public static DefaultTestFactory getInstance() {
		if (instance == null)
			instance = new DefaultTestFactory();
		return instance;
	}

	public void resetRecursion() {
		current_recursion.clear();
	}

	public void insertRandomCallOnObject(TestCase test, int position) {
		// add call on existing object
		VariableReference object = test.getRandomObject(position);
		if (object != null) {
			List<AccessibleObject> calls = test_cluster.getCallsFor(object.getVariableClass());
			// List<Method> calls = getMethods(object.getVariableClass());
			if (!calls.isEmpty()) {
				AccessibleObject call = calls.get(randomness.nextInt(calls.size()));
				addCallFor(test, object, call, position);
			}
		}
	}

	/**
	 * Insert a random statement at a random position in the test
	 * 
	 * @param test
	 */
	@Override
	public void insertRandomStatement(TestCase test) {
		int position = randomness.nextInt(test.size() + 1);
		final double P = 1d / 3d;

		double r = randomness.nextDouble();

		if (r <= P) {
			// add new call of the UUT - only declared in UUT!
			insertRandomCall(test, position);

		} else if (r <= 2 * P) {
			// add call on existing object
			VariableReference object = test.getRandomObject(position);
			if (object != null) {
				if (object.isArray()) {
					if (object.array_length > 0) {
						// ArrayStatement as = (ArrayStatement)
						// test.getStatement(object.statement);
						// AssignmentStatement as = new
						// AssignmentStatement(object);
						// if(as.size() > 0) {
						// logger.info("Inserting array thingy");
						int index = randomness.nextInt(object.array_length);
						// logger.info("Array statement: "+as.getCode());
						//logger.info("Inserting array index " + index + " at position "
						//       + position + " for array with length "
						//       + object.array_length + ": "
						//      + object.getArray().getName());
						// logger.info("Array thinks it's at position: "+as.retval.statement);
						try {
							assignArray(test, object, index, position);
							// logger.info("Inserted array index");
							// logger.info(test.toCode());
						} catch (ConstructionFailedException e) {
							// logger.info("Failed!");
						}
					}
				} else {
					List<AccessibleObject> calls = test_cluster.getCallsFor(object.getVariableClass());
					// List<Method> calls =
					// getMethods(object.getVariableClass());
					if (!calls.isEmpty()) {
						AccessibleObject call = calls.get(randomness.nextInt(calls.size()));
						addCallFor(test, object, call, position);
					}
				}
			}
		} else // FIXME - not used
		{
			// add call that uses existing object as parameter (consider all
			// possible calls)
			VariableReference object = test.getRandomObject(position);
			if (object != null) {
				if (object.isArrayIndex()) {
					List<AccessibleObject> calls = test_cluster.getTestCallsWith(object.getType());
					if (!calls.isEmpty()) {
						AccessibleObject call = calls.get(randomness.nextInt(calls.size()));
						addCallWith(test, object, call, position);
					}
				} else {
					List<AccessibleObject> calls = test_cluster.getTestCallsWith(object.getType());
					if (!calls.isEmpty()) {
						AccessibleObject call = calls.get(randomness.nextInt(calls.size()));
						addCallWith(test, object, call, position);
					}
				}
			}
		}
	}

	/**
	 * Delete the statement at position from the test case and remove all
	 * references to it
	 * 
	 * @param test
	 * @param position
	 * @throws ConstructionFailedException
	 */
	@Override
	public void deleteStatement(TestCase test, int position)
	        throws ConstructionFailedException {
		logger.trace("Deleting Statement - " + position);
		// logger.info(test.toCode());

		List<VariableReference> references = test.getReferences(test.getReturnValue(position));
		List<Integer> positions = new ArrayList<Integer>();
		Set<Integer> p = new HashSet<Integer>();
		p.add(position);
		for (VariableReference var : references) {
			p.add(var.statement);
			// positions.add(var.statement);
		}
		positions.addAll(p);
		Collections.sort(positions, Collections.reverseOrder());
		for (Integer pos : positions) {
			// logger.info("Deleting statement: "+pos);
			test.remove(pos);
		}

		// logger.trace("DeleteStatement mutation: Var is not referenced, deleting");
		// logger.info("Deleting statement: "+position);
		// test.remove(position);
		// logger.info(test.toCode());
	}

	/**
	 * Append statement s, trying to satisfy parameters
	 * 
	 * Called from TestChromosome when doing crossover
	 * 
	 * @param test
	 * @param s
	 */
	@Override
	public void appendStatement(TestCase test, StatementInterface statement)
	        throws ConstructionFailedException {
		current_recursion.clear();

		if (statement instanceof ConstructorStatement) {
			addConstructor(test, ((ConstructorStatement) statement).constructor,
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

	@Override
	public void deleteStatementGracefully(TestCase test, int position)
	        throws ConstructionFailedException {
		logger.trace("Delete Statement - " + position);

		VariableReference var = test.getReturnValue(position);
		if (var.isArrayIndex()) {
			deleteStatement(test, position);
			return;
		}

		List<VariableReference> alternatives = test.getObjects(var.getType(), position);
		alternatives.remove(var);

		if (!alternatives.isEmpty()) {
			// Change all references to return value at position to something
			// else
			for (int i = position; i < test.size(); i++) {
				StatementInterface s = test.getStatement(i);
				if (s.references(var)) {
					if (s instanceof MethodStatement) {
						MethodStatement ms = (MethodStatement) s;
						if (ms.callee != null) {
							if (ms.callee.equals(var)) {
								VariableReference r = randomness.choice(alternatives);
								ms.callee = r.clone();
								logger.trace("Replacing callee in method call");
							} else if (var.equals(ms.callee.array)) {
								VariableReference r = randomness.choice(alternatives);
								ms.callee.array = r.clone();
								if (r.array_length > 1)
									ms.callee.array_index = randomness.nextInt(r.array_length);
								else
									ms.callee.array_index = 0;
							}
						}
						for (int pos = 0; pos < ms.parameters.size(); pos++) {
							if (ms.parameters.get(pos).equals(var)) {
								VariableReference r = randomness.choice(alternatives);
								ms.parameters.set(pos, r.clone());
								logger.trace("Replacing parameter in method call");
							} else if (var.equals(ms.parameters.get(pos).array)) {
								VariableReference r = randomness.choice(alternatives);
								ms.parameters.get(pos).array = r.clone();
								if (r.array_length > 1)
									ms.parameters.get(pos).array_index = randomness.nextInt(r.array_length);
								else
									ms.parameters.get(pos).array_index = 0;
							}
						}
					} else if (s instanceof ConstructorStatement) {
						ConstructorStatement cs = (ConstructorStatement) s;
						for (int pos = 0; pos < cs.parameters.size(); pos++) {
							if (cs.parameters.get(pos).equals(var)) {
								VariableReference r = randomness.choice(alternatives);
								cs.parameters.set(pos, r.clone());
								logger.trace("Replacing parameter in constructor call");
							} else if (var.equals(cs.parameters.get(pos).array)) {
								VariableReference r = randomness.choice(alternatives);
								cs.parameters.get(pos).array = r.clone();
								if (r.array_length > 1)
									cs.parameters.get(pos).array_index = randomness.nextInt(r.array_length);
								else
									cs.parameters.get(pos).array_index = 0;
							}
						}
					} else if (s instanceof FieldStatement) {
						FieldStatement fs = (FieldStatement) s;
						if (fs.source != null && fs.source.equals(var)) {
							VariableReference r = randomness.choice(alternatives);
							fs.source = r.clone();
							logger.trace("Replacing field source");
						} else if (fs.source != null && var.equals(fs.source.array)) {
							VariableReference r = randomness.choice(alternatives);
							fs.source.array = r.clone();
							if (r.array_length > 1)
								fs.source.array_index = randomness.nextInt(r.array_length);
							else
								fs.source.array_index = 0;
						}
					} else if (s instanceof AssignmentStatement) {
						AssignmentStatement as = (AssignmentStatement) s;
						if (as.retval != null && as.retval.equals(var)) { // TODO:
							                                              // array
							                                              // index
							                                              // might
							                                              // exceed
							                                              // length
							VariableReference r = randomness.choice(alternatives);
							as.retval = r.clone();
							logger.trace("Replacing array source");
						} else if (var.equals(as.retval.array)) {
							VariableReference r = randomness.choice(alternatives);
							as.retval.array = r.clone();
							if (r.array_length > 1)
								as.retval.array_index = randomness.nextInt(r.array_length);
							else
								as.retval.array_index = 0;
						}
						if (as.parameter != null && as.parameter.equals(var)) {
							VariableReference r = randomness.choice(alternatives);
							as.parameter = r.clone();
							logger.trace("Replacing array parameter");
						} else if (var.equals(as.parameter.array)) {
							VariableReference r = randomness.choice(alternatives);
							as.parameter.array = r.clone();
							if (r.array_length > 1)
								as.parameter.array_index = randomness.nextInt(r.array_length);
							else
								as.parameter.array_index = 0;
						}
					}
				}
			}
		}

		// Remove everything else
		deleteStatement(test, position);
	}

	/**
	 * Add constructor at given position if max recursion depth has not been
	 * reached
	 * 
	 * @param constructor
	 * @param position
	 * @param recursion_depth
	 * @return
	 * @throws ConstructionFailedException
	 */
	public VariableReference addConstructor(TestCase test, Constructor<?> constructor,
	        int position, int recursion_depth) throws ConstructionFailedException {
		if (recursion_depth > MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException();
		}
		logger.debug("Adding constructor " + constructor.toGenericString());

		int length = test.size();
		List<VariableReference> parameters = satisfyParameters(test,
		                                                       null,
		                                                       getParameterTypes(constructor),
		                                                       position, recursion_depth);
		int new_length = test.size();
		position += (new_length - length);
		VariableReference ret_val = new VariableReference(
		        constructor.getDeclaringClass(), position);
		test.addStatement(new ConstructorStatement(constructor, ret_val, parameters),
		                  position);

		return ret_val;
	}

	/**
	 * Add method at given position if max recursion depth has not been reached
	 * 
	 * @param test
	 * @param method
	 * @param position
	 * @param recursion_depth
	 * @return
	 * @throws ConstructionFailedException
	 */
	public VariableReference addMethod(TestCase test, Method method, int position,
	        int recursion_depth) throws ConstructionFailedException {
		// System.out.println("TG: Looking for callee of type "+method.getDeclaringClass());
		logger.debug("Recursion depth: " + recursion_depth);
		if (recursion_depth > MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException();
		}
		logger.debug("Adding method " + method.toGenericString());
		int length = test.size();
		VariableReference callee = null;
		List<VariableReference> parameters = null;
		if (!Modifier.isStatic(method.getModifiers())) { // TODO: Consider reuse
			                                             // probability here?
			try {
				// TODO: Would casting be an option here?
				callee = test.getRandomObject(method.getDeclaringClass(), position);
				logger.debug("Found callee of type "
				        + method.getDeclaringClass().getName());
			} catch (ConstructionFailedException e) {
				logger.debug("No callee of type " + method.getDeclaringClass().getName()
				        + " found");
				callee = attemptGeneration(test, method.getDeclaringClass(), position,
				                           recursion_depth, false);
				position += test.size() - length;
				length = test.size();
			}
			parameters = satisfyParameters(test, callee,
			                               getParameterTypes(callee, method), position,
			                               recursion_depth);
		} else {
			parameters = satisfyParameters(test, callee,
			                               getParameterTypes(callee, method), position,
			                               recursion_depth);
		}

		int new_length = test.size();
		position += (new_length - length);

		// VariableReference ret_val = new
		// VariableReference(method.getGenericReturnType(), position);
		VariableReference ret_val = getReturnVariable(method, callee, position);

		test.addStatement(new MethodStatement(method, callee, ret_val, parameters),
		                  position);
		logger.debug("Success: Adding method " + method);

		return ret_val;
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
	@SuppressWarnings("unchecked")
	private VariableReference addPrimitive(TestCase test, PrimitiveStatement<?> old,
	        int position) throws ConstructionFailedException {
		logger.debug("Adding primitive");
		VariableReference ret_val = new VariableReference(old.getReturnType(), position);
		test.addStatement(new PrimitiveStatement(ret_val, old.value), position);

		return ret_val;
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
				callee = test.getRandomObject(field.getDeclaringClass(), position);
			} catch (ConstructionFailedException e) {
				logger.debug("No callee of type " + field.getDeclaringClass().getName()
				        + " found");
				callee = attemptGeneration(test, field.getDeclaringClass(), position, 0,
				                           false);
				position += test.size() - length;
				length = test.size();
			}
		}
		VariableReference ret_val = new VariableReference(field.getGenericType(),
		        position);
		test.addStatement(new FieldStatement(field, callee, ret_val), position);

		return ret_val;
	}

	/**
	 * Satisfy a list of parameters by reusing or creating variables
	 * 
	 * @param test
	 * @param parameter_types
	 * @param position
	 * @param recursion_depth
	 * @param constraints
	 * @return
	 * @throws ConstructionFailedException
	 */
	private List<VariableReference> satisfyParameters(TestCase test,
	        VariableReference callee, List<Type> parameter_types, int position,
	        int recursion_depth) throws ConstructionFailedException {
		List<VariableReference> parameters = new ArrayList<VariableReference>();
		logger.debug("Trying to satisfy " + parameter_types.size() + " parameters");
		int i = 0;
		for (Type parameter_type : parameter_types) {
			double reuse = randomness.nextDouble();
			int previous_length = test.size();

			List<VariableReference> objects = test.getObjects(parameter_type, position);
			if (callee != null)
				objects.remove(callee);
			// if(test.hasObject(parameter_type, position, constraints.get(i))
			// && reuse <= object_reuse_probability) {
			if ((parameter_type instanceof Class<?>)
			        && ((Class<?>) parameter_type).isPrimitive() && !objects.isEmpty()
			        && reuse <= PRIMITIVE_REUSE_PROBABILITY) {
				logger.debug(" Parameter " + i + ": Looking for existing object of type "
				        + parameter_type);
				VariableReference reference = randomness.choice(objects);
				parameters.add(reference);
				logger.debug(" Found object of type: " + reference.getType());
			} else if (!objects.isEmpty() && reuse <= OBJECT_REUSE_PROBABILITY) {
				logger.debug(" Parameter " + i + ": Looking for existing object of type "
				        + parameter_type);
				VariableReference reference = randomness.choice(objects);
				parameters.add(reference);
				logger.debug(" Found object of type: " + reference.getType());

			} else {
				logger.debug(" Parameter " + i + ": Creating new object of type "
				        + parameter_type);

				VariableReference reference = attemptGeneration(test, parameter_type,
				                                                position,
				                                                recursion_depth, true);
				parameters.add(reference);
			}
			int current_length = test.size();
			position += current_length - previous_length;
			i++;
		}
		logger.debug("Satisfied " + parameter_types.size() + " parameters");
		return parameters;
	}

	public void assignArray(TestCase test, VariableReference array, int array_index,
	        int position) throws ConstructionFailedException {
		List<VariableReference> objects = test.getObjects(array.getComponentType(),
		                                                  position);
		Iterator<VariableReference> iterator = objects.iterator();
		while (iterator.hasNext()) {
			VariableReference var = iterator.next();
			if (var.isArrayIndex() && var.array.equals(array))
				iterator.remove();
		}
		assignArray(test, array, array_index, position, objects);
	}

	protected void assignArray(TestCase test, VariableReference array, int array_index,
	        int position, List<VariableReference> objects)
	        throws ConstructionFailedException {
		// VariableReference index = array.getVariable(array_index).clone();
		VariableReference index = new VariableReference(array.clone(), array_index,
		        array.array_length, position);
		// index.statement = position;
		if (!objects.isEmpty() && randomness.nextDouble() <= OBJECT_REUSE_PROBABILITY) {
			// Assign an existing value
			// TODO:
			// Do we need a special "[Array]AssignmentStatement"?
			test.addStatement(new AssignmentStatement(index,
			        randomness.choice(objects).clone()), position);
		} else {
			// Assign a new value
			// Need a primitive, method, constructor, or field statement where
			// retval is set to index
			// Need a version of attemptGeneration that takes retval as
			// parameter

			// OR: Create a new variablereference and then assign it to array
			// (better!)
			int old_len = test.size();
			VariableReference var = attemptGeneration(test, array.getComponentType(),
			                                          position);
			position += test.size() - old_len;
			index.statement = position;
			test.addStatement(new AssignmentStatement(index, var.clone()), position);
		}
	}

	private VariableReference createArray(TestCase test, Type type, int position,
	        int recursion_depth) throws ConstructionFailedException {

		// Create array with random size
		VariableReference reference = new VariableReference(type, position);
		ArrayStatement statement = new ArrayStatement(reference);
		test.addStatement(statement, position);
		position++;

		// For each value of array, call attemptGeneration
		List<VariableReference> objects = test.getObjects(reference.getComponentType(),
		                                                  position);
		for (int i = 0; i < statement.size(); i++) {
			int old_len = test.size();
			assignArray(test, statement.retval, i, position, objects);
			position += test.size() - old_len;
		}
		return reference;

	}

	public VariableReference addTestCall(TestCase test, int position)
	        throws ConstructionFailedException {

		TestSuiteChromosome suite = (TestSuiteChromosome) CurrentChromosomeTracker.getInstance().getCurrentChromosome();

		Set<Integer> candidates = new HashSet<Integer>();
		int num = 0;
		for (TestChromosome testc : suite.tests) {
			if (!testc.test.hasCalls()) {
				// logger.info("Test has no call: ");
				// logger.info(testc.test.toCode());
				if (!test.equals(testc.test))
					candidates.add(num);
			} else {
				// logger.info("Test " + num + " already has call");
				// logger.info(testc.test.toCode());
			}
			num++;
		}
		if (candidates.isEmpty())
			throw new ConstructionFailedException();
		// logger.info("Choice of " + candidates.size() + " chromosomes out of "
		// + suite.size());
		num = randomness.choice(candidates);
		logger.info("Chosen " + num + " out of " + candidates.size()
		        + " chromosomes out of " + suite.size());
		TestCallObject call = new TestCallObject(num);
		VariableReference retval = new VariableReference(Properties.getTargetClass(),
		        position);
		TestCallStatement statement = new TestCallStatement(call, retval);
		test.addStatement(statement, position);
		// logger.info("Added call to test: " + call.getTest().toCode());
		return retval;
	}

	public VariableReference attemptGeneration(TestCase test, Type type, int position)
	        throws ConstructionFailedException {
		return attemptGeneration(test, type, position, 0, false);
	}

	/**
	 * Try to generate an object of a given type
	 * 
	 * @param test
	 * @param type
	 * @param position
	 * @param recursion_depth
	 * @param constraint
	 * @param allow_null
	 * @return
	 * @throws ConstructionFailedException
	 */
	private VariableReference attemptGeneration(TestCase test, Type type, int position,
	        int recursion_depth, boolean allow_null) throws ConstructionFailedException {
		GenericClass clazz = new GenericClass(type);

		if (clazz.isPrimitive() || clazz.isString()) {
			if (logger.isDebugEnabled())
				logger.debug("Generating primitive of type "
				        + ((Class<?>) type).getName());
			VariableReference reference = new VariableReference(type, position); // -1;?

			test.addStatement(PrimitiveStatement.getRandomStatement(reference, type),
			                  position);
			return reference.clone();
		} else if (clazz.isArray()) {
			return createArray(test, type, position, recursion_depth).clone();

		} else {
			if (allow_null && randomness.nextDouble() <= NULL_PROBABILITY) {
				return new NullReference(type);
			}

			if (!test.hasCalls() && randomness.nextDouble() <= CALL_PROBABILITY) {
				return addTestCall(test, position).clone();
			}

			AccessibleObject o = test_cluster.getRandomGenerator(type, current_recursion);
			current_recursion.add(o);
			if (o == null) {
				throw new ConstructionFailedException();
			} else if (o instanceof Field) {
				logger.debug("Attempting generating of " + type + " via field of type "
				        + type);
				VariableReference ret = addField(test, (Field) o, position);
				logger.debug("Success in generating type " + type);
				return ret.clone();
			} else if (o instanceof Method) {
				logger.debug("Attempting generating of " + type + " via method "
				        + ((Method) o).getName() + " of type " + type);
				VariableReference ret = addMethod(test, (Method) o, position,
				                                  recursion_depth + 1);
				logger.debug("Success in generating type " + type);
				return ret.clone();
			} else if (o instanceof Constructor<?>) {
				logger.debug("Attempting generating of " + type + " via constructor "
				        + ((Constructor<?>) o).getName() + " of type " + type);
				VariableReference ret = addConstructor(test, (Constructor<?>) o,
				                                       position, recursion_depth + 1);
				logger.debug("Success in generating type " + type);
				return ret.clone();
			} else {
				logger.debug("No generators found for type " + type);
				throw new ConstructionFailedException();
			}
		}
		// TODO: Sometimes we could use null
	}

	/**
	 * Append given call to the test case at given position
	 * 
	 * @param test
	 * @param call
	 * @param position
	 */
	private void addCallFor(TestCase test, VariableReference callee,
	        AccessibleObject call, int position) {
		int previous_length = test.size();
		current_recursion.clear();
		try {
			if (call instanceof Method) {
				addMethodFor(test, callee, (Method) call, position);
			}
		} catch (ConstructionFailedException e) {
			// TODO: Check this!
			logger.debug("Inserting call " + call + " has failed. Removing statements");
			// System.out.println("TG: Failed");
			// TODO: Doesn't work if position != test.size()
			while (test.size() != previous_length) {
				logger.debug("  Removing statement: "
				        + test.getStatement(position).getCode());
				test.remove(position);
			}
		}
	}

	/**
	 * Append given call to the test case at given position
	 * 
	 * @param test
	 * @param call
	 * @param position
	 */
	private void addCallWith(TestCase test, VariableReference parameter,
	        AccessibleObject call, int position) {
		int previous_length = test.size();
		current_recursion.clear();
		try {
			if (call instanceof Method) {
				addMethodWith(test, parameter, (Method) call, position);
			} else if (call instanceof Constructor<?>) {
				addConstructorWith(test, parameter, (Constructor<?>) call, position);
			}
		} catch (ConstructionFailedException e) {
			// TODO: Check this!
			logger.debug("Inserting call " + call + " has failed. Removing statements");
			// System.out.println("TG: Failed");
			// TODO: Doesn't work if position != test.size()
			while (test.size() != previous_length) {
				logger.debug("  Removing statement: "
				        + test.getStatement(position).getCode());
				test.remove(position);
			}
		}
	}

	/**
	 * Insert a random call at given position
	 * 
	 * @param test
	 * @param position
	 */
	public void insertRandomCall(TestCase test, int position) {
		int previous_length = test.size();
		String name = "";
		current_recursion.clear();
		try {
			// AccessibleObject o = mapping.getRandomCall();
			AccessibleObject o = test_cluster.getRandomTestCall();
			if (o instanceof Constructor<?>) {
				Constructor<?> c = (Constructor<?>) o;
				logger.debug("Adding constructor call " + c.getName());
				name = c.getName();
				addConstructor(test, c, position, 0);
			} else if (o instanceof Method) {
				Method m = (Method) o;
				logger.debug("Adding method call " + m.getName());
				name = m.getName();
				addMethod(test, m, position, 0);
			} else {
				logger.error("Got type other than method or constructor!");
			}
		} catch (ConstructionFailedException e) {
			// TODO: Check this!
			logger.debug("Inserting statement " + name
			        + " has failed. Removing statements");
			// System.out.println("TG: Failed");
			// TODO: Doesn't work if position != test.size()
			while (test.size() != previous_length) {
				logger.debug("  Removing statement: "
				        + test.getStatement(position).getCode());
				test.remove(position);
			}

			// logger.info("Attempting search");
			// test.chop(previous_length);
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
	@Override
	public void changeCall(TestCase test, StatementInterface statement, AccessibleObject call)
	        throws ConstructionFailedException {
		int position = statement.getReturnValue().statement;

		if (call instanceof Method) {
			Method method = (Method) call;
			VariableReference retval = statement.getReturnValue();
			VariableReference callee = null;
			if (!Modifier.isStatic(method.getModifiers()))
				callee = test.getRandomObject(method.getDeclaringClass(), position);
			List<VariableReference> parameters = new ArrayList<VariableReference>();
			for (Type type : getParameterTypes(callee, method)) {
				parameters.add(test.getRandomObject(type, position));
			}
			MethodStatement m = new MethodStatement(method, callee, retval, parameters);
			test.setStatement(m, position);

		} else if (call instanceof Constructor<?>) {

			Constructor<?> constructor = (Constructor<?>) call;
			VariableReference retval = statement.getReturnValue();
			List<VariableReference> parameters = new ArrayList<VariableReference>();
			for (Type type : getParameterTypes(constructor)) {
				parameters.add(test.getRandomObject(type, position));
			}
			ConstructorStatement c = new ConstructorStatement(constructor, retval,
			        parameters);
			test.setStatement(c, position);

		} else if (call instanceof Field) {
			Field field = (Field) call;
			VariableReference retval = statement.getReturnValue();
			VariableReference source = null;
			if (!Modifier.isStatic(field.getModifiers()))
				source = test.getRandomObject(field.getDeclaringClass(), position);

			FieldStatement f = new FieldStatement(field, source, retval);
			test.setStatement(f, position);

		}
	}

	private VariableReference getReturnVariable(Method method, VariableReference callee,
	        int position) {

		VariableReference ret_val = new VariableReference(method.getGenericReturnType(),
		        position);

		// Casting
		// Case 1: clone() on cloneable -> cast to type of defining class
		if (method.getReturnType().equals(Object.class)
		        && method.getName().equals("clone")) {
			ret_val.setType(method.getDeclaringClass());
			logger.debug("Found clone method: Changing type to: " + ret_val.getType());
			// TODO: Need to cast this!
			return ret_val;
		}

		if (callee == null)
			ret_val.setType(descriptor_replacement.getReturnType(method.getDeclaringClass().getName(),
			                                                     method));
		else
			ret_val.setType(descriptor_replacement.getReturnType(callee.getVariableClass().getName(),
			                                                     method));

		return ret_val;
	}

	private List<Type> getParameterTypes(VariableReference callee, Method method) {
		if (callee == null)
			return descriptor_replacement.getParameterTypes(method);
		else
			return descriptor_replacement.getParameterTypes(callee.getType(), method);
	}

	private List<Type> getParameterTypes(Constructor<?> constructor) {
		return descriptor_replacement.getParameterTypes(constructor);
	}

	public VariableReference addConstructorWith(TestCase test,
	        VariableReference parameter, Constructor<?> constructor, int position)
	        throws ConstructionFailedException {

		logger.debug("Adding constructor " + constructor.toGenericString());

		int length = test.size();
		List<VariableReference> parameters = satisfyParameters(test,
		                                                       null,
		                                                       getParameterTypes(constructor),
		                                                       position, 0);
		// Force one parameter to be "parameter"
		if (!parameters.contains(parameter)) {
			int num = 0;
			for (Type type : constructor.getGenericParameterTypes()) {
				if (parameter.isAssignableTo(type)) {
					parameters.set(num, parameter);
					break;
				}
				num++;
			}
		}
		int new_length = test.size();
		position += (new_length - length);
		VariableReference ret_val = new VariableReference(
		        constructor.getDeclaringClass(), position);
		test.addStatement(new ConstructorStatement(constructor, ret_val, parameters),
		                  position);

		return ret_val;
	}

	public VariableReference addMethodFor(TestCase test, VariableReference callee,
	        Method method, int position) throws ConstructionFailedException {
		logger.debug("Adding method " + method.toGenericString());
		current_recursion.clear();
		int length = test.size();
		List<VariableReference> parameters = null;
		parameters = satisfyParameters(test, callee, getParameterTypes(callee, method),
		                               position, 0);
		int new_length = test.size();
		position += (new_length - length);
		// VariableReference ret_val = new
		// VariableReference(method.getGenericReturnType(), position);
		VariableReference ret_val = getReturnVariable(method, callee, position);
		test.addStatement(new MethodStatement(method, callee, ret_val, parameters),
		                  position);

		logger.debug("Success: Adding method " + method);
		return ret_val;
	}

	public VariableReference addMethodWith(TestCase test, VariableReference parameter,
	        Method method, int position) throws ConstructionFailedException {
		logger.debug("Adding method " + method.toGenericString());
		int length = test.size();
		VariableReference callee = null;
		List<VariableReference> parameters = null;
		if (!Modifier.isStatic(method.getModifiers())) { // TODO: Consider reuse
			                                             // probability here?
			try {
				callee = test.getRandomObject(method.getDeclaringClass(), position);
				logger.debug("Found callee of type "
				        + method.getDeclaringClass().getName());
			} catch (ConstructionFailedException e) {
				logger.debug("No callee of type " + method.getDeclaringClass().getName()
				        + " found at position " + position);
				callee = attemptGeneration(test, method.getDeclaringClass(), position, 0,
				                           false);
				position += test.size() - length;
				length = test.size();
			}
		}
		parameters = satisfyParameters(test, callee, getParameterTypes(callee, method),
		                               position, 0);

		// Force one parameter to be "parameter"
		if (!parameters.contains(parameter)) {
			int num = 0;
			for (Type type : getParameterTypes(callee, method)) {
				if (parameter.isAssignableTo(type)) {
					parameters.set(num, parameter);
					break;
				}
				num++;
			}
		}
		// System.out.println("TG: Found callee for method call "+method.getName());
		// System.out.println("TG: Found parameters for method call "+method.getName());
		position += (test.size() - length);

		// VariableReference ret_val = new
		// VariableReference(method.getGenericReturnType(), position);
		VariableReference ret_val = getReturnVariable(method, callee, position);
		test.addStatement(new MethodStatement(method, callee, ret_val, parameters),
		                  position);

		logger.debug("Success: Adding method " + method);
		return ret_val;
	}

	private Set<Type> getDependencies(Method method) {
		Set<Type> dependencies = new HashSet<Type>();
		if (!Modifier.isStatic(method.getModifiers())) {
			dependencies.add(method.getDeclaringClass());
		}
		for (Type type : method.getGenericParameterTypes()) {
			dependencies.add(type);
		}

		return dependencies;
	}

	private Set<Type> getDependencies(Constructor<?> constructor) {
		Set<Type> dependencies = new HashSet<Type>();
		for (Type type : constructor.getGenericParameterTypes()) {
			dependencies.add(type);
		}

		return dependencies;
	}

	private Set<Type> getDependencies(Field field) {
		Set<Type> dependencies = new HashSet<Type>();
		if (!Modifier.isStatic(field.getModifiers())) {
			dependencies.add(field.getDeclaringClass());
		}

		return dependencies;
	}

	private boolean dependenciesSatisfied(Set<Type> dependencies,
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

	// TODO: Return all possible statements that have this return type
	private List<AccessibleObject> getPossibleCalls(Type return_type,
	        List<VariableReference> objects) {
		List<AccessibleObject> calls = new ArrayList<AccessibleObject>();
		List<AccessibleObject> all_calls;

		try {
			all_calls = test_cluster.getGenerators(return_type);
		} catch (ConstructionFailedException e) {
			return calls;
		}

		for (AccessibleObject call : all_calls) {
			Set<Type> dependencies = null;
			if (call instanceof Method) {
				dependencies = getDependencies((Method) call);
			} else if (call instanceof Constructor<?>) {
				dependencies = getDependencies((Constructor<?>) call);
			} else if (call instanceof Field) {
				dependencies = getDependencies((Field) call);
			} else {
				assert (false);
			}
			if (dependenciesSatisfied(dependencies, objects))
				calls.add(call);
		}

		// TODO: What if primitive?

		return calls;
	}

	@Override
	public boolean changeRandomCall(TestCase test, StatementInterface statement) {
		List<VariableReference> objects = test.getObjects(statement.getReturnValue().statement);
		objects.remove(statement.getReturnValue());
		List<AccessibleObject> calls = getPossibleCalls(statement.getReturnType(),
		                                                objects);

		if (statement instanceof MethodStatement) {
			MethodStatement ms = (MethodStatement) statement;
			calls.remove(ms.getMethod());
		} else if (statement instanceof ConstructorStatement) {
			ConstructorStatement cs = (ConstructorStatement) statement;
			calls.remove(cs.getConstructor());
		} else if (statement instanceof FieldStatement) {
			FieldStatement fs = (FieldStatement) statement;
			calls.remove(fs.getField());
		}

		logger.debug("Got " + calls.size() + " possible calls for " + objects.size()
		        + " objects");
		AccessibleObject call = randomness.choice(calls);
		try {
			if (statement instanceof TestCallStatement)
				logger.info("Changing testcall statement");
			changeCall(test, statement, call);
			return true;
		} catch (ConstructionFailedException e) {
			// Ignore
			logger.info("Change failed");
		}
		return false;

	}

}
