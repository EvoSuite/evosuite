package org.evosuite.testcase;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.setup.ResourceList;
import org.evosuite.testcarver.extraction.CarvingRunListener;
import org.evosuite.utils.Randomness;
import org.junit.runner.JUnitCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JUnitTestCarvedChromosomeFactory implements
		ChromosomeFactory<TestChromosome> {

	private static final long serialVersionUID = -569338946355072318L;
	
	private static final Logger logger = LoggerFactory.getLogger(JUnitTestCarvedChromosomeFactory.class);

	private List<TestCase> junitTests = new ArrayList<TestCase>();
	
	private final ChromosomeFactory<TestChromosome> defaultFactory;

	public JUnitTestCarvedChromosomeFactory(ChromosomeFactory<TestChromosome> defaultFactory) {
		this.defaultFactory = defaultFactory;
		readTestCases();
	}
	
	private void readTestCases() {
		JUnitCore runner = new JUnitCore();
		CarvingRunListener listener = new CarvingRunListener();
		runner.addListener(listener);
		Pattern pattern = Pattern.compile(Properties.JUNIT_PREFIX.replace('.', File.separatorChar)+"*.class");
		Collection<String> junitTestNames = ResourceList.getResources(pattern);
		List<Class<?>> junitTestClasses = new ArrayList<Class<?>>();
		for(String className : junitTestNames) {
			
			try {
				Class<?> junitClass = TestGenerationContext.getClassLoader().loadClass(className);
				junitTestClasses.add(junitClass);
			} catch (ClassNotFoundException e) {
				logger.warn("Error trying to load JUnit test class "+className);
			}
		}
		
		Class<?>[] classes = new Class<?>[junitTestClasses.size()];
		junitTestClasses.toArray(classes);
		runner.run(classes);
		junitTests.addAll(listener.getTestCases());
	}
	
	@Override
	public TestChromosome getChromosome() {
		int N_mutations = Properties.SEED_MUTATIONS;
		double P_clone = Properties.SEED_CLONE;

		if (Randomness.nextDouble() >= P_clone || junitTests.isEmpty()) {
			logger.info("Using random test");
			return defaultFactory.getChromosome();
		}

		// Cloning
		logger.debug("Cloning user test");
		TestCase test = Randomness.choice(junitTests);
		TestChromosome chromosome = new TestChromosome();
		chromosome.setTestCase(test.clone());
		if (N_mutations > 0) {
			int numMutations = Randomness.nextInt(N_mutations);
			logger.debug("Mutations: " + numMutations);

			// Delta
			for (int i = 0; i < numMutations; i++) {
				chromosome.mutate();
			}
		}

		return chromosome;	}

}
