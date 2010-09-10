package de.unisb.cs.st.evosuite.mutation.HOM;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.unisb.cs.st.ga.FitnessFunction;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;
import de.unisb.cs.st.javalanche.mutation.results.MutationCoverageFile;
import de.unisb.cs.st.javalanche.mutation.results.MutationTestResult;
import de.unisb.cs.st.javalanche.mutation.runtime.testDriver.MutationTestDriver;

public abstract class HOMFitnessFunction extends FitnessFunction {

	protected HOMSwitcher hom_switcher = new HOMSwitcher();

	protected MutationTestDriver test_driver;
	
	protected Set<String> tests;
	
	public HOMFitnessFunction(MutationTestDriver driver) {
		this.test_driver = driver;
		List<String> test_list = test_driver.getAllTests();
		tests = new HashSet<String>();
		for(String test : test_list) {
			tests.add(test);
		}
		logger.info("Number of tests  : "+tests.size());
		logger.info("Number of mutants: "+hom_switcher.getNumMutants());

	}
	
	protected MutationTestResult runHOM(HOMChromosome hom) {
		Set<String> coveredTests = new HashSet<String>();
		for(Mutation m : hom.getActiveMutants()) {
			coveredTests.addAll(MutationCoverageFile.getCoverageDataId(m.getId()));			
		}
				
		Set<String> testsForThisRun = coveredTests.size() > 0 ? coveredTests : new HashSet<String>(tests);
		logger.info("Checking HOM against "+testsForThisRun.size()+" tests");
		hom_switcher.switchOn(hom);
		MutationTestResult mutationTestResult = test_driver.runTests(testsForThisRun);
		hom_switcher.switchOff();
		
		return mutationTestResult;
	}

}
