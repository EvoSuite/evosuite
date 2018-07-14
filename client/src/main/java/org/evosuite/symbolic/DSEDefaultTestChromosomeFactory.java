package org.evosuite.symbolic;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.dse.TestCaseBuilder;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.variable.VariableReference;
import org.objectweb.asm.Type;

public class DSEDefaultTestChromosomeFactory extends DSEDefaultChromosomeFactory<TestChromosome> {

	/**
	 * Returns a set with the static methods of a class
	 * 
	 * @param targetClass
	 *            a class instance
	 * @return
	 */
	private static Set<Method> getTargetStaticMethods(Class<?> targetClass) {
		Method[] declaredMethods = targetClass.getDeclaredMethods();
		Set<Method> targetStaticMethods = new HashSet<Method>();
		for (Method m : declaredMethods) {
			if (Modifier.isStatic(m.getModifiers()) && !Modifier.isPrivate(m.getModifiers())) {
				targetStaticMethods.add(m);
			}
		}
		return targetStaticMethods;
	}

	private final List<TestChromosome> defaultTestChromosomes;
	private int currentTestChrommosomeIndex;

	/**
	 * Builds a default test case for a static target method
	 * 
	 * @param targetStaticMethod
	 * @return
	 */
	private DefaultTestCase buildDefaultTestCase(Method targetStaticMethod) {
		TestCaseBuilder testCaseBuilder = new TestCaseBuilder();

		Type[] argumentTypes = Type.getArgumentTypes(targetStaticMethod);

		ArrayList<VariableReference> arguments = new ArrayList<VariableReference>();
		for (Type argumentType : argumentTypes) {
			switch (argumentType.getSort()) {
			case Type.INT: {
				VariableReference variableReference = testCaseBuilder.appendIntPrimitive(0);
				arguments.add(variableReference);
				break;
			}
			default: {
				throw new UnsupportedOperationException();
			}
			}
		}
		testCaseBuilder.appendMethod(targetStaticMethod, arguments.toArray(new VariableReference[] {}));
		DefaultTestCase testCase = testCaseBuilder.getDefaultTestCase();
		return testCase;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2007698806862876842L;

	public DSEDefaultTestChromosomeFactory() {
		defaultTestChromosomes = initializeDefaultTestChromosomes();

	}

	/**
	 * Creates a list of test chromosomes (one per target static method)
	 * 
	 * @return
	 */
	private List<TestChromosome> initializeDefaultTestChromosomes() {
		List<TestChromosome> defaultTestChromosomes = new ArrayList<TestChromosome>();
		final Class<?> targetClass = Properties.getTargetClassAndDontInitialise();
		final Set<Method> targetStaticMethods = getTargetStaticMethods(targetClass);
		if (targetStaticMethods.isEmpty()) {
			// create empty test chromosome
			TestChromosome testChromosome = new TestChromosome();
			defaultTestChromosomes.add(testChromosome);
		} else {

			for (Method staticMethod : targetStaticMethods) {
				DefaultTestCase testCase = buildDefaultTestCase(staticMethod);
				TestChromosome testChromosome = new TestChromosome();
				testChromosome.setTestCase(testCase);
				defaultTestChromosomes.add(testChromosome);
			}
		}
		return defaultTestChromosomes;
	}

	@Override
	public TestChromosome getChromosome() {
		if (currentTestChrommosomeIndex < defaultTestChromosomes.size()) {
			TestChromosome testChromosome = defaultTestChromosomes.get(currentTestChrommosomeIndex);
			currentTestChrommosomeIndex++;
			return testChromosome;
		} else {
			return null;
		}
	}

	@Override
	public int numberOfDefaultChromosomes() {
		return defaultTestChromosomes.size();
	}

	@Override
	public TestChromosome getDefaultChromosome(int i) {
		return defaultTestChromosomes.get(i);
	}

}
