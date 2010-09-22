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
import de.unisb.cs.st.evosuite.testcase.Statement;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCluster;
import de.unisb.cs.st.evosuite.testcase.VariableReference;
import de.unisb.cs.st.ga.ConstructionFailedException;
import de.unisb.cs.st.ga.Randomness;

/**
 * Handle test case generation
 * @author Gordon Fraser
 *
 */
public class OUMTestFactory extends AbstractTestFactory {

	private static OUMTestFactory instance = null;
	
	private static Logger logger = Logger.getLogger(OUMTestFactory.class);
	
	private final static int MAX_RECURSION = Properties.getPropertyOrDefault("max_recursion", 10);

	//private int max_attempts  = Integer.parseInt(System.getProperty("GA.max_attempts"));
	private double object_reuse_probability = Properties.getPropertyOrDefault("object_reuse_probability", 0.9);

	private double null_probability = Properties.getPropertyOrDefault("null_probability", 0.1);

	private Randomness randomness = Randomness.getInstance();

	private MethodDescriptorReplacement descriptor_replacement = MethodDescriptorReplacement.getInstance();
	
	/**
	 * Accessor to the test cluster
	 */
	private TestCluster test_cluster = TestCluster.getInstance();

	/**
	 * Keep track of objects we are already trying to generate
	 */
	private Set<Class<?>> current_recursion = new HashSet<Class<?>>();

	private UsageModel usage_model = UsageModel.getInstance();
	
	private OUMTestFactory() { }
	
	public static OUMTestFactory getInstance() {
		if(instance == null)
			instance = new OUMTestFactory();
		return instance;
	}
	
	/**
	 * Determine the last method/constructor call performed on this object before position
	 * @param test
	 * @param position
	 * @param variable
	 * @return
	 */
	private AccessibleObject getLastUse(TestCase test, int position, VariableReference variable) {
		for(int i = Math.min(position, test.size() - 1); i>=variable.statement; i--) {
			Statement s = test.getStatement(i);
			if(s.references(variable)) {
				if(s instanceof MethodStatement) {
					MethodStatement ms = (MethodStatement)s;
					if(!ms.isStatic()) {
						if(ms.getCallee().equals(variable))
							return ms.getMethod();
					} else {
						//logger.info("Checking static call of class "+ms.getMethod().getDeclaringClass()+" while looking for "+variable.getClassName());
						if(variable.isAssignableFrom(ms.getMethod().getDeclaringClass())) {
							return ms.getMethod();
						}
					}
					
				} else if(s instanceof ConstructorStatement) {
					if(s.getReturnValue().equals(variable)) {
						ConstructorStatement cs = (ConstructorStatement)s;
						return cs.getConstructor();
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
		
	/**
	 * Insert a random statement at a random position in the test
	 * @param test
	 */
	public void insertRandomStatement(TestCase test) {
		int position = randomness.nextInt(test.size() + 1);
		final double P = 1d/2d;
		
		double r = randomness.nextDouble();
		logger.debug("Inserting into test:");
		logger.debug(test.toCode());
		
		if(r <= P) {
			logger.debug("Inserting UUT call at position "+position);
			// add new instance of the UUT -> use on of the initial locations in the markov chart
			List<VariableReference> vars = test.getObjects(Properties.getTargetClass(), position);
			if(vars.isEmpty() || randomness.nextDouble() > object_reuse_probability) { // or certain probability - reuse probability?
				// Generate new
				logger.debug("Creating new UUT object");
				AccessibleObject generator = usage_model.getGenerator(Properties.TARGET_CLASS);
				logger.debug("Chosen generator: "+generator);
				try {
					if(generator instanceof Method) {
						addMethod(test, (Method)generator, position, 0);
					} else if(generator instanceof Constructor<?>) {
						addConstructor(test, (Constructor<?>)generator, position, 0);
					} else if(generator instanceof Field) {
						addField(test, (Field)generator, position);
					} else {
						logger.error("Encountered unknown type "+generator);
					}
				} catch (ConstructionFailedException e) {
					logger.info("Construction failed: "+generator);
				}
			} else {
				VariableReference target = randomness.choice(vars);
				logger.debug("Chosen existing object: "+target);
				AccessibleObject last_call = getLastUse(test, position, target);
				logger.debug("Last call: "+last_call);
				logger.info("Getting next call for target object of class: "+target.getClassName());
				AccessibleObject next_call = usage_model.getNextMethod(target.getClassName(), last_call);
				logger.debug("Next call: "+next_call);
				addCallFor(test, target, next_call, position);
			}

		} 
		else if( r <= 2*P)
		{
			logger.debug("Inserting call on existing object");
			logger.debug(test.toCode());
			// Add call on existing object
			//VariableReference object = test.getRandomObject(position);
			List<VariableReference> objects = test.getObjects(position);
			VariableReference object = randomness.choice(objects);
			logger.info("Chosen position: "+position+" -> "+object);
			while((object == null || object.isPrimitive()) && !objects.isEmpty()) {
				logger.debug("Dropping primitive object from choice...");
				objects.remove(object);
				if(!objects.isEmpty())
					object = randomness.choice(objects);
				else
					object = null;
			}
			if(object != null && !object.isPrimitive()) {
				if(object.isArray() && object.array_length > 0) {
					logger.debug("Selected array object");
					int index = randomness.nextInt(object.array_length);
					try {
						assignArray(test, object, index, position);
					} catch (ConstructionFailedException e) {
					}
				} else {
					logger.debug("Selected "+object);
					if(!usage_model.hasClass(object.getClassName())) {
						logger.info("Have no usage information about "+object.getClassName());
						return;
					}
					AccessibleObject last_call = getLastUse(test, position, object);
					logger.debug("Last call: "+last_call);
					/*
					  if(last_call == null) {
					 
						logger.warn("Impossible - no previous call for object at position "+position+" found: "+object.getName() +" in "+test.toCode());
						return;
					}
				*/
					logger.info("Getting next call for object of type "+object.getClassName()+", last call: "+last_call);
					AccessibleObject next_call = usage_model.getNextMethod(object.getClassName(), last_call);
					logger.debug("Next call: "+next_call);
					
					if(next_call != null) {
						addCallFor(test, object, next_call, position);
					}
				}
			}	
		}
	}
	
	/**
	 * Delete the statement at position from the test case and remove all references to it
	 * @param test
	 * @param position
	 * @throws ConstructionFailedException
	 */
	public void deleteStatement(TestCase test, int position) throws ConstructionFailedException {
		logger.trace("Delete Statement - "+position);

		List<VariableReference> references = test.getReferences(test.getReturnValue(position));
		List<Integer> positions = new ArrayList<Integer>();
		Set<Integer> p = new HashSet<Integer>();
		for(VariableReference var : references) {
			p.add(var.statement);
			//positions.add(var.statement);
		}
		positions.addAll(p);		
		Collections.sort(positions, Collections.reverseOrder());
		for(Integer pos : positions) {
			logger.trace("Deleting statement: "+pos);
			test.remove(pos);
		} 
		
//		logger.trace("DeleteStatement mutation: Var is not referenced, deleting");
		logger.trace("Deleting statement: "+position);
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
	public void appendStatement(TestCase test, Statement statement) throws ConstructionFailedException {
		current_recursion.clear();

		if(statement instanceof ConstructorStatement) {
			addConstructor(test, ((ConstructorStatement) statement).getConstructor(), test.size(), 0);
		}
		else if(statement instanceof MethodStatement) {
			addMethod(test, ((MethodStatement) statement).getMethod(), test.size(), 0);
		}
		else if(statement instanceof PrimitiveStatement<?>) {
			addPrimitive(test, (PrimitiveStatement<?>)statement, test.size());
			//				test.statements.add((PrimitiveStatement) statement);
		}
		else if(statement instanceof FieldStatement) {
			addField(test, ((FieldStatement) statement).getField(), test.size());
		}
	}
	
	
	public void deleteStatementGracefully(TestCase test, int position) throws ConstructionFailedException {
		logger.trace("Delete Statement - "+position);

		VariableReference var = test.getReturnValue(position);
		if(var.isArrayIndex()) {
			deleteStatement(test, position);
			return;
		}
		List<VariableReference> alternatives = test.getObjects(var.getType(), position);
		alternatives.remove(var);
		
		if(!alternatives.isEmpty()) {
			// Change all references to return value at position to something else
			for(int i = position; i< test.size(); i++) {
				Statement s = test.getStatement(i);
				if(s.references(var)) {
					if(s instanceof MethodStatement) {
						MethodStatement ms = (MethodStatement)s;
						if(ms.getCallee() != null && ms.getCallee().equals(var)) {
							VariableReference r = randomness.choice(alternatives);
							ms.setCallee(r.clone());
							logger.trace("Replacing callee in method call");
						}
						for(int pos = 0; pos<ms.parameters.size(); pos++) {
							if(ms.parameters.get(pos).equals(var)) {
								VariableReference r = randomness.choice(alternatives);
								ms.parameters.set(pos, r.clone());
								logger.trace("Replacing parameter in method call");
							}	
						}
					} else if(s instanceof ConstructorStatement) {
						ConstructorStatement cs = (ConstructorStatement)s;
						for(int pos = 0; pos<cs.parameters.size(); pos++) {
							if(cs.parameters.get(pos).equals(var)) {
								VariableReference r = randomness.choice(alternatives);
								cs.parameters.set(pos, r.clone());
								logger.trace("Replacing parameter in constructor call");
							}	
						}
					} else if(s instanceof FieldStatement) {
						FieldStatement fs = (FieldStatement)s;
						if(fs.getSource() != null && fs.getSource().equals(var)) {
							VariableReference r = randomness.choice(alternatives);
							fs.setSource(r.clone());
							logger.trace("Replacing field source");
						}
					} else if(s instanceof AssignmentStatement) {
						AssignmentStatement as = (AssignmentStatement)s;
						if(as.getReturnValue() != null && as.getReturnValue().equals(var))
						{ // TODO: array index might exceed length
							VariableReference r = randomness.choice(alternatives);
							as.setArray(r.clone());
							logger.trace("Replacing array source");
						}
						if(as.parameter != null && as.parameter.equals(var))
						{
							VariableReference r = randomness.choice(alternatives);
							as.parameter = r.clone();
							logger.trace("Replacing array parameter");
						}
					}
				}
			}
		}
			
		// Remove everything else
		deleteStatement(test, position);
	}
	
	public VariableReference addCall(TestCase test, AccessibleObject call, int position, int recursion_depth) throws ConstructionFailedException {
		if( call == null) {
			throw new ConstructionFailedException();
		} else if(call instanceof Field) {
			VariableReference ret = addField(test, (Field)call, position);
			return ret.clone();
		} else if(call instanceof Method) {
			VariableReference ret = addMethod(test, (Method)call, position, recursion_depth + 1);
			return ret.clone();
		} else if(call instanceof Constructor<?>) {
			VariableReference ret = addConstructor(test, (Constructor<?>)call, position, recursion_depth + 1);
			return ret.clone();
		} else {
			throw new ConstructionFailedException();
		}
	}
	
	/**
	 * Add constructor at given position if max recursion depth has not been reached
	 * @param constructor
	 * @param position
	 * @param recursion_depth
	 * @return
	 * @throws ConstructionFailedException
	 */
	public VariableReference addConstructor(TestCase test, Constructor<?> constructor, int position, int recursion_depth) throws ConstructionFailedException {
		if(recursion_depth > MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException();
		}
		logger.debug("Adding constructor "+constructor.toGenericString());

		int length = test.size();
		List<VariableReference> parameters = satisfyParameters(test, null, constructor.getDeclaringClass().getName(), constructor, getParameterTypes(constructor), position, recursion_depth);
		int new_length = test.size();
		position += (new_length - length);
		VariableReference ret_val = new VariableReference(constructor.getDeclaringClass(), position);
		test.addStatement(new ConstructorStatement(constructor, ret_val, parameters), position);
		
		return ret_val;
	}
	
	/**
	 * Add method at given position if max recursion depth has not been reached
	 * @param test
	 * @param method
	 * @param position
	 * @param recursion_depth
	 * @return
	 * @throws ConstructionFailedException
	 */
	public VariableReference addMethod(TestCase test, Method method, int position, int recursion_depth) throws ConstructionFailedException {
		//System.out.println("TG: Looking for callee of type "+method.getDeclaringClass());
		if(recursion_depth > MAX_RECURSION) {
			logger.debug("Max recursion depth reached");
			throw new ConstructionFailedException();
		}
		logger.debug("Adding method "+method.toGenericString());
		int length = test.size();
		VariableReference callee = null;
		List<VariableReference> parameters = null;
		String className = method.getDeclaringClass().getName();
		if(callee != null)
			className = callee.getClassName();
		
		if(! Modifier.isStatic(method.getModifiers())) { // TODO: Consider reuse probability here?
			try {
				// TODO: Would casting be an option here?
				callee = test.getRandomObject(method.getDeclaringClass(), position);
				logger.debug("Found callee of type "+method.getDeclaringClass().getName());
			} catch(ConstructionFailedException e) {
				logger.debug("No callee of type "+method.getDeclaringClass().getName()+" found");
				
				// Check if we have usage information on how the callee is created: Check for information on parameter 0!
				if(!usage_model.hasGenerators(className, method, 0)) {
					logger.debug("Generating callee without usage information");
					callee = attemptGeneration(test, method.getDeclaringClass(), position, recursion_depth, true);
				} else {
					logger.debug("Generating callee with usage information");
					callee = attemptGenerationUsage(test, className, method, 0, position, recursion_depth, true);
				}
//				callee = attemptGeneration(test, method.getDeclaringClass(), position, recursion_depth, false);
				position += test.size() - length;
				length = test.size();
			}
			parameters = satisfyParameters(test, callee, callee.getClassName(), method, getParameterTypes(callee, method), position, recursion_depth);			
		} else {
			parameters = satisfyParameters(test, callee, method.getDeclaringClass().getName(), method, getParameterTypes(callee, method), position, recursion_depth);			
		}

		int new_length = test.size();
		position += (new_length - length);

		//VariableReference ret_val = new VariableReference(method.getGenericReturnType(), position);
		VariableReference ret_val = getReturnVariable(method, callee, position);
		
		test.addStatement(new MethodStatement(method, callee, ret_val, parameters), position);
		logger.debug("Success: Adding method "+method);

		return ret_val;
	}
	
	/**
	 * Add primitive statement at position
	 * @param test
	 * @param old
	 * @param position
	 * @return
	 * @throws ConstructionFailedException
	 */
	@SuppressWarnings("unchecked")
	private VariableReference addPrimitive(TestCase test, PrimitiveStatement<?> old, int position) throws ConstructionFailedException {
		logger.debug("Adding primitive");
		VariableReference ret_val = new VariableReference(old.getReturnType(), position);
		test.addStatement(new PrimitiveStatement(ret_val, old.getValue()), position);
		
		return ret_val;
	}
	
	/**
	 * Add a field to the test case
	 * @param test
	 * @param field
	 * @param position
	 * @return
	 * @throws ConstructionFailedException
	 */
	private VariableReference addField(TestCase test, Field field, int position) throws ConstructionFailedException {
		logger.debug("Adding field "+field.toGenericString());

		VariableReference callee = null;
		int length = test.size();

		if(!Modifier.isStatic(field.getModifiers())) {
			try {
				callee = test.getRandomObject(field.getDeclaringClass(), position);
			} catch(ConstructionFailedException e) {
				logger.debug("No callee of type "+field.getDeclaringClass().getName()+" found");
				callee = attemptGeneration(test, field.getDeclaringClass(), position, 0, false);
				position += test.size() - length;
				length = test.size();
			}
		}
		VariableReference ret_val = new VariableReference(field.getGenericType(), position);
		test.addStatement(new FieldStatement(field, callee, ret_val), position);
		
		return ret_val;
	}
	
	private AccessibleObject getCall(TestCase test, VariableReference var) {
		Statement s = test.getStatement(var.statement);
		if(s instanceof MethodStatement) {
			return ((MethodStatement)s).getMethod();
		} else if(s instanceof ConstructorStatement) {
			return ((ConstructorStatement)s).getConstructor();
		} else if(s instanceof FieldStatement) {
			return ((FieldStatement)s).getField();
		}
		return null;
	}
	
	/**
	 * Satisfy a list of parameters by reusing or creating variables
	 * @param test
	 * @param parameter_types
	 * @param position
	 * @param recursion_depth
	 * @param constraints
	 * @return
	 * @throws ConstructionFailedException
	 */
	private List<VariableReference> satisfyParameters(TestCase test, VariableReference callee, String className, AccessibleObject call, List<Type> parameter_types, int position, int recursion_depth) throws ConstructionFailedException {
		List<VariableReference> parameters = new ArrayList<VariableReference>();
		logger.debug("Trying to satisfy "+parameter_types.size()+" parameters for call "+call);
		int i = 0;
		for(Type parameter_type : parameter_types) {
			double reuse = randomness.nextDouble();
			int previous_length = test.size();

			// Collect all objects that satisfy the usage constraints
			List<VariableReference> objects = new ArrayList<VariableReference>();
			if(usage_model.hasGenerators(className, call, i+1)) {
				logger.debug("Using information for parameter selection...");
				for(VariableReference var : test.getObjects(parameter_type, position)) {
					AccessibleObject generator = getCall(test, var);
					if(usage_model.hasGenerator(className, call, i + 1, generator)) 
						objects.add(var);
				}
			} else {
				objects = test.getObjects(parameter_type, position);
			}
			
			if(callee != null)
				objects.remove(callee);
			logger.debug("Found suitable objects: "+objects.size());

			if(!objects.isEmpty() && reuse <= object_reuse_probability) {
				logger.debug(" Parameter "+i+": Reusing existing object of type "+parameter_type);
				VariableReference reference = randomness.choice(objects);
				parameters.add(reference);
			} else {
				logger.debug(" Parameter "+i+": Creating new object of type "+parameter_type);
				VariableReference reference;
				if(!usage_model.hasGenerators(className, call, i + 1)) {
					logger.debug("Have no usage information for this parameter");
					//logger.debug("Checking for "+className);
					//logger.debug("Might be found for class: "+call);
					if(!usage_model.hasClass(className))
						logger.debug("Don't have ANY usage information for the class!");
				} 
				GenericClass clazz = new GenericClass(parameter_type);
				if(clazz.isPrimitive() || clazz.isString() || clazz.isArray() || 
						!usage_model.hasGenerators(className, call, i + 1)) {
					reference = attemptGeneration(test, parameter_type, position, recursion_depth, true);
				} else {
					logger.debug("Have usage information for this parameter");
					reference = attemptGenerationUsage(test, className, call, i + 1, position, recursion_depth, true);
				}
				parameters.add(reference);						
			}
			int current_length = test.size();
			position += current_length - previous_length;
			i++;
		}
		logger.debug("Satisfied "+parameter_types.size()+" parameters");
		return parameters;
	}

	protected void assignArray(TestCase test, VariableReference array, int array_index, int position) throws ConstructionFailedException {
		List<VariableReference> objects = test.getObjects(array.getComponentType(), position);
		Iterator<VariableReference> iterator = objects.iterator();
		while(iterator.hasNext()) {
			VariableReference var = iterator.next();
			if(var.isArrayIndex() && var.getArray().equals(array))
				iterator.remove();
		}
		assignArray(test, array, array_index, position, objects);
	}
	
	protected void assignArray(TestCase test, VariableReference array, int array_index, int position, List<VariableReference> objects) throws ConstructionFailedException {
//		VariableReference index = array.getVariable(array_index).clone();
		VariableReference index = new VariableReference(array.clone(), array_index, array.array_length, position);
		//index.statement = position;
		if(!objects.isEmpty() && randomness.nextDouble() <= object_reuse_probability) {
			// Assign an existing value
			// TODO:
			// Do we need a special "[Array]AssignmentStatement"?
			test.addStatement(new AssignmentStatement(index, randomness.choice(objects).clone()), position);
			
		} else {
			// Assign a new value
			// Need a primitive, method, constructor, or field statement where retval is set to index
			// Need a version of attemptGeneration that takes retval as parameter

			// OR: Create a new variablereference and then assign it to array (better!)
			int old_len = test.size();
			VariableReference var = attemptGeneration(test, array.getComponentType(), position);
			position += test.size() - old_len;
			index.statement = position;
			test.addStatement(new AssignmentStatement(index, var.clone()), position);				
		}
	}
	
	private VariableReference createArray(TestCase test, Type type, int position, int recursion_depth) throws ConstructionFailedException {
		// Create array with random size
		VariableReference reference = new VariableReference(type, position); // TODO: Is this correct? -1;?
		ArrayStatement statement = new ArrayStatement(reference);
		test.addStatement(statement, position);
		position++;
		//logger.info(test.toCode());
		// For each value of array, call attemptGeneration
		List<VariableReference> objects = test.getObjects(reference.getComponentType(), position);
		for(int i = 0; i < statement.size(); i++) {
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
	
	public VariableReference attemptGeneration(TestCase test, Type type, int position) throws ConstructionFailedException {
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
	private VariableReference attemptGeneration(TestCase test, Type type, int position, int recursion_depth, boolean allow_null) throws ConstructionFailedException {
		GenericClass clazz = new GenericClass(type);
		
		if(clazz.isPrimitive() || clazz.isString()) {
			logger.debug("Generating primitive of type "+((Class<?>)type).getName());
			VariableReference reference = new VariableReference(type, position); // TODO: Is this correct? -1;?
			// TODO: Check before cast!
			test.addStatement(PrimitiveStatement.getRandomStatement(reference, type), position);
			return reference.clone();
		} else if(clazz.isArray()) {
			return createArray(test, type, position, recursion_depth).clone();

		} else {
			if(allow_null && randomness.nextDouble() <= null_probability) {
				logger.info("Using Null!");
				return new NullReference(type);
			}
			
			//logger.info("Current recursion list: "+current_recursion.size());
//			AccessibleObject o = mapping.getRandomGenerator(type, (ClassConstraint)constraint);
			AccessibleObject o = test_cluster.getRandomGenerator(type);
			if( o == null) {
				throw new ConstructionFailedException();
			} else if(o instanceof Field) {
				logger.debug("Attempting generating of "+type+" via field of type "+type);
				VariableReference ret = addField(test, (Field)o, position);
				logger.debug("Success in generating type "+type);
				return ret.clone();
			} else if(o instanceof Method) {
				logger.debug("Attempting generating of "+type+" via method "+((Method)o).getName()+ " of type "+type);
				VariableReference ret = addMethod(test, (Method)o, position, recursion_depth + 1);
				logger.debug("Success in generating type "+type);
				return ret.clone();
			} else if(o instanceof Constructor<?>) {
				logger.debug("Attempting generating of "+type+" via constructor "+((Constructor<?>)o).getName()+ " of type "+type);
				VariableReference ret = addConstructor(test, (Constructor<?>)o, position, recursion_depth + 1);
				logger.debug("Success in generating type "+type);
				return ret.clone();
			} else {
				logger.debug("No generators found for type "+type);
				throw new ConstructionFailedException();
			}
		}
		// TODO: Sometimes we could use null 
	}

	private VariableReference attemptGenerationUsage(TestCase test, String className, AccessibleObject call, int parameter, int position, int recursion_depth, boolean allow_null) throws ConstructionFailedException {			
		AccessibleObject o = usage_model.getGenerator(className, call, parameter);
		if( o == null) {
			throw new ConstructionFailedException();
		} else if(o instanceof Field) {
			logger.debug("Attempting generating of via field "+o);
			VariableReference ret = addField(test, (Field)o, position);
			logger.debug("Success in generating type ");
			return ret.clone();
		} else if(o instanceof Method) {
			logger.debug("Attempting generating via method "+((Method)o).getName());
			VariableReference ret = addMethod(test, (Method)o, position, recursion_depth + 1);
			logger.debug("Success in generating");
			return ret.clone();
		} else if(o instanceof Constructor<?>) {
			logger.debug("Attempting generating via constructor "+((Constructor<?>)o).getName());
			VariableReference ret = addConstructor(test, (Constructor<?>)o, position, recursion_depth + 1);
			logger.debug("Success in generating type ");
			return ret.clone();
		} else {
			logger.debug("No generators found for type ");
			throw new ConstructionFailedException();
		}

	}
	/**
	 * Append given call to the test case at given position
	 * @param test
	 * @param call
	 * @param position
	 */
	private void addCallFor(TestCase test, VariableReference callee, AccessibleObject call, int position) {
		int previous_length = test.size();
		current_recursion.clear();
		try {
			if(call instanceof Method) {
				addMethodFor(test, callee, (Method)call, position);
			} 
		} catch(ConstructionFailedException e) {
			// TODO: Check this!
			logger.debug("Inserting call "+call+" has failed. Removing statements");
			//System.out.println("TG: Failed");			
			// TODO: Doesn't work if position != test.size()
			while(test.size() != previous_length) {
				logger.debug("  Removing statement: "+test.getStatement(position).getCode());
				test.remove(position);
			}
		}
	}
	
	/**
	 * Insert a random call at given position
	 * @param test
	 * @param position
	 */
	private void insertRandomCall(TestCase test, int position) {
		int previous_length = test.size();
		String name = "";
		current_recursion.clear();
		try { 
//			AccessibleObject o = mapping.getRandomCall();
			AccessibleObject o = test_cluster.getRandomTestCall();
			if(o instanceof Constructor<?>) {
				Constructor<?> c = (Constructor<?>)o;
				logger.debug("Adding constructor call "+c.getName());
				name = c.getName();
				addConstructor(test, c, position, 0);
			} else if (o instanceof Method) {
				Method m = (Method)o;
				logger.debug("Adding method call "+m.getName());
				name = m.getName();
				addMethod(test, m, position, 0);
			} else {
				logger.error("Got type other than method or constructor!");
			}
		} catch(ConstructionFailedException e) {
			// TODO: Check this!
			logger.debug("Inserting statement "+name+" has failed. Removing statements");
			//System.out.println("TG: Failed");			
			// TODO: Doesn't work if position != test.size()
			while(test.size() != previous_length) {
				logger.debug("  Removing statement: "+test.getStatement(position).getCode());
				test.remove(position);
			}
			
			//logger.info("Attempting search");
			//test.chop(previous_length);
		}
	}
	
	/**
	 * Replace the statement with a new statement using given call
	 * @param test
	 * @param statement
	 * @param call
	 * @throws ConstructionFailedException
	 */
	public void changeCall(TestCase test, Statement statement, AccessibleObject call) throws ConstructionFailedException {
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

	
	private VariableReference getReturnVariable(Method method, VariableReference callee, int position) {
		
		VariableReference ret_val = new VariableReference(method.getGenericReturnType(), position);
		
		// Casting
		// Case 1: clone() on cloneable -> cast to type of defining class
		if(method.getReturnType().equals(Object.class) && method.getName().equals("clone")) {
			ret_val.setType(method.getDeclaringClass());
			logger.debug("Found clone method: Changing type to: "+ret_val.getType());
			// TODO: Need to cast this!
			return ret_val;
		}
		
		if(callee == null)
			ret_val.setType(descriptor_replacement.getReturnType(method.getDeclaringClass().getName(), method));
		else
			ret_val.setType(descriptor_replacement.getReturnType(callee.getVariableClass().getName(), method));
		
		return ret_val;
	}
	
	private List<Type> getParameterTypes(VariableReference callee, Method method) {
		if(callee == null)
			return descriptor_replacement.getParameterTypes(method);
		else
			return descriptor_replacement.getParameterTypes(callee.getType(), method);
	}
	
	private List<Type> getParameterTypes(Constructor<?> constructor) {
		return descriptor_replacement.getParameterTypes(constructor);
	}
	
	public VariableReference addConstructorWith(TestCase test, VariableReference parameter, Constructor<?> constructor, int position) throws ConstructionFailedException {

		logger.debug("Adding constructor "+constructor.toGenericString());

		int length = test.size();
		List<VariableReference> parameters = satisfyParameters(test, null, constructor.getDeclaringClass().getName(), constructor, getParameterTypes(constructor), position, 0);
		// Force one parameter to be "parameter"
		if(!parameters.contains(parameter)) {
			int num = 0;
			for(Type type: constructor.getGenericParameterTypes()) {
				if(parameter.isAssignableTo(type)) {
					parameters.set(num, parameter);
					break;
				}
				num++;
			}
		}
		int new_length = test.size();
		position += (new_length - length);
		VariableReference ret_val = new VariableReference(constructor.getDeclaringClass(), position);
		test.addStatement(new ConstructorStatement(constructor, ret_val, parameters), position);
		
		return ret_val;
	}
	
	public VariableReference addMethodFor(TestCase test, VariableReference callee, Method method, int position) throws ConstructionFailedException {
		logger.debug("Adding method "+method.toGenericString());
		int length = test.size();
		List<VariableReference> parameters = null;
		parameters = satisfyParameters(test, callee, callee.getClassName(), method, getParameterTypes(callee, method), position, 0);
		int new_length = test.size();
		position += (new_length - length);
		VariableReference ret_val = getReturnVariable(method, callee, position);
		test.addStatement(new MethodStatement(method, callee, ret_val, parameters), position);
		
		logger.debug("Success: Adding method "+method);
		return ret_val;
	}
	
	private Set<Type> getDependencies(Method method) {
		Set<Type> dependencies = new HashSet<Type>();
		if(!Modifier.isStatic(method.getModifiers())) {
			dependencies.add(method.getDeclaringClass());
		}
		for(Type type : method.getGenericParameterTypes()) {
			dependencies.add(type);
		}
		
		return dependencies;
	}
	private Set<Type> getDependencies(Constructor<?> constructor) {
		Set<Type> dependencies = new HashSet<Type>();
		for(Type type : constructor.getGenericParameterTypes()) {
			dependencies.add(type);
		}
		
		return dependencies;
	}
	private Set<Type> getDependencies(Field field) {
		Set<Type> dependencies = new HashSet<Type>();
		if(!Modifier.isStatic(field.getModifiers())) {
			dependencies.add(field.getDeclaringClass());
		}
		
		return dependencies;
	}
	private boolean dependenciesSatisfied(Set<Type> dependencies, List<VariableReference> objects) {
		for(Type type : dependencies) {
			boolean found = false;
			for(VariableReference var : objects) {
				if(var.getType().equals(type)) {
					found = true;
					break;
				}
			}
			if(!found)
				return false;
		}
		return true;
	}
	
	// TODO: Return all possible statements that have this return type
	private List<AccessibleObject> getPossibleCalls(Type return_type, List<VariableReference> objects) {
		List<AccessibleObject> calls = new ArrayList<AccessibleObject>();
		List<AccessibleObject> all_calls;
		
		try {
			all_calls = test_cluster.getGenerators(return_type);
		} catch (ConstructionFailedException e) {
			return calls;
		}
		
		for(AccessibleObject call : all_calls) {
			Set<Type> dependencies = null;
			if(call instanceof Method) {
				dependencies = getDependencies((Method)call);
			} else if(call instanceof Constructor<?>) {
				dependencies = getDependencies((Constructor<?>)call);
			} else if(call instanceof Field) {
				dependencies = getDependencies((Field)call);		
			} else {
				assert(false);
			}
			if(dependenciesSatisfied(dependencies, objects))
				calls.add(call);
		}
		
		// TODO: What if primitive?
		
		return calls;
	}
	
	public boolean changeRandomCall(TestCase test, Statement statement) {
		List<VariableReference> objects = test.getObjects(statement.getReturnValue().statement);
		objects.remove(statement.getReturnValue());
		List<AccessibleObject> calls = getPossibleCalls(statement.getReturnType(), objects);
		logger.debug("Got "+calls.size()+" possible calls for "+objects.size()+" objects");
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
