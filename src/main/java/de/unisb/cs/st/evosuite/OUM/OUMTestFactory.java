package de.unisb.cs.st.evosuite.OUM;

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
import de.unisb.cs.st.evosuite.testcase.AbstractTestFactory;
import de.unisb.cs.st.evosuite.testcase.ArrayStatement;
import de.unisb.cs.st.evosuite.testcase.AssignmentStatement;
import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.FieldStatement;
import de.unisb.cs.st.evosuite.testcase.GenericClass;
import de.unisb.cs.st.evosuite.testcase.MethodDescriptorReplacement;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.NullReference;
import de.unisb.cs.st.evosuite.testcase.PrimitiveStatement;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/**
 * Handle test case generation
 * 
 * @author Gordon Fraser
 * 
 */
public class OUMTestFactory extends AbstractTestFactory {

	private static OUMTestFactory instance = null;

	private static Logger logger = Logger.getLogger(OUMTestFactory.class);

	private final Randomness randomness = Randomness.getInstance();

	private final MethodDescriptorReplacement descriptor_replacement = MethodDescriptorReplacement.getInstance();

	/**
	 * Accessor to the test cluster
	 */
	private final TestCluster test_cluster = TestCluster.getInstance();

	/**
	 * Keep track of objects we are already trying to generate
	 */
	private final Set<Class<?>> current_recursion = new HashSet<Class<?>>();

	private UsageModel usage_model = null;

	private OUMTestFactory() {
		usage_model = UsageModel.getInstance();
	}

	public static OUMTestFactory getInstance() {
		if (instance == null)
			instance = new OUMTestFactory();
		return instance;
	}

	/**
	 * Determine the last method/constructor call performed on this object
	 * before position
	 * 
	 * @param test
	 * @param position
	 * @param variable
	 * @return
	 */
	public ConcreteCall getLastUse(TestCase test, int position, VariableReference variable) {
		for (int i = Math.min(position, test.size() - 1); i >= variable.getStPosition(); i--) {
			StatementInterface s = test.getStatement(i);
			if (s.references(variable)) {
				if (s instanceof MethodStatement) {
					MethodStatement ms = (MethodStatement) s;
					if (!ms.isStatic()) {
						if (ms.getCallee().equals(variable))
							return new ConcreteCall(variable.getClassName(),
							        ms.getMethod());
						/*
						} else {
						//logger.info("Checking static call of class "+ms.getMethod().getDeclaringClass()+" while looking for "+variable.getClassName());
						if(variable.isAssignableFrom(ms.getMethod().getDeclaringClass())) {
							return new ConcreteCall(variable.getClassName(), ms.getMethod());
						}
						*/
					}

				} else if (s instanceof ConstructorStatement) {
					if (s.getReturnValue().equals(variable)) {
						ConstructorStatement cs = (ConstructorStatement) s;
						return new ConcreteCall(variable.getClassName(),
						        cs.getConstructor());
					}
					/*
					  				} else if(s instanceof FieldStatement) {
					 
										FieldStatement fs = (FieldStatement)s;
										if(fs.getSource().equals(variable)) {
											return fs.getField();
										}
										*/
				}
			}
		}
		return null;
	}

	public ConcreteCall getNextUse(TestCase test, int position, VariableReference variable) {
		for (int i = position; i < test.size(); i++) {
			StatementInterface s = test.getStatement(i);
			if (s.references(variable)) {
				if (s instanceof MethodStatement) {
					MethodStatement ms = (MethodStatement) s;
					if (!ms.isStatic()) {
						if (ms.getCallee().equals(variable))
							return new ConcreteCall(variable.getClassName(),
							        ms.getMethod());
					} else {
						//logger.info("Checking static call of class "+ms.getMethod().getDeclaringClass()+" while looking for "+variable.getClassName());
						if (variable.isAssignableFrom(ms.getMethod().getDeclaringClass())) {
							return new ConcreteCall(variable.getClassName(),
							        ms.getMethod());
						}
					}

				} else if (s instanceof ConstructorStatement) {
					if (s.getReturnValue().equals(variable)) {
						ConstructorStatement cs = (ConstructorStatement) s;
						return new ConcreteCall(variable.getClassName(),
						        cs.getConstructor());
					}
					/*
					  				} else if(s instanceof FieldStatement) {
					 
										FieldStatement fs = (FieldStatement)s;
										if(fs.getSource().equals(variable)) {
											return fs.getField();
										}
										*/
				}
			}
		}
		return null;
	}

	public void appendRandomCall(TestCase test) {
		int position = test.size();
		logger.debug("Appending UUT call at position " + position);
		// add new instance of the UUT -> use on of the initial locations in the markov chart
		List<VariableReference> vars = test.getObjects(Properties.getTargetClass(),
		                                               position);
		if (vars.isEmpty()
		        || randomness.nextDouble() > Properties.OBJECT_REUSE_PROBABILITY) { // or certain probability - reuse probability?
			// Generate new
			logger.debug("Creating new UUT object");
			ConcreteCall generator = null;
			if (randomness.nextDouble() <= Properties.USAGE_RATE)
				generator = usage_model.getGenerator(Properties.TARGET_CLASS,
				                                     Properties.getTargetClass());
			else {
				AccessibleObject o;
				try {
					o = test_cluster.getRandomGenerator(Properties.getTargetClass());
					//					generator = new ConcreteCall(Properties.TARGET_CLASS, o);
					generator = getConcreteCall(o);

				} catch (ConstructionFailedException e) {
					return;
				}
			}

			logger.debug("Chosen generator: " + generator);
			try {
				if (generator.isMethod()) {
					addMethod(test, generator, position, 0);
				} else if (generator.isConstructor()) {
					addConstructor(test, generator, position, 0);
				} else if (generator.isField()) {
					addField(test, generator, position, 0);
				} else {
					logger.error("Encountered unknown type " + generator);
				}
			} catch (ConstructionFailedException e) {
				logger.info("Construction failed: " + generator);
			}
		} else {
			VariableReference target = randomness.choice(vars);
			if (randomness.nextDouble() < Properties.USAGE_RATE) {
				logger.debug("Chosen existing object: " + target);
				ConcreteCall last_call = getLastUse(test, position, target);
				logger.debug("Last call: " + last_call);
				logger.debug("Getting next call for target object of class: "
				        + target.getClassName());
				ConcreteCall next_call = getNextUse(test, position, target);
				logger.debug("Next call: " + next_call);
				ConcreteCall inserted_call = usage_model.getNextMethod(target.getClassName(),
				                                                       last_call);
				logger.debug("Inserted call: " + inserted_call);
				addCallFor(test, target, inserted_call, position);
			} else {
				List<AccessibleObject> calls = test_cluster.getCallsFor(target.getVariableClass());
				//List<Method> calls = getMethods(object.getVariableClass());
				if (!calls.isEmpty()) {
					AccessibleObject call = calls.get(randomness.nextInt(calls.size()));
					addCallFor(test, target,
					           new ConcreteCall(target.getClassName(), call), position);
				}
			}
		}
	}

	private ConcreteCall getConcreteCall(AccessibleObject o) {
		if (o instanceof Method) {
			return new ConcreteCall(((Method) o).getDeclaringClass().getName(), o);
		} else if (o instanceof Field) {
			return new ConcreteCall(((Field) o).getDeclaringClass().getName(), o);
		} else if (o instanceof Constructor<?>) {
			return new ConcreteCall(((Constructor<?>) o).getDeclaringClass().getName(), o);
		} else
			return null;
	}

	/**
	 * Insert a random statement at a random position in the test
	 * 
	 * @param test
	 */
	@Override
	public void insertRandomStatement(TestCase test) {
		int position = randomness.nextInt(test.size() + 1);
		final double P = 1d / 2d;

		double r = randomness.nextDouble();
		logger.debug("Inserting into test:");
		logger.debug(test.toCode());

		if (r <= P) {
			logger.debug("Inserting UUT call at position " + position);
			// add new instance of the UUT -> use on of the initial locations in the markov chart
			List<VariableReference> vars = test.getObjects(Properties.getTargetClass(),
			                                               position);
			if (vars.isEmpty()
			        || randomness.nextDouble() > Properties.OBJECT_REUSE_PROBABILITY) { // or certain probability - reuse probability?
				// Generate new
				logger.debug("Creating new UUT object");
				ConcreteCall generator = null;
				if (randomness.nextDouble() <= Properties.USAGE_RATE)
					generator = usage_model.getGenerator(Properties.TARGET_CLASS,
					                                     Properties.getTargetClass());
				else {
					AccessibleObject o;
					try {
						o = test_cluster.getRandomGenerator(Properties.getTargetClass());
						//						generator = new ConcreteCall(Properties.TARGET_CLASS, o);
						generator = getConcreteCall(o);
					} catch (ConstructionFailedException e) {
						return;
					}
				}

				logger.debug("Chosen generator: " + generator);
				try {
					if (generator == null) {
						logger.debug("Construction failed - found no generator");
					} else if (generator.isMethod()) {
						addMethod(test, generator, position, 0);
					} else if (generator.isConstructor()) {
						addConstructor(test, generator, position, 0);
					} else if (generator.isField()) {
						addField(test, generator, position, 0);
					} else {
						logger.error("Encountered unknown type " + generator);
					}
				} catch (ConstructionFailedException e) {
					logger.debug("Construction failed: " + generator);
				}
			} else {
				VariableReference target = randomness.choice(vars);
				if (randomness.nextDouble() < +Properties.USAGE_RATE) {
					logger.debug("Chosen existing object: " + target);
					ConcreteCall last_call = getLastUse(test, position, target);
					logger.debug("Last call: " + last_call);
					logger.debug("Getting next call for target object of class: "
					        + target.getClassName());
					ConcreteCall next_call = getNextUse(test, position, target);
					logger.debug("Next call: " + next_call);
					ConcreteCall inserted_call = usage_model.getNextMethod(target.getClassName(),
					                                                       last_call);
					logger.debug("Inserted call: " + inserted_call);
					addCallFor(test, target, inserted_call, position);
				} else {
					List<AccessibleObject> calls = test_cluster.getCallsFor(target.getVariableClass());
					//List<Method> calls = getMethods(object.getVariableClass());
					if (!calls.isEmpty()) {
						AccessibleObject call = calls.get(randomness.nextInt(calls.size()));
						addCallFor(test, target, new ConcreteCall(target.getClassName(),
						        call), position);
					}
				}
			}

		} else if (r <= 2 * P) {
			logger.debug("Inserting call on existing object");
			logger.debug(test.toCode());
			// Add call on existing object
			//VariableReference object = test.getRandomObject(position);
			List<VariableReference> objects = test.getObjects(position);
			VariableReference object = randomness.choice(objects);
			logger.debug("Chosen position: " + position + " -> " + object);
			while ((object == null || object.isPrimitive()) && !objects.isEmpty()) {
				logger.debug("Dropping primitive object from choice...");
				objects.remove(object);
				if (!objects.isEmpty())
					object = randomness.choice(objects);
				else
					object = null;
			}
			if (object != null && !object.isPrimitive()) {
				if (object.isArray() && object.getArrayLength() > 0) {
					logger.debug("Selected array object");
					int index = randomness.nextInt(object.getArrayLength());
					try {
						assignArray(test, object, index, position);
					} catch (ConstructionFailedException e) {
					}
				} else {

					if (randomness.nextDouble() <= Properties.USAGE_RATE) {

						logger.debug("Selected " + object);
						if (!usage_model.hasClass(object.getClassName())) {
							logger.debug("Have no usage information about "
							        + object.getClassName());
							return;
						}
						ConcreteCall last_call = getLastUse(test, position, object);
						logger.debug("Last call: " + last_call);
						/*
						if(last_call == null) {

						logger.warn("Impossible - no previous call for object at position "+position+" found: "+object.getName() +" in "+test.toCode());
						return;
						}
						 */
						logger.debug("Getting next call for object of type "
						        + object.getClassName() + ", last call: " + last_call);
						ConcreteCall next_call = usage_model.getNextMethod(object.getClassName(),
						                                                   last_call);
						logger.debug("Next call: " + next_call);

						if (next_call != null) {
							addCallFor(test, object, next_call, position);
						}
					} else {
						List<AccessibleObject> calls = test_cluster.getCallsFor(object.getVariableClass());
						//List<Method> calls = getMethods(object.getVariableClass());
						if (!calls.isEmpty()) {
							AccessibleObject call = calls.get(randomness.nextInt(calls.size()));
							addCallFor(test, object,
							           new ConcreteCall(object.getClassName(), call),
							           position);
						}
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
		logger.trace("Delete Statement - " + position);

		List<VariableReference> references = test.getReferences(test.getReturnValue(position));
		List<Integer> positions = new ArrayList<Integer>();
		Set<Integer> p = new HashSet<Integer>();
		for (VariableReference var : references) {
			p.add(var.getStPosition());
			//positions.add(var.statement);
		}
		positions.addAll(p);
		Collections.sort(positions, Collections.reverseOrder());
		for (Integer pos : positions) {
			logger.trace("Deleting statement: " + pos);
			test.remove(pos);
		}

		//		logger.trace("DeleteStatement mutation: Var is not referenced, deleting");
		logger.trace("Deleting statement: " + position);
		test.remove(position);
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
			addConstructor(test, new ConcreteCall(
			        statement.getReturnValue().getClassName(),
			        ((ConstructorStatement) statement).getConstructor()), test.size(), 0);
		} else if (statement instanceof MethodStatement) {
			addMethod(test,
			          new ConcreteCall(
			                  ((MethodStatement) statement).getMethod().getDeclaringClass().getName(),
			                  ((MethodStatement) statement).getMethod()), test.size(), 0);
		} else if (statement instanceof PrimitiveStatement<?>) {
			addPrimitive(test, (PrimitiveStatement<?>) statement, test.size());
			//				test.statements.add((PrimitiveStatement) statement);
		} else if (statement instanceof FieldStatement) {
			addField(test,
			         new ConcreteCall(
			                 ((FieldStatement) statement).getField().getDeclaringClass().getName(),
			                 ((FieldStatement) statement).getField()), test.size(), 0);
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
			// Change all references to return value at position to something else
			for (int i = position; i < test.size(); i++) {
				StatementInterface s = test.getStatement(i);
				if (s.references(var)) {
					if (s instanceof MethodStatement) {
						MethodStatement ms = (MethodStatement) s;
						if (ms.getCallee() != null && ms.getCallee().equals(var)) {
							VariableReference r = randomness.choice(alternatives);
							ms.setCallee(r);
							logger.trace("Replacing callee in method call");
						}
						for (int pos = 0; pos < ms.parameters.size(); pos++) {
							if (ms.parameters.get(pos).equals(var)) {
								VariableReference r = randomness.choice(alternatives);
								ms.parameters.set(pos, r);
								logger.trace("Replacing parameter in method call");
							}
						}
					} else if (s instanceof ConstructorStatement) {
						ConstructorStatement cs = (ConstructorStatement) s;
						for (int pos = 0; pos < cs.parameters.size(); pos++) {
							if (cs.parameters.get(pos).equals(var)) {
								VariableReference r = randomness.choice(alternatives);
								cs.parameters.set(pos, r);
								logger.trace("Replacing parameter in constructor call");
							}
						}
					} else if (s instanceof FieldStatement) {
						FieldStatement fs = (FieldStatement) s;
						if (fs.getSource() != null && fs.getSource().equals(var)) {
							VariableReference r = randomness.choice(alternatives);
							fs.setSource(r);
							logger.trace("Replacing field source");
						}
					} else if (s instanceof AssignmentStatement) {
						AssignmentStatement as = (AssignmentStatement) s;
						if (as.getReturnValue() != null
						        && as.getReturnValue().equals(var)) { // TODO: array index might exceed length
							VariableReference r = randomness.choice(alternatives);
							as.setArray(r);
							logger.trace("Replacing array source");
						}
						if (as.parameter != null && as.parameter.equals(var)) {
							VariableReference r = randomness.choice(alternatives);
							as.parameter = r;
							logger.trace("Replacing array parameter");
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
	public VariableReference addConstructor(TestCase test, ConcreteCall constructor,
	        int position, int recursion_depth) throws ConstructionFailedException {
		if (recursion_depth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException();
		}
		logger.debug("Adding constructor " + constructor);

		int length = test.size();
		List<VariableReference> parameters = satisfyParameters(test,
		                                                       null,
		                                                       constructor,
		                                                       getParameterTypes(constructor.getConstructor()),
		                                                       position, recursion_depth);
		int new_length = test.size();
		position += (new_length - length);
		StatementInterface st = new ConstructorStatement(test, constructor.getConstructor(), constructor.getCallClass(),
		        parameters);
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
	public VariableReference addMethod(TestCase test, ConcreteCall method, int position,
	        int recursion_depth) throws ConstructionFailedException {
		//System.out.println("TG: Looking for callee of type "+method.getDeclaringClass());
		if (recursion_depth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException();
		}
		logger.debug("Adding method " + method);
		int length = test.size();
		VariableReference callee = null;
		List<VariableReference> parameters = null;
		String className = method.getClassName();
		if (callee != null)
			className = callee.getClassName();

		if (!Modifier.isStatic(method.getMethod().getModifiers())) { // TODO: Consider reuse probability here?
			try {
				// TODO: Would casting be an option here?
				callee = test.getRandomObject(method.getCallClass(), position);
				logger.debug("Found callee of type " + callee.getClassName());
			} catch (ConstructionFailedException e) {
				logger.debug("No callee of type " + method.getClassName() + " found");

				// Check if we have usage information on how the callee is created: Check for information on parameter 0!
				if (!usage_model.hasGenerators(className, method, 0)
				        || randomness.nextDouble() > Properties.USAGE_RATE) {
					logger.debug("Generating callee without usage information");
					callee = attemptGeneration(test, method.getCallClass(), position,
					                           recursion_depth, true);
					if (callee instanceof NullReference)
						throw new ConstructionFailedException(); // Can't use null as callee, that's just silly
				} else {
					logger.debug("Generating callee with usage information");
					callee = attemptGenerationUsage(test, method, 0, position,
					                                recursion_depth, true);
					if (!callee.isAssignableTo(method.getCallClass())) {
						logger.debug("Got to look for return value");
						callee = test.getRandomObject(method.getCallClass(), position);
					}
				}
				//				callee = attemptGeneration(test, method.getDeclaringClass(), position, recursion_depth, false);
				position += test.size() - length;
				length = test.size();
			}
			parameters = satisfyParameters(test, callee, method,
			                               getParameterTypes(callee, method.getMethod()),
			                               position, recursion_depth);
		} else {
			parameters = satisfyParameters(test, callee, method,
			                               getParameterTypes(callee, method.getMethod()),
			                               position, recursion_depth);
		}

		int new_length = test.size();
		position += (new_length - length);

		//VariableReference ret_val = new VariableReference(method.getGenericReturnType(), position);
		Type ret_val_type = getReturnVariable(method.getMethod(), callee);

		StatementInterface st = new MethodStatement(test, method.getMethod(), callee, ret_val_type,
		        parameters);
		VariableReference ret = test.addStatement(st, position);
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
	@SuppressWarnings("unchecked")
	private VariableReference addPrimitive(TestCase test, PrimitiveStatement<?> old,
	        int position) throws ConstructionFailedException {
		logger.debug("Adding primitive");
		StatementInterface st = new PrimitiveStatement(test, old.getReturnType(), old.getValue());
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
	private VariableReference addField(TestCase test, ConcreteCall field, int position,
	        int recursion_depth) throws ConstructionFailedException {
		logger.debug("Adding field " + field);
		if (recursion_depth > Properties.MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException();
		}

		VariableReference callee = null;
		int length = test.size();

		if (!Modifier.isStatic(field.getField().getModifiers())) {
			try {
				callee = test.getRandomObject(field.getCallClass(), position);
			} catch (ConstructionFailedException e) {
				logger.debug("No callee of type " + field.getClassName() + " found");
				// TODO: choose between factory! 
				callee = attemptGeneration(test, field.getCallClass(), position,
				                           recursion_depth, false);
				position += test.size() - length;
				length = test.size();
			}
		}
	
		StatementInterface st = new FieldStatement(test, field.getField(), callee, field.getField().getGenericType());
		return test.addStatement(st, position);
	}

	private ConcreteCall getCall(TestCase test, VariableReference var) {
		StatementInterface s = test.getStatement(var.getStPosition());
		if (s instanceof MethodStatement) {
			return new ConcreteCall(var.getClassName(), ((MethodStatement) s).getMethod());
		} else if (s instanceof ConstructorStatement) {
			return new ConcreteCall(var.getClassName(),
			        ((ConstructorStatement) s).getConstructor());
		} else if (s instanceof FieldStatement) {
			return new ConcreteCall(var.getClassName(), ((FieldStatement) s).getField());
		}
		return null;
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
	        VariableReference callee, ConcreteCall call, List<Type> parameter_types,
	        int position, int recursion_depth) throws ConstructionFailedException {
		List<VariableReference> parameters = new ArrayList<VariableReference>();
		logger.debug("Trying to satisfy " + parameter_types.size()
		        + " parameters for call " + call);
		int i = 0;
		for (Type parameter_type : parameter_types) {
			double reuse = randomness.nextDouble();
			int previous_length = test.size();

			// Collect all objects that satisfy the usage constraints
			List<VariableReference> objects = new ArrayList<VariableReference>();
			if (randomness.nextDouble() <= Properties.USAGE_RATE
			        && usage_model.hasGenerators(call.getClassName(), call, i + 1)) {
				logger.debug("Using information for parameter selection...");
				for (VariableReference var : test.getObjects(parameter_type, position)) {
					ConcreteCall generator = getCall(test, var);
					if (usage_model.hasGenerator(call.getClassName(), call, i + 1,
					                             generator))
						objects.add(var);
				}
			} else {
				objects = test.getObjects(parameter_type, position);
			}

			if (callee != null)
				objects.remove(callee);
			logger.debug("Found suitable objects: " + objects.size());

			if (!objects.isEmpty() && reuse <= Properties.OBJECT_REUSE_PROBABILITY) {
				logger.debug(" Parameter " + i + ": Reusing existing object of type "
				        + parameter_type);
				VariableReference reference = randomness.choice(objects);
				parameters.add(reference);
			} else {
				logger.debug(" Parameter " + i + ": Creating new object of type "
				        + parameter_type);
				VariableReference reference;
				GenericClass clazz = new GenericClass(parameter_type);
				if (clazz.isPrimitive() || clazz.isString() || clazz.isArray()
				        || !usage_model.hasGenerators(call.getClassName(), call, i + 1)) {
					logger.debug("Have no usage information for this parameter");
					reference = attemptGeneration(test, parameter_type, position,
					                              recursion_depth + 1, true);
				} else {
					logger.debug("Have usage information for this parameter");
					if (randomness.nextDouble() <= Properties.USAGE_RATE)
						reference = attemptGenerationUsage(test, call, i + 1, position,
						                                   recursion_depth + 1, true);
					else
						reference = attemptGeneration(test, parameter_type, position,
						                              recursion_depth + 1, true);
				}
				parameters.add(reference);
			}
			int current_length = test.size();
			position += current_length - previous_length;
			i++;
		}
		logger.debug("Satisfied " + parameter_types.size() + " parameters");
		return parameters;
	}

	protected void assignArray(TestCase test, VariableReference array, int array_index,
	        int position) throws ConstructionFailedException {
		List<VariableReference> objects = test.getObjects(array.getComponentType(),
		                                                  position);
		Iterator<VariableReference> iterator = objects.iterator();
		while (iterator.hasNext()) {
			VariableReference var = iterator.next();
			if (var.isArrayIndex() && var.getArray().equals(array))
				iterator.remove();
		}
		assignArray(test, array, array_index, position, objects);
	}

	protected void assignArray(TestCase test, VariableReference array, int array_index,
	        int position, List<VariableReference> objects)
	        throws ConstructionFailedException {
		//		VariableReference index = array.getVariable(array_index).clone();

		//index.statement = position;
		if (!objects.isEmpty()
		        && randomness.nextDouble() <= Properties.OBJECT_REUSE_PROBABILITY) {
			// Assign an existing value
			// TODO:
			// Do we need a special "[Array]AssignmentStatement"?
			test.addStatement(new AssignmentStatement(test, array, array_index, array.getArrayLength(),
			        randomness.choice(objects)), position);

		} else {
			// Assign a new value
			// Need a primitive, method, constructor, or field statement where retval is set to index
			// Need a version of attemptGeneration that takes retval as parameter

			// OR: Create a new variablereference and then assign it to array (better!)
			int old_len = test.size();
			VariableReference var = attemptGeneration(test, array.getComponentType(),
			                                          position);
			position += test.size() - old_len;
			test.addStatement(new AssignmentStatement(test, array, array_index, array.getArrayLength(), var), position);
		}
	}

	private VariableReference createArray(TestCase test, Type type, int position,
	        int recursion_depth) throws ConstructionFailedException {
		// Create array with random size
		//VariableReference reference = new VariableReference(type, position); // TODO: Is this correct? -1;?
		ArrayStatement statement = new ArrayStatement(test, type, position);
		VariableReference reference = test.addStatement(statement, position);
		position++;
		//logger.info(test.toCode());
		// For each value of array, call attemptGeneration
		List<VariableReference> objects = test.getObjects(reference.getComponentType(),
		                                                  position);
		for (int i = 0; i < statement.size(); i++) {
			int old_len = test.size();
			assignArray(test, statement.getReturnValue(), i, position, objects);
			position += test.size() - old_len;
		}
		/*
		VariableReference index = statement.getVariable(i);
		index.statement = position;
		if(!objects.isEmpty() && randomness.nextDouble() <= object_reuse_probability) {
			// Assign an existing value
			// TODO:
			// Do we need a special "[Array]AssignmentStatement"?
			test.addStatement(new AssignmentStatement(index, randomness.choice(objects)));

		} else {
			// Assign a new value
			// Need a primitive, method, constructor, or field statement where retval is set to index
			// Need a version of attemptGeneration that takes retval as parameter
			
			// OR: Create a new variablereference and then assign it to array (better!)
			int old_len = test.size();
			VariableReference var = attemptGeneration(test, reference.getComponentType(), position);
			position += test.size() - old_len;
			test.addStatement(new AssignmentStatement(index, var));				
		}
		position++;
		*/
		//test.addStatement(statement);
		//}
		//logger.info(test.toCode());
		return reference;

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
	//	private VariableReference attemptGeneration(TestCase test, Type type, int position, int recursion_depth, boolean allow_null) throws ConstructionFailedException {
	private VariableReference attemptGeneration(TestCase test, Type type, int position,
	        int recursion_depth, boolean allow_null) throws ConstructionFailedException {
		GenericClass clazz = new GenericClass(type);

		if (clazz.isPrimitive() || clazz.isString()) {
			logger.debug("Generating primitive of type " + ((Class<?>) type).getName());
			//VariableReference reference = new VariableReference(type, position); // TODO: Is this correct? -1;?
			// TODO: Check before cast!
			StatementInterface st = PrimitiveStatement.getRandomStatement(test, type, position, type);
			return test.addStatement(st, position);
		} else if (clazz.isArray()) {
			return createArray(test, type, position, recursion_depth + 1);

		} else {
			if (allow_null && randomness.nextDouble() <= Properties.NULL_PROBABILITY) {
				logger.debug("Using Null!");
				return new NullReference(test, type);
			}

			//logger.info("Current recursion list: "+current_recursion.size());
			//			AccessibleObject o = mapping.getRandomGenerator(type, (ClassConstraint)constraint);
			//AccessibleObject o = test_cluster.getRandomGenerator(type); // !Problem!!!
			ConcreteCall o = null;
			if (randomness.nextDouble() <= Properties.USAGE_RATE)
				o = usage_model.getGenerator(clazz.getClassName(), clazz.getRawClass());
			else {
				//				o = new ConcreteCall(clazz.getClassName(), test_cluster.getRandomGenerator(type));
				o = getConcreteCall(test_cluster.getRandomGenerator(type));
			}
			if (o == null) {
				logger.debug("No generators found for type " + type);
				throw new ConstructionFailedException();
			} else if (o.isField()) {
				logger.debug("Attempting generating of " + type + " via field of type "
				        + type);
				VariableReference ret = addField(test, o, position, recursion_depth + 1);
				logger.debug("Success in generating type " + type);
				return ret;
			} else if (o.isMethod()) {
				logger.debug("Attempting generating of " + type + " via method " + o
				        + " of type " + type);
				VariableReference ret = addMethod(test, o, position, recursion_depth + 1);
				logger.debug("Success in generating type " + type);
				return ret;
			} else if (o.isConstructor()) {
				logger.debug("Attempting generating of " + type + " via constructor " + o
				        + " of type " + type);
				VariableReference ret = addConstructor(test, o, position,
				                                       recursion_depth + 1);
				logger.debug("Success in generating type " + type);
				return ret;
			} else {
				logger.debug("No generators found for type " + type);
				throw new ConstructionFailedException();
			}
		}
		// TODO: Sometimes we could use null 
	}

	private VariableReference attemptGenerationUsage(TestCase test, ConcreteCall call,
	        int parameter, int position, int recursion_depth, boolean allow_null)
	        throws ConstructionFailedException {
		ConcreteCall o = usage_model.getGenerator(call.getClassName(), call, parameter);
		if (o == null) {
			throw new ConstructionFailedException();
		} else if (o.isField()) {
			logger.debug("Attempting generating of via field " + o);
			VariableReference ret = addField(test, o, position, recursion_depth + 1);
			logger.debug("Success in generating type ");
			return ret;
		} else if (o.isMethod()) {
			logger.debug("Attempting generating via method " + o.getName());
			VariableReference ret = addMethod(test, o, position, recursion_depth + 1);
			logger.debug("Success in generating");
			logger.debug(test.toCode());
			return ret;
		} else if (o.isConstructor()) {
			logger.debug("Attempting generating via constructor " + o.getName());
			VariableReference ret = addConstructor(test, o, position, recursion_depth + 1);
			logger.debug("Success in generating type ");
			return ret;
		} else {
			logger.debug("No generators found for type ");
			throw new ConstructionFailedException();
		}

	}

	/**
	 * Append given call to the test case at given position
	 * 
	 * @param test
	 * @param call
	 * @param position
	 */
	private void addCallFor(TestCase test, VariableReference callee, ConcreteCall call,
	        int position) {
		int previous_length = test.size();
		current_recursion.clear();
		try {
			if (call != null && call.isMethod()) {
				addMethodFor(test, callee, call, position);
			}
		} catch (ConstructionFailedException e) {
			// TODO: Check this!
			logger.debug("Inserting call " + call + " has failed. Removing statements");
			//System.out.println("TG: Failed");			
			// TODO: Doesn't work if position != test.size()
			while (test.size() != previous_length) {
				logger.debug("  Removing statement: "
				        + test.getStatement(position).getCode());
				test.remove(position);
			}
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
		return;
		/*
		int position = statement.getReturnValue().statement;
		
		if(call instanceof Method) {
			Method method = (Method)call;
			VariableReference retval = statement.getReturnValue();
			VariableReference callee = null;
			if(!Modifier.isStatic(method.getModifiers()))
				callee = test.getRandomObject(method.getDeclaringClass(), position);
			List<VariableReference> parameters = new ArrayList<VariableReference>();
			for(Type type : getParameterTypes(callee, method)) {
				parameters.add(test.getRandomObject(type, position));
			}
			MethodStatement m = new MethodStatement(method, callee, retval, parameters);
			test.setStatement(m, position);
			
		} else if(call instanceof Constructor<?>) {

			Constructor<?> constructor = (Constructor<?>)call;
			VariableReference retval = statement.getReturnValue();
			List<VariableReference> parameters = new ArrayList<VariableReference>();
			for(Type type : getParameterTypes(constructor)) {
				parameters.add(test.getRandomObject(type, position));
			}
			ConstructorStatement c = new ConstructorStatement(constructor, retval, parameters);
			test.setStatement(c, position);

		} else if(call instanceof Field) {
			Field field = (Field)call;
			VariableReference retval = statement.getReturnValue();
			VariableReference source = null;
			if(!Modifier.isStatic(field.getModifiers()))
				source = test.getRandomObject(field.getDeclaringClass(), position);

			FieldStatement f = new FieldStatement(field, source, retval);
			test.setStatement(f, position);
			
		}
		*/
	}

	private Type getReturnVariable(Method method, VariableReference callee) {

		Type ret_val = method.getGenericReturnType();

		// Casting
		// Case 1: clone() on cloneable -> cast to type of defining class
		if (method.getReturnType().equals(Object.class)
		        && method.getName().equals("clone")) {
			ret_val=method.getDeclaringClass();
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
	        VariableReference parameter, ConcreteCall constructor, int position)
	        throws ConstructionFailedException {

		logger.debug("Adding constructor " + constructor);

		int length = test.size();
		List<VariableReference> parameters = satisfyParameters(test,
		                                                       null,
		                                                       constructor,
		                                                       getParameterTypes(constructor.getConstructor()),
		                                                       position, 0);
		// Force one parameter to be "parameter"
		if (!parameters.contains(parameter)) {
			int num = 0;
			for (Type type : constructor.getConstructor().getGenericParameterTypes()) {
				if (parameter.isAssignableTo(type)) {
					parameters.set(num, parameter);
					break;
				}
				num++;
			}
		}
		int new_length = test.size();
		position += (new_length - length);
		StatementInterface st = new ConstructorStatement(test, constructor.getConstructor(),  constructor.getCallClass(), parameters);
		return test.addStatement(st, position);
	}

	public VariableReference addMethodFor(TestCase test, VariableReference callee,
	        ConcreteCall method, int position) throws ConstructionFailedException {
		logger.debug("Adding method " + method);
		int length = test.size();
		List<VariableReference> parameters = null;
		parameters = satisfyParameters(test, callee, method,
		                               getParameterTypes(callee, method.getMethod()),
		                               position, 0);
		int new_length = test.size();
		position += (new_length - length);
		Type ret_val = getReturnVariable(method.getMethod(), callee);
		StatementInterface st = new MethodStatement(test, method.getMethod(), callee, ret_val,
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
		List<VariableReference> objects = test.getObjects(statement.getReturnValue().getStPosition());
		objects.remove(statement.getReturnValue());
		List<AccessibleObject> calls = getPossibleCalls(statement.getReturnType(),
		                                                objects);
		logger.debug("Got " + calls.size() + " possible calls for " + objects.size()
		        + " objects");
		AccessibleObject call = randomness.choice(calls);
		try {
			changeCall(test, statement, call);
			return true;
		} catch (ConstructionFailedException e) {
			// Ignore
			logger.info("Change failed");
		}
		return false;

	}

}
