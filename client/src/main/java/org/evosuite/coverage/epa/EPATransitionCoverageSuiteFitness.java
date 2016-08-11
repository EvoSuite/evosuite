package org.evosuite.coverage.epa;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.EvosuiteError;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
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
public class EPATransitionCoverageSuiteFitness extends EPASuiteFitness {

	public EPATransitionCoverageSuiteFitness(String epaXMLFilename) {
		super(epaXMLFilename);
	}

	@Override
	protected EPATransitionCoverageFactory getGoalFactory() {
		return new EPATransitionCoverageFactory(Properties.TARGET_CLASS, getEPA());
	}


}
