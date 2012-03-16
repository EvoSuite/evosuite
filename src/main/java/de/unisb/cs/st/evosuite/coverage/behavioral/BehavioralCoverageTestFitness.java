package de.unisb.cs.st.evosuite.coverage.behavioral;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import de.unisaarland.cs.st.adabu.core.state.Value;
import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.ExecutionResult;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

/** Class that represents a fitness function of a behavioral coverage goal. */
public class BehavioralCoverageTestFitness extends TestFitnessFunction {
	
	private static final long serialVersionUID = -5629800086160967984L;
	
	/** The parameter values to reach. */
	private Value[] goal;
	
	/** The target object of the goal. */
	private AccessibleObject target;
	
	/**
	 * Creates a new fitness function with given goal values
	 * and target object.</p>
	 * 
	 * @param goal - the goal values.
	 * @param target - the target object.
	 * 
	 * @throws IllegalArgumentException
	 *             if one of the parameters is <tt>null</tt>.
	 */
	public BehavioralCoverageTestFitness(Value[] goal, AccessibleObject target) {
		if (goal == null)
			throw new IllegalArgumentException("The given array of goal values is null!");
		if (target == null)
			throw new IllegalArgumentException("The given target object is null!");
		
		this.goal = goal;
		this.target = target;
	}
	
	/**
	 * Calculates the fitness of an individual. The fitness of an individual
	 * is the sum of parameter deviation from the goal values. For every
	 * parameter value of the target call there is calculated an additional
	 * fitness that indicates how far away the parameter value is to fulfill
	 * the required goal value.</p>
	 * 
	 * @param individual - the individual to calculate the fitness for.
	 * @param result - the execution result of the individual.
	 * 
	 * @return the fitness of the given individual.
	 */
	@Override
	public double getFitness(TestChromosome individual, ExecutionResult result) {
		assert (!(target instanceof Field));
		
		double fitness = 0.0;
		String classname;
		Scope scope;
		TestCase test = individual.getTestCase();
		
		// check for existing statements
		if (test.isEmpty())
			return Double.MAX_VALUE;
		
		// get the target class name
		if (target instanceof Constructor<?>) {
			classname = ((Constructor<?>) target).getDeclaringClass().getName();
		} else {
			classname = ((Method) target).getDeclaringClass().getName();
		}
		
		// prepare for fitness computation
		scope = new Scope();
		for (int i = 0; i < test.size(); i++) {
			StatementInterface statement = test.getStatement(i);
			int count = 0; // number of constructor calls
			
			// check for not allowed statements
			if (target instanceof Constructor<?>) {
				// only the last statement may be a constructor call of the target class
				if (statement instanceof ConstructorStatement && i != test.size()-1) {
					Constructor<?> cons = ((ConstructorStatement) statement).getConstructor();
					if (classname.equals(cons.getDeclaringClass().getName()))
						return Double.MAX_VALUE;
				}
			} else {
				// only one constructor call of the target class is allowed
				if (statement instanceof ConstructorStatement) {
					Constructor<?> cons = ((ConstructorStatement) statement).getConstructor();
					if (classname.equals(cons.getDeclaringClass().getName())) {
						count++;
						if (count > 1)
							return Double.MAX_VALUE;
					}
				}
				// only the last statement may be a method invocation of the target class
				if (statement instanceof MethodStatement && i != test.size()-1) {
					Method method = ((MethodStatement) statement).getMethod();
					if (classname.equals(method.getDeclaringClass().getName()))
						return Double.MAX_VALUE;
				}
			}
			
			// execute the statement to learn the parameters values
			try {
				statement.execute(scope, System.out);
			} catch (Exception e) {
				System.out.print("* Warning: Error occurred executing a test during fitness computation: " + e.getMessage());
				return Double.MAX_VALUE;
			}
		}
		
		// compute the fitness for last statement
		StatementInterface statement = test.getStatement(test.size()-1);
		// check whether the method to generate is a constructor
		if (target instanceof Constructor<?>) {
			// check for constructor statement
			if (statement instanceof ConstructorStatement) {
				ConstructorStatement constructorStatement = (ConstructorStatement) statement;
				Constructor<?> constructor = constructorStatement.getConstructor();
				
				// check for correct class and constructor
				if (!constructor.equals((Constructor<?>) target)) {
					fitness = Double.MAX_VALUE;
				} else {
					List<VariableReference> var_refs = constructorStatement.getParameterReferences();
					
					// check the parameter size
					if (var_refs.size() != goal.length)
						return Double.MAX_VALUE;
					
					// for every parameter compute the corresponding fitness
					for (int i = 0; i < var_refs.size(); i++) { // no parameters fitness 0.0 is fine
						fitness += additionalFitness(var_refs.get(i), scope, goal[i]);
					}
				}
			} else { // no constructor statement - maximize fitness
				fitness = Double.MAX_VALUE;
			}
		} else { // actually the same as for constructor
			// check for method statement
			if (statement instanceof MethodStatement) {
				MethodStatement methodStatement = (MethodStatement) statement;
				Method method = methodStatement.getMethod();
				
				// check for correct class and method
				if (!method.equals((Method) target)) {
					fitness = Double.MAX_VALUE;
				} else {
					List<VariableReference> var_refs = methodStatement.getParameterReferences();
					
					// check the parameter size
					if (var_refs.size() != goal.length)
						return Double.MAX_VALUE;
					
					// for every parameter compute the corresponding fitness
					for (int i = 0; i < var_refs.size(); i++) { // no parameters fitness 0.0 is fine
						fitness += additionalFitness(var_refs.get(i), scope, goal[i]);
					}
				}
			} else { // no method statement - maximize fitness
				fitness = Double.MAX_VALUE;
			}
		}
		
		return fitness;
	}
	
	/**
	 * <b>Helper method.</b></br>
	 * Calculates the fitness of a parameter, whereby the fitness
	 * is the parameter deviation from the goal value.</p>
	 * 
	 * @param var_ref - the parameter variable.
	 * @param scope - the scope holding the parameter value.
	 * @param goalValue - the parameter goal value.
	 * 
	 * @return the parameter fitness of the given variable.
	 */
	private double additionalFitness(VariableReference var_ref, Scope scope, Value goalValue) {
		double fitness = 0.0;
		
		try {
			Class<?> varType = var_ref.getVariableClass();
			if (var_ref.isPrimitive()) { // var_ref has a primitive type
				Object obj1 = var_ref.getObject(scope);
				Object obj2 = goalValue.getData();
				if (obj1 == null || obj2 == null) { // handle null
					fitness = (obj1 == obj2) ? 0.0 : 1000.0;
				} else if (varType.equals(void.class)) {
					fitness = 0.0; // nothing to fulfill
				} else if (varType.equals(boolean.class)) {
					boolean variable = (Boolean) obj1;
					boolean goalVal = (Boolean) obj2;
					fitness = (variable == goalVal) ? 0.0 : 1000.0;
				} else if (varType.equals(char.class)) {
					fitness = 0.0; // nothing to fulfill
				} else { // var_ref has a numerical type
					double variable = ((Number) obj1).doubleValue();
					double goalVal = ((Number) obj2).doubleValue();
					if (goalVal > 0.0) { // positive value in abstract domain is 1
						fitness = (variable > 0.0) ? 0.0 : Math.abs(variable)+1;
					} else if (goalVal < 0.0) { // negative value in abstract domain is -1
						fitness = (variable < 0.0) ? 0.0 : variable+1;
					} else { // goalVal == 0.0
						fitness = Math.abs(variable);
					}
				}
			} else if (var_ref.isEnum()) { // var_ref has enumeration type
				Object variable = var_ref.getObject(scope);
				Object goalVal = goalValue.getData();
				fitness = (variable == goalVal) ? 0.0 : 1000.0;
			} else { // var_ref has a complex type (includes wrapper types) or is array type
				Object variable = var_ref.getObject(scope);
				if (goalValue.isNull()) {
					fitness = (variable == null) ? 0.0 : 1000.0;
				} else { // goalValue != null
					fitness = (variable != null) ? 0.0 : 1000.0;
				}
			}
		} catch (Exception e) {
			System.out.print("* Warning: Error occurred during fitness computation: " + e.getMessage());
			fitness = Double.MAX_VALUE;
		}
		return fitness;
	}
	
	/**
	 * Returns the goal of this fitness function.</p>
	 * 
	 * @return the goal of this fitness function.
	 */
	public Value[] getGoal() {
		return goal;
	}
	
	/**
	 * Returns the target object of this fitness function.</p>
	 * 
	 * @return the target object of this fitness function.
	 */
	public AccessibleObject getTarget() {
		return target;
	}
}
