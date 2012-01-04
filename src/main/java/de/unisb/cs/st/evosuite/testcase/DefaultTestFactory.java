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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.gentyref.GenericTypeReflector;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.primitives.ObjectPool;
import de.unisb.cs.st.evosuite.testsuite.CurrentChromosomeTracker;
import de.unisb.cs.st.evosuite.testsuite.TestCallObject;
import de.unisb.cs.st.evosuite.testsuite.TestCallStatement;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;
import de.unisb.cs.st.evosuite.utils.Randomness;

/**
 * Handle test case generation
 * 
 * @author Gordon Fraser
 * 
 */
public class DefaultTestFactory extends AbstractTestFactory {

	private static final long serialVersionUID = 6238144737328754849L;

	private static DefaultTestFactory instance = null;

	private static Logger logger = LoggerFactory.getLogger(DefaultTestFactory.class);

	private transient MethodDescriptorReplacement descriptor_replacement = MethodDescriptorReplacement.getInstance();

	/**
	 * Accessor to the test cluster
	 */
	private transient TestCluster testCluster = TestCluster.getInstance();

	/**
	 * Keep track of objects we are already trying to generate
	 */
	private transient Set<AccessibleObject> currentRecursion = new HashSet<AccessibleObject>();

	private DefaultTestFactory() {
	}

	public static DefaultTestFactory getInstance() {
		if (instance == null)
			instance = new DefaultTestFactory();
		return instance;
	}

	public void resetRecursion() {
		currentRecursion.clear();
	}

	public void insertRandomCallOnObject(TestCase test, int position) {
		// add call on existing object
		VariableReference object = test.getRandomObject(position);
		if (object != null) {
			List<AccessibleObject> calls = testCluster.getCallsFor(object.getVariableClass());

			if (!calls.isEmpty()) {
				AccessibleObject call = Randomness.choice(calls);
				addCallFor(test, object, call, position);
			}
		}
	}

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

			if (dist >= rnd)
				return test.getStatement(i).getReturnValue();
			else
				rnd = rnd - dist;
		}

		if (position > 0) {
			int i = Randomness.nextInt(position);
			return test.getStatement(i).getReturnValue();
		} else {
			return test.getStatement(0).getReturnValue();
		}
	}

	/**
	 * Insert a random statement at a random position in the test
	 * 
	 * @param test
	 */
	@Override
	public void insertRandomStatement(TestCase test) {
		final double P = 1d / 3d;

		double r = Randomness.nextDouble();
		int position = Randomness.nextInt(test.size() + 1);

		for (int i = 0; i < test.size(); i++) {
			logger.debug(test.getStatement(i).getCode() + ": Distance = "
			        + test.getStatement(i).getReturnValue().getDistance());
		}

		if (r <= P) {
			// add new call of the UUT - only declared in UUT!
			logger.debug("Adding new call on UUT");
			insertRandomCall(test, position);

		} else if (r <= 2 * P) {
			// Select a random variable
			VariableReference var = selectVariableForCall(test, position);
			if (var != null)
				logger.debug("Inserting call at position " + position + ", chosen var: "
				        + var.getName() + ", distance: " + var.getDistance()
				        + ", class: " + var.getClassName());
			// Add call for this variable at random position
			if (var != null) {
				logger.debug("Chosen object: " + var.getName());
				if (var instanceof ArrayReference) {
					logger.debug("Chosen object is array ");
					ArrayReference array = (ArrayReference) var;
					if (array.getArrayLength() > 0) {
						int index = Randomness.nextInt(array.getArrayLength());
						try {
							assignArray(test, var, index, position);
						} catch (ConstructionFailedException e) {
							// logger.info("Failed!");
						}
					}
				} else {
					logger.debug("Getting calls for object " + var.toString());
					List<AccessibleObject> calls = testCluster.getCallsFor(var.getVariableClass());
					if (!calls.isEmpty()) {
						AccessibleObject call = calls.get(Randomness.nextInt(calls.size()));
						logger.debug("Chosen call " + call);
						addCallFor(test, var, call, position);
						logger.debug("Done adding call " + call);
					}

				}
			} else {
				logger.debug("Adding new call on UUT");
				insertRandomCall(test, position);
			}
		} else // FIXME - not used
		{
			// add call that uses existing object as parameter (consider all
			// possible calls)
			VariableReference object = test.getRandomObject(position);
			boolean mutated = false;
			if (object != null) {
				if (object instanceof ArrayIndex) {
					List<AccessibleObject> calls = testCluster.getTestCallsWith(object.getType());
					if (!calls.isEmpty()) {
						AccessibleObject call = calls.get(Randomness.nextInt(calls.size()));
						addCallWith(test, object, call, position);
						mutated = true;
					}
				} else {
					List<AccessibleObject> calls = testCluster.getTestCallsWith(object.getType());
					if (!calls.isEmpty()) {
						AccessibleObject call = calls.get(Randomness.nextInt(calls.size()));
						addCallWith(test, object, call, position);
						mutated = true;
					}
				}
			}
			if (!mutated)
				insertRandomCall(test, position);
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
		logger.debug("Deleting target statement - " + position);
		//logger.info(test.toCode());

		Set<VariableReference> references = new HashSet<VariableReference>();
		Set<Integer> positions = new HashSet<Integer>();
		positions.add(position);
		references.add(test.getReturnValue(position));
		for (int i = position; i < test.size(); i++) {
			Set<VariableReference> temp = new HashSet<VariableReference>();
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

		/*
				Set<VariableReference> references = test.getReferences(test.getReturnValue(position));
				List<Integer> positions = new ArrayList<Integer>();
				Set<Integer> p = new HashSet<Integer>();
				p.add(position);
				for (VariableReference var : references) {
					if (!(var instanceof ConstantValue))
						p.add(var.getStPosition());
					// positions.add(var.statement);
				}
				positions.addAll(p);
				Collections.sort(positions, Collections.reverseOrder());
				for (Integer pos : positions) {
					test.remove(pos);
				}
				// logger.trace("DeleteStatement mutation: Var is not referenced, deleting");
				// logger.info("Deleting statement: "+position);
				// test.remove(position);
		*/
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
		//new PrimitiveStatement(test, old.getReturnType(),
		//        old.value); // TODO: Check
		return test.addStatement(st, position);
	}

	@Override
	public void deleteStatementGracefully(TestCase test, int position)
	        throws ConstructionFailedException {
		logger.debug("Delete Statement - " + position);
		assert (test.isValid());

		VariableReference var = test.getReturnValue(position);
		if (var instanceof ArrayIndex) {
			deleteStatement(test, position);
			return;
		}

		// Get possible replacements
		List<VariableReference> alternatives = test.getObjects(var.getType(), position);

		// Remove self, and all field or array references to self
		alternatives.remove(var);
		Iterator<VariableReference> replacement = alternatives.iterator();
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

		assert (test.isValid());

		// Remove everything else
		deleteStatement(test, position);
		assert (test.isValid());

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
		if (recursion_depth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException("Max recursion depth reached");
		}
		logger.debug("Adding constructor " + constructor.toGenericString());

		int length = test.size();
		List<VariableReference> parameters = satisfyParameters(test,
		                                                       null,
		                                                       getParameterTypes(constructor),
		                                                       position,
		                                                       recursion_depth + 1);
		int new_length = test.size();
		position += (new_length - length);

		StatementInterface st = new ConstructorStatement(test, constructor,
		        constructor.getDeclaringClass(), parameters);
		return test.addStatement(st, position);
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
		if (recursion_depth > Properties.MAX_RECURSION) {
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
					callee = test.getRandomObject(method.getDeclaringClass(), position);
					logger.debug("Found callee of type "
					        + method.getDeclaringClass().getName() + ": "
					        + callee.getName());
				} catch (ConstructionFailedException e) {
					logger.debug("No callee of type "
					        + method.getDeclaringClass().getName() + " found");
					callee = attemptGeneration(test, method.getDeclaringClass(),
					                           position, recursion_depth, false);
					position += test.size() - length;
					length = test.size();
				}
				parameters = satisfyParameters(test, callee,
				                               getParameterTypes(callee, method),
				                               position, recursion_depth + 1);
			} else {
				parameters = satisfyParameters(test, callee,
				                               getParameterTypes(callee, method),
				                               position, recursion_depth + 1);
			}
		} catch (ConstructionFailedException e) {
			testCluster.checkDependencies(method);
			throw e;
		}

		int new_length = test.size();
		position += (new_length - length);
		// VariableReference ret_val = new
		// VariableReference(method.getGenericReturnType(), position);
		Type ret_val_type = getReturnVariable(method, callee);

		StatementInterface st = new MethodStatement(test, method, callee, ret_val_type,
		        parameters);
		VariableReference ret = test.addStatement(st, position);
		if (callee != null)
			ret.setDistance(callee.getDistance() + 1);
		return ret;
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
	public VariableReference addFieldAssignment(TestCase test, Field field, int position,
	        int recursion_depth) throws ConstructionFailedException {
		logger.debug("Recursion depth: " + recursion_depth);
		if (recursion_depth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException("Max recursion depth reached");
		}
		logger.debug("Adding field " + field.toGenericString());// + " to test "; 
		//        + test.toCode() + " at position " + position);
		int length = test.size();
		VariableReference callee = null;
		if (!Modifier.isStatic(field.getModifiers())) { // TODO: Consider reuse
			                                            // probability here?
			try {
				// TODO: Would casting be an option here?
				callee = test.getRandomObject(field.getDeclaringClass(), position);
				logger.debug("Found callee of type "
				        + field.getDeclaringClass().getName());
			} catch (ConstructionFailedException e) {
				logger.debug("No callee of type " + field.getDeclaringClass().getName()
				        + " found");
				callee = attemptGeneration(test, field.getDeclaringClass(), position,
				                           recursion_depth, false);
				position += test.size() - length;
				length = test.size();
			}
		}

		VariableReference var = createOrReuseVariable(test, field.getGenericType(),
		                                              position, recursion_depth, callee);
		int new_length = test.size();
		position += (new_length - length);

		FieldReference f = new FieldReference(test, field, callee);

		StatementInterface st = new AssignmentStatement(test, f, var);
		//logger.info("FIeld assignment: " + st.getCode());
		VariableReference ret = test.addStatement(st, position);
		assert (test.isValid());
		return ret;
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

		StatementInterface st = new FieldStatement(test, field, callee,
		        field.getGenericType());

		return test.addStatement(st, position);
	}

	private VariableReference createOrReuseVariable(TestCase test, Type parameter_type,
	        int position, int recursion_depth, VariableReference exclude)
	        throws ConstructionFailedException {
		double reuse = Randomness.nextDouble();

		List<VariableReference> objects = test.getObjects(parameter_type, position);
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

		Class<?> clazz = GenericTypeReflector.erase(parameter_type);
		if ((clazz.isPrimitive() || clazz.isEnum()) && !objects.isEmpty()
		        && reuse <= Properties.PRIMITIVE_REUSE_PROBABILITY) {
			logger.debug(" Looking for existing object of type " + parameter_type);
			VariableReference reference = Randomness.choice(objects);
			return reference;

		} else if (!clazz.isPrimitive()
		        && !clazz.isEnum()
		        && !objects.isEmpty()
		        && ((reuse <= Properties.OBJECT_REUSE_PROBABILITY) || !testCluster.hasGenerator(parameter_type))) {

			logger.debug(" Choosing from " + objects.size() + " existing objects");
			VariableReference reference = Randomness.choice(objects);
			logger.debug(" Using existing object of type " + parameter_type + ": "
			        + reference);
			return reference;

		} else {
			logger.debug(" Generating new object of type " + parameter_type);
			VariableReference reference = attemptGeneration(test, parameter_type,
			                                                position, recursion_depth,
			                                                true);
			return reference;
		}
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
		for (Type parameter_type : parameter_types) {
			int previous_length = test.size();

			VariableReference var = createOrReuseVariable(test, parameter_type, position,
			                                              recursion_depth, callee);
			parameters.add(var);

			int current_length = test.size();
			position += current_length - previous_length;
		}
		logger.debug("Satisfied " + parameter_types.size() + " parameters");
		return parameters;
	}

	public void assignArray(TestCase test, VariableReference array, int array_index,
	        int position) throws ConstructionFailedException {
		//logger.info("Array " + array.getSimpleClassName() + " " + array.getName() + " - "
		//        + array.getComponentType());
		List<VariableReference> objects = test.getObjects(array.getComponentType(),
		                                                  position);
		Iterator<VariableReference> iterator = objects.iterator();
		while (iterator.hasNext()) {
			VariableReference var = iterator.next();
			if (var instanceof ArrayIndex && ((ArrayIndex) var).getArray().equals(array))
				iterator.remove();
		}
		assignArray(test, array, array_index, position, objects);
	}

	protected void assignArray(TestCase test, VariableReference array, int array_index,
	        int position, List<VariableReference> objects)
	        throws ConstructionFailedException {
		assert (array instanceof ArrayReference);
		ArrayReference arrRef = (ArrayReference) array;
		// VariableReference index = array.getVariable(array_index).clone();

		// index.statement = position;
		if (!objects.isEmpty()
		        && Randomness.nextDouble() <= Properties.OBJECT_REUSE_PROBABILITY) {
			// Assign an existing value
			// TODO:
			// Do we need a special "[Array]AssignmentStatement"?
			logger.debug("Reusing value");

			ArrayIndex index = new ArrayIndex(test, arrRef, array_index);
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
			int old_len = test.size();
			logger.debug("Attempting generation of object of type "
			        + array.getComponentType());
			VariableReference var = attemptGeneration(test, array.getComponentType(),
			                                          position);
			position += test.size() - old_len;
			ArrayIndex index = new ArrayIndex(test, arrRef, array_index);
			StatementInterface st = new AssignmentStatement(test, index, var);
			test.addStatement(st, position);
		}
	}

	private VariableReference createArray(TestCase test, Type type, int position,
	        int recursion_depth) throws ConstructionFailedException {

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
			int old_len = test.size();
			assignArray(test, reference, i, position, objects);
			position += test.size() - old_len;
		}
		reference.setDistance(recursion_depth);
		return reference;

	}

	public VariableReference addTestCall(TestCase test, int position)
	        throws ConstructionFailedException {

		TestSuiteChromosome suite = (TestSuiteChromosome) CurrentChromosomeTracker.getInstance().getCurrentChromosome();

		Set<Integer> candidates = new HashSet<Integer>();
		int num = 0;
		for (TestChromosome testc : suite.getTestChromosomes()) {
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
			throw new ConstructionFailedException("Max recursion depth reached");
		// logger.info("Choice of " + candidates.size() + " chromosomes out of "
		// + suite.size());
		num = Randomness.choice(candidates);
		logger.info("Chosen " + num + " out of " + candidates.size()
		        + " chromosomes out of " + suite.size());
		TestCallObject call = new TestCallObject(num);
		TestCallStatement statement = new TestCallStatement(test, call,
		        Properties.getTargetClass());
		return test.addStatement(statement, position);
	}

	public VariableReference attemptGeneration(TestCase test, Type type, int position)
	        throws ConstructionFailedException {
		return attemptGeneration(test, type, position, 0, false);
	}

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
	 * @param recursion_depth
	 * @param constraint
	 * @param allow_null
	 * @return
	 * @throws ConstructionFailedException
	 */
	private VariableReference attemptGeneration(TestCase test, Type type, int position,
	        int recursion_depth, boolean allow_null) throws ConstructionFailedException {
		GenericClass clazz = new GenericClass(type);

		if (clazz.isPrimitive() || clazz.isString() || clazz.isEnum()) {
			if (logger.isDebugEnabled())
				logger.debug("Generating primitive of type "
				        + ((Class<?>) type).getName());
			//VariableReference reference = new VariableReference(type, position); // TODO: Is this correct? -1;?
			StatementInterface st = PrimitiveStatement.getRandomStatement(test, type,
			                                                              position, type);
			VariableReference ret = test.addStatement(st, position);
			ret.setDistance(recursion_depth);
			return ret;
		} else if (clazz.isArray()) {
			return createArray(test, type, position, recursion_depth);
		} else {
			if (allow_null && Randomness.nextDouble() <= Properties.NULL_PROBABILITY) {
				logger.debug("Using a null reference to satisfy the type: " + type);
				StatementInterface st = new NullStatement(test, type);
				test.addStatement(st, position);
				VariableReference ret = test.getStatement(position).getReturnValue();
				ret.setDistance(recursion_depth);
				return ret;
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

			if (!test.hasCalls()
			        && Randomness.nextDouble() <= Properties.CALL_PROBABILITY) {
				logger.debug("adding a test call, at position: " + position);
				return addTestCall(test, position);
			}

			AccessibleObject o = testCluster.getRandomGenerator(type, currentRecursion);
			currentRecursion.add(o);
			if (o == null) {
				if (!testCluster.hasGenerator(type)) {
					logger.debug("We have no generator for class " + type);
				}
				throw new ConstructionFailedException("Generator is null");
			} else if (o instanceof Field) {
				logger.debug("Attempting generating of " + type + " via field of type "
				        + type);
				VariableReference ret = addField(test, (Field) o, position);
				ret.setDistance(recursion_depth + 1);
				logger.debug("Success in generating type " + type);
				return ret;
			} else if (o instanceof Method) {
				logger.debug("Attempting generating of " + type + " via method "
				        + ((Method) o).getName() + " of type " + type);
				VariableReference ret = addMethod(test, (Method) o, position,
				                                  recursion_depth + 1);
				logger.debug("Success in generating type " + type);
				ret.setDistance(recursion_depth + 1);
				return ret;
			} else if (o instanceof Constructor<?>) {
				logger.debug("Attempting generating of " + type + " via constructor "
				        + ((Constructor<?>) o).getName() + " of type " + type);
				VariableReference ret = addConstructor(test, (Constructor<?>) o,
				                                       position, recursion_depth + 1);
				logger.debug("Success in generating type " + type);
				ret.setDistance(recursion_depth + 1);

				return ret;
			} else {
				logger.debug("No generators found for type " + type);
				throw new ConstructionFailedException("No generator found for type "
				        + type);
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
		logger.trace("addCallFor " + callee.getName());

		int previous_length = test.size();
		currentRecursion.clear();
		try {
			if (call instanceof Method) {
				addMethodFor(test, callee, (Method) call, position);
			} else if (call instanceof Field) {
				addFieldFor(test, callee, (Field) call, position);
			}
		} catch (ConstructionFailedException e) {
			// TODO: Check this!
			logger.debug("Inserting call " + call + " has failed. Removing statements");
			// System.out.println("TG: Failed");
			// TODO: Doesn't work if position != test.size()
			int length_difference = test.size() - previous_length;
			for (int i = length_difference - 1; i >= 0; i--) { //we need to remove them in order, so that the testcase is at all time consistent 
				logger.debug("  Removing statement: "
				        + test.getStatement(position + i).getCode());
				test.remove(position + i);
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
		currentRecursion.clear();
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
			int length_difference = test.size() - previous_length;
			for (int i = length_difference - 1; i >= 0; i--) { //we need to remove them in order, so that the testcase is at all time consistent 
				logger.debug("  Removing statement: "
				        + test.getStatement(position + i).getCode());
				test.remove(position + i);
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
		currentRecursion.clear();
		logger.debug("Inserting random call at position " + position);
		AccessibleObject o = testCluster.getRandomTestCall();
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
				addMethod(test, m, position, 0);
			} else if (o instanceof Field) {
				Field f = (Field) o;
				//logger.info("Adding field assignment " + f.getName());
				name = f.getName();
				addFieldAssignment(test, f, position, 0);
			} else {
				logger.error("Got type other than method or constructor!");
			}
		} catch (ConstructionFailedException e) {
			// TODO: Check this!
			testCluster.checkDependencies(o);
			logger.debug("Inserting statement " + name
			        + " has failed. Removing statements");
			// System.out.println("TG: Failed");
			// TODO: Doesn't work if position != test.size()
			int length_difference = test.size() - previous_length;
			for (int i = length_difference - 1; i >= 0; i--) { //we need to remove them in order, so that the testcase is at all time consistent 
				logger.debug("  Removing statement: "
				        + test.getStatement(position + i).getCode());
				test.remove(position + i);
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
	public void changeCall(TestCase test, StatementInterface statement,
	        AccessibleObject call) throws ConstructionFailedException {
		int position = statement.getReturnValue().getStPosition();

		logger.debug("Changing call " + test.getStatement(position) + " with " + call);

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
			MethodStatement m = new MethodStatement(test, method, callee, retval,
			        parameters);
			logger.debug("Using method " + m.getCode());

			test.setStatement(m, position);

		} else if (call instanceof Constructor<?>) {

			Constructor<?> constructor = (Constructor<?>) call;
			VariableReference retval = statement.getReturnValue();
			List<VariableReference> parameters = new ArrayList<VariableReference>();
			for (Type type : getParameterTypes(constructor)) {
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
				source = test.getRandomObject(field.getDeclaringClass(), position);

			try {
				FieldStatement f = new FieldStatement(test, field, source, retval);
				logger.debug("Using field " + f.getCode());

				test.setStatement(f, position);
			} catch (Throwable e) {

				logger.warn("Error: " + e);
				e.printStackTrace();
				logger.warn("Field: " + field);
				logger.warn("Test: " + test);
				System.exit(0);
			}
		}
	}

	private Type getReturnVariable(Method method, VariableReference callee) {

		Type ret_val = method.getGenericReturnType();

		// Casting
		// Case 1: clone() on cloneable -> cast to type of defining class
		if (method.getReturnType().equals(Object.class)
		        && method.getName().equals("clone")) {
			ret_val = method.getDeclaringClass();
			logger.debug("Found clone method: Changing type to: " + ret_val);
			// TODO: Need to cast this!
			return ret_val;
		}

		if (callee == null)
			ret_val = descriptor_replacement.getReturnType(method.getDeclaringClass().getName(),
			                                               method);
		else
			ret_val = descriptor_replacement.getReturnType(callee.getVariableClass().getName(),
			                                               method);

		return ret_val;
	}

	public List<Type> getParameterTypes(VariableReference callee, Method method) {
		if (callee == null)
			return descriptor_replacement.getParameterTypes(method);
		else
			return descriptor_replacement.getParameterTypes(callee.getType(), method);
	}

	public List<Type> getParameterTypes(Constructor<?> constructor) {
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
		                                                       position, 1);
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

		StatementInterface st = new ConstructorStatement(test, constructor,
		        constructor.getDeclaringClass(), parameters);
		return test.addStatement(st, position);
	}

	public VariableReference addMethodFor(TestCase test, VariableReference callee,
	        Method method, int position) throws ConstructionFailedException {
		logger.debug("Adding method " + method.toGenericString());
		currentRecursion.clear();
		int length = test.size();
		List<VariableReference> parameters = null;
		parameters = satisfyParameters(test, callee, getParameterTypes(callee, method),
		                               position, 1);
		int new_length = test.size();
		position += (new_length - length);
		// VariableReference ret_val = new
		// VariableReference(method.getGenericReturnType(), position);

		Type ret_val = getReturnVariable(method, callee);
		StatementInterface st = new MethodStatement(test, method, callee, ret_val,
		        parameters);
		VariableReference ret = test.addStatement(st, position);
		ret.setDistance(callee.getDistance() + 1);
		logger.debug("Success: Adding method " + method);
		return ret;
	}

	public VariableReference addFieldFor(TestCase test, VariableReference callee,
	        Field field, int position) throws ConstructionFailedException {
		logger.debug("Adding field " + field.toGenericString() + " for variable "
		        + callee);
		currentRecursion.clear();

		FieldReference fieldVar = new FieldReference(test, field, callee);
		int length = test.size();
		VariableReference value = createOrReuseVariable(test, fieldVar.getType(),
		                                                position, 0, callee);
		int new_length = test.size();
		position += (new_length - length);

		StatementInterface st = new AssignmentStatement(test, fieldVar, value);
		VariableReference ret = test.addStatement(st, position);
		ret.setDistance(callee.getDistance() + 1);

		assert (test.isValid());

		return ret;
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
				        + method.getDeclaringClass().getName() + ": " + callee.getName());
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
		                               position, 1);

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
		Type ret_val = getReturnVariable(method, callee);
		StatementInterface st = new MethodStatement(test, method, callee, ret_val,
		        parameters);
		VariableReference ret = test.addStatement(st, position);

		logger.debug("Success: Adding method " + method);
		return ret;
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
		Set<AccessibleObject> all_calls;

		try {
			all_calls = testCluster.getGenerators(return_type);
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
			if (dependenciesSatisfied(dependencies, objects)) {
				calls.add(call);
			}
		}

		// TODO: What if primitive?

		return calls;
	}

	@Override
	public boolean changeRandomCall(TestCase test, StatementInterface statement) {
		logger.debug("Changing statement " + statement.getCode());
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

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

	// assumes "static java.util.Date aDate;" declared
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
	        IOException {
		ois.defaultReadObject();
		testCluster = TestCluster.getInstance();
		descriptor_replacement = MethodDescriptorReplacement.getInstance();
		currentRecursion = new HashSet<AccessibleObject>();
	}
}
