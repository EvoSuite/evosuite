package org.evosuite.testcase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.ga.ChromosomeFactory;
import org.evosuite.testcarver.extraction.CarvingRunListener;
import org.evosuite.utils.Randomness;
import org.evosuite.utils.Utils;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JUnitTestCarvedChromosomeFactory implements
		ChromosomeFactory<TestChromosome> {

	private static final long serialVersionUID = -569338946355072318L;
	
	private static final Logger logger = LoggerFactory.getLogger(JUnitTestCarvedChromosomeFactory.class);

	private List<TestCase> junitTests = new ArrayList<TestCase>();
	
	private final ChromosomeFactory<TestChromosome> defaultFactory;

	/**
	 * The carved test cases are used only with a certain probability P.
	 * So, with probability 1-P the 'default' factory is rather used.
	 * 
	 * @param defaultFactory
	 * @throws IllegalStateException  if Properties are not properly set
	 */
	public JUnitTestCarvedChromosomeFactory(ChromosomeFactory<TestChromosome> defaultFactory) throws IllegalStateException{
		this.defaultFactory = defaultFactory;
		readTestCases();
	}
	
	private void readTestCases() throws IllegalStateException{
		
		JUnitCore runner = new JUnitCore();
		CarvingRunListener listener = new CarvingRunListener();
		runner.addListener(listener);
		
		Collection<String> junitTestNames = getListOfJUnitClassNames();

		List<Class<?>> junitTestClasses = new ArrayList<Class<?>>();
		org.evosuite.testcarver.extraction.CarvingClassLoader classLoader = new org.evosuite.testcarver.extraction.CarvingClassLoader(); 
		for(String className : junitTestNames) {
			
			String classNameWithDots = Utils.getClassNameFromResourcePath(className);
			try {
				Class<?> junitClass = classLoader.loadClass(classNameWithDots);
				junitTestClasses.add(junitClass);
			} catch (ClassNotFoundException e) {
				logger.warn("Error trying to load JUnit test class "+classNameWithDots+": "+e);
			}
		}
		
		Class<?>[] classes = new Class<?>[junitTestClasses.size()];
		junitTestClasses.toArray(classes);
		Result result = runner.run(classes);
		junitTests.addAll(listener.getTestCases());
		
		if(junitTests.size()>0){
			logger.info("Carved "+junitTests.size()+" tests");
		} else {
			String outcome = "";
			for(Failure failure : result.getFailures()){
				outcome += "("+failure.getDescription()+", "+failure.getTrace()+") ";
			}
			logger.warn("It was not possible to carve any test case from: " +
					Arrays.toString(junitTestNames.toArray()) + 
					". Test execution results: "+outcome);
		}
	}

	public boolean hasCarvedTestCases(){
		return junitTests.size() > 0 ;
	}
	
	public int getNumCarvedTestCases() {
		return junitTests.size();
	}
	
	private Collection<String> getListOfJUnitClassNames() throws IllegalStateException{

		String prop = Properties.SELECTED_JUNIT;
		if(prop==null || prop.trim().isEmpty()){
			throw new IllegalStateException("Trying to use a test carver factory, but empty Properties.SELECTED_JUNIT");
		}
		
		String[] paths = prop.split(":");
		Collection<String> junitTestNames = new HashSet<String>();
		for(String s : paths){
			junitTestNames.add(s.trim());
		}
		
		/* 
		Pattern pattern = Pattern.compile(Properties.JUNIT_PREFIX+".*.class");
		Collection<String> junitTestNames = ResourceList.getResources(pattern);		
		logger.info("Found "+junitTestNames.size()+" candidate junit classes for pattern "+pattern);
		*/
		return junitTestNames;
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
