package de.unisb.cs.st.evosuite.coverage.behavioral;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Type;

import de.unisaarland.cs.st.adabu.core.state.Value;
import de.unisaarland.cs.st.adabu.util.asm.MyType;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.AbstractFitnessFactory;

/** Class that provides the functions to compute the behavioral coverage goals for an object state. */
public class BehavioralCoverageFactory extends AbstractFitnessFactory {
	
	/** The targets to compute the coverage goals for. */
	private AccessibleObject[] targets;
	
	/**
	 * Creates a new factory with given targets.
	 * <tt>Non-public</tt> target constructors will be ignored.</p>
	 * 
	 * @param targets - the target array of constructors.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given target array is <tt>null</tt>.
	 */
	public BehavioralCoverageFactory(Constructor<?>[] targets) {
		if (targets == null)
			throw new IllegalArgumentException("The given target array is null!");
		
		LinkedList<Constructor<?>> constructors = new LinkedList<Constructor<?>>();
		for (Constructor<?> constructor : targets) {
			// check whether the constructor is public
			int mod = constructor.getModifiers();
			if (Modifier.isPublic(mod)) {
				constructors.add(constructor);
			}
		}
		
		this.targets = constructors.toArray(new Constructor<?>[constructors.size()]);
	}
	
	/**
	 * Creates a new factory with given targets.
	 * <tt>Non-public</tt> or <tt>static</tt> target methods will be ignored.</p>
	 * 
	 * @param targets - the target array of methods.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given target array is <tt>null</tt>.
	 */
	public BehavioralCoverageFactory(Method[] targets) {
		if (targets == null)
			throw new IllegalArgumentException("The given target array is null!");
		
		LinkedList<Method> methods = new LinkedList<Method>();
		for (Method method : targets) {
			// check whether the method is public
			int mod = method.getModifiers();
			if (Modifier.isPublic(mod) && !Modifier.isStatic(mod)) {
				methods.add(method);
			}
		}
		
		this.targets = methods.toArray(new Method[methods.size()]);
	}
	
	/**
	 * Computes the list of behavioral coverage goals for the targets given
	 * by {@link BehavioralCoverageFactory#targets}, whereby a target is either
	 * a constructor or method.</br>
	 * The computed goals are not aiming to cover a certain destination in the
	 * source code of the target, but to cover various calls of the target.
	 * 
	 * <p>For each target the following goals are computed:</br>
	 * <ol>
	 * <li><b>The target has no parameters.</b></br>
	 * If the target doesn't take parameters then one goal is added to the
	 * result goal list that simply aims to call the target with no parameters.</li>
	 * </br></br>
	 * <li><b>The target has parameters.</b></br>
	 * If the target takes parameters then several goals must be added to the
	 * result goal list. Each parameter refers to an abstract domain containing
	 * abstract values given by abstraction.</br>
	 * The computed goals aim to cover every possible combination of target calls
	 * restricting the possible parameters values to their abstract domains.</li>
	 * </ol>
	 * </p>
	 * 
	 * @return the list of fitness functions representing the
	 *         coverage goals.
	 */
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {
		List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();
		for (AccessibleObject target : targets) {
			goals.addAll(getCoverageGoals(target));
		}
		return goals;
	}
	
	/**
	 * <b>Helper method.</b></br>
	 * Computes the behavioral coverage goals for an individual
	 * constructor or method as specified by {@link #getCoverageGoals()}.</p>
	 * 
	 * @param target - the constructor or method to compute the coverage goals for.
	 * 
	 * @return the list of fitness functions representing the
	 *         coverage goals.
	 */
	private List<TestFitnessFunction> getCoverageGoals(AccessibleObject target) {
		assert (!(target instanceof Field));
		
		List<TestFitnessFunction> goals;
		Class<?>[] parameters;
		
		// get the parameters
		if (target instanceof Constructor<?>) { // if target is constructor
			parameters = ((Constructor<?>) target).getParameterTypes();
		} else { // else target must be a method
			parameters = ((Method) target).getParameterTypes();
		}
		
		// create goals for every abstract parameter domain
		List<Value[]> parameterValues = getParameterGoals(parameters);
		
		// for every parameter array create a new fitness-function
		goals = new ArrayList<TestFitnessFunction>();
		for (Value[] values : parameterValues) {
			goals.add(new BehavioralCoverageTestFitness(values, target));
		}
		return goals;
	}
	
	/**
	 * <b>Helper method.</b></br>
	 * Computes the list of goal calls with the corresponding
	 * abstract values for a list of parameters.</p>
	 * 
	 * @param parameters - the list of parameters.
	 * 
	 * @return the list of parameter goal calls.
	 */
	private List<Value[]> getParameterGoals(Class<?>[] parameters) {
		List<Value[]> result = new ArrayList<Value[]>();
		addParameterGoals(parameters, 0, result, new Value[0]);
		return result;
	}
	
	/**
	 * <b>Helper method.</b></br>
	 * Computes the array of abstract parameter values. Given the list of
	 * parameters and a start index, the method iterates recursively over the
	 * possible combinations beginning with the parameter at the position
	 * in the list specified by the start index.
	 * 
	 * <p><b>Note:</b> As side effect this method adds all computed goal arrays
	 * of possible abstract parameter values to the given result list.</p>
	 * 
	 * @param parameters - the list of parameters.
	 * @param index - the current parameter index in the recursion.
	 * @param result - the result list to add the goals.
	 * @param currentValues - the current values in the recursion.
	 */
	private void addParameterGoals(Class<?>[] parameters, int index, List<Value[]> result, Value[] currentValues) {
		// check whether last parameter has been reached
		if (index < parameters.length) {
			// get the abstract domain of the current parameter
			Value[] abstractDomain = getAbstractDomain(parameters[index]);
			
			// for every abstract value create a new goal
			for (Value value : abstractDomain) {
				Value[] newValues = new Value[parameters.length];
				for (int i = 0; i < index; i++) { // add all old values
					newValues[i] = currentValues[i]; 
				}
				newValues[index] = value; // add the new value
				addParameterGoals(parameters, index+1, result, newValues);
			}
		} else { // add the current values to result
			result.add(currentValues);
		}
	}
	
	/**
	 * Computes the abstract domain for a given class.</p>
	 * 
	 * @param clazz - the class to get the abstract domain for.
	 * 
	 * @return the values of the abstract domain.
	 */
	public static Value[] getAbstractDomain(Class<?> clazz) {
		Value[] domain;
		MyType type = new MyType(Type.getType(clazz).getDescriptor());
		if (clazz.equals(boolean.class)) {
			domain = new Value[2];
			domain[0] = new Value(new Boolean(true), type);
			domain[1] = new Value(new Boolean(false), type);
		} else if (clazz.equals(char.class)) {
			domain = new Value[1];
			domain[0] = new Value(new Character('0'), type);
		} else if (clazz.equals(byte.class)) {
			domain = new Value[3];
			domain[0] = new Value(new Byte((byte) -1), type);
			domain[1] = new Value(new Byte((byte) 0), type);
			domain[2] = new Value(new Byte((byte) 1), type);
		} else if (clazz.equals(short.class)) {
			domain = new Value[3];
			domain[0] = new Value(new Short((short) -1), type);
			domain[1] = new Value(new Short((short) 0), type);
			domain[2] = new Value(new Short((short) 1), type);
		} else if (clazz.equals(int.class)) {
			domain = new Value[3];
			domain[0] = new Value(new Integer(-1), type);
			domain[1] = new Value(new Integer(0), type);
			domain[2] = new Value(new Integer(1), type);
		} else if (clazz.equals(long.class)) {
			domain = new Value[3];
			domain[0] = new Value(new Long(-1), type);
			domain[1] = new Value(new Long(0), type);
			domain[2] = new Value(new Long(1), type);
		} else if (clazz.equals(float.class)) {
			domain = new Value[3];
			domain[0] = new Value(new Float(-1f), type);
			domain[1] = new Value(new Float(0f), type);
			domain[2] = new Value(new Float(1f), type);
		} else if (clazz.equals(double.class)) {
			domain = new Value[3];
			domain[0] = new Value(new Double(-1d), type);
			domain[1] = new Value(new Double(0d), type);
			domain[2] = new Value(new Double(1d), type);
		} else if (clazz.equals(void.class) || clazz.equals(Void.class)) {
			domain = new Value[1];
			domain[0] = new Value(null, type);
		} else if (clazz.isEnum()) {
			int enum_count = clazz.getEnumConstants().length;
			if (enum_count > 0) { // one could also add null values here
				domain = new Value[enum_count];
				for (int i = 0; i < enum_count; i++) {
					domain[i] = new Value(clazz.getEnumConstants()[i], type);
				}
			} else {
				domain = new Value[1];
				domain[0] = new Value(null, type);
			}
		} else if (clazz.equals(Boolean.class) || clazz.equals(Character.class)
				|| clazz.equals(Byte.class) || clazz.equals(Short.class)
				|| clazz.equals(Integer.class) || clazz.equals(Long.class)
				|| clazz.equals(Float.class) || clazz.equals(Double.class)) {
			domain = new Value[1];
			domain[0] = new Value(new Object(), type);
		} else { // complex or array type
			domain = new Value[2];
			domain[0] = new Value(null, type);
			domain[1] = new Value(new Object(), type);
		}
		
		return domain;
	}
}
