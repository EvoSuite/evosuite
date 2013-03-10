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
		Pattern pattern = Pattern.compile(Properties.JUNIT_PREFIX+".*.class");
		Collection<String> junitTestNames = ResourceList.getResources(pattern);
		logger.info("Found "+junitTestNames.size()+" candidate junit classes for pattern "+pattern);

		List<Class<?>> junitTestClasses = new ArrayList<Class<?>>();
		org.evosuite.testcarver.extraction.CarvingClassLoader classLoader = new org.evosuite.testcarver.extraction.CarvingClassLoader(); 
		for(String className : junitTestNames) {
			
			String classNameWithDots = className.replace(".class", "").replace('/', '.');
			try {
				Class<?> junitClass = classLoader.loadClass(classNameWithDots);
				junitTestClasses.add(junitClass);
			} catch (ClassNotFoundException e) {
				logger.warn("Error trying to load JUnit test class "+classNameWithDots+": "+e);
			}
		}
		
		Class<?>[] classes = new Class<?>[junitTestClasses.size()];
		junitTestClasses.toArray(classes);
		runner.run(classes);
		junitTests.addAll(listener.getTestCases());
		logger.info("Carved "+junitTests.size()+" tests");
	}
	
	@Override
	public TestChromosome getChromosome() {
		final int N_mutations = Properties.SEED_MUTATIONS;
		final double P_clone = Properties.SEED_CLONE;

		double r = Randomness.nextDouble(); 
		
		if (r >= P_clone || junitTests.isEmpty()) {
			logger.debug("Using random test");
			return defaultFactory.getChromosome();
		}

		// Cloning
		logger.info("Cloning user test");
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
