package org.evosuite.coverage.epa;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testsuite.AbstractTestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;
import org.objectweb.asm.Type;
import org.xml.sax.SAXException;

/**
 * This fitness function counts the degree of covered transitions. It is a
 * minimization function (less is better). The value 0.0 means all transitions
 * were covered.
 * 
 * @author galeotti
 *
 */
public class EPATransitionCoverageSuiteFitness extends TestSuiteFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9069407017356785315L;

	private final EPA epa;

	public EPATransitionCoverageSuiteFitness(String epaXMLFilename) {
		if (epaXMLFilename == null) {
			throw new IllegalArgumentException("epa XML Filename cannot be null");
		}
		try {
			this.epa = EPAFactory.buildEPA(epaXMLFilename);

			checkEPAStates(Properties.TARGET_CLASS);
			checkEPAActionNames(Properties.TARGET_CLASS);

		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new EvosuiteError(e);
		}
	}

	private void checkEPAStates(String className) {
		try {
			for (EPAState epaState : this.epa.getStates()) {
				boolean found;
				found = hasMethodOrConstructor(className, "reportState" + epaState.getName(), "()V");
				if (!found) {
					throw new EvosuiteError("Report method for EPA State " + epaState.getName()
							+ " was not found in target class " + className);
				}
			}
		} catch (ClassNotFoundException e) {
			throw new EvosuiteError(e);
		}
	}

	private void checkEPAActionNames(String className) {
		try {
			for (EPATransition transition : this.epa.getTransitions()) {
				final String actionName = transition.getActionName();
				String[] parts = actionName.split("\\(");
				final String methodName = parts[0];
				final String methodSignature = "(" + parts[1];

				boolean found = hasMethodOrConstructor(className, methodName, methodSignature);
				if (!found) {
					throw new EvosuiteError(
							"EPA Action Name " + actionName + "was not found in target class " + className);
				}
			}
		} catch (ClassNotFoundException e) {
			throw new EvosuiteError(e);
		}
	}

	private static boolean hasMethodOrConstructor(String className, final String methodName,
			final String methodSignature) throws ClassNotFoundException {
		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(className);
		for (Method method : clazz.getDeclaredMethods()) {
			String methodDescriptor = Type.getMethodDescriptor(method);
			if ((methodDescriptor.equals(methodSignature)) && method.getName().equals(methodName)) {
				return true;
			}
		}
		for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			String constructorDescriptor = Type.getConstructorDescriptor(constructor);
			if ((constructorDescriptor.equals(methodSignature)) && "<init>".equals(methodName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Fitness value is the number of uncovered EPA transitions
	 */
	@Override
	public double getFitness(AbstractTestSuiteChromosome<? extends ExecutableChromosome> suite) {

		List<ExecutionResult> executionResults = runTestSuite(suite);
		List<EPATrace> epaTraces = new ArrayList<EPATrace>();
		for (ExecutionResult executionResult : executionResults) {
			try {
				List<EPATrace> newEpaTraces = EPATraceFactory.buildEPATraces(Properties.TARGET_CLASS,
						executionResult.getTrace(), epa);
				epaTraces.addAll(newEpaTraces);
			} catch (MalformedEPATraceException e) {
				throw new EvosuiteError(e);
			}
		}

		final Set<EPATransition> tracedEpaTransitions = epaTraces.stream().map(EPATrace::getEpaTransitions)
				.flatMap(Collection::stream).collect(Collectors.toSet());

		final int epaTransitionSize = epa.getTransitions().size();
		final int tracedEpaTransitionSize = tracedEpaTransitions.size();
		final int uncoveredEpaTransitions = epaTransitionSize - tracedEpaTransitionSize;
		final double fitness = uncoveredEpaTransitions;

		updateIndividual(this, suite, fitness);

		return fitness;
	}

}
