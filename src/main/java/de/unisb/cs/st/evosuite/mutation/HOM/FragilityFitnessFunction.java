package de.unisb.cs.st.evosuite.mutation.HOM;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.javalanche.mutation.properties.ConfigurationLocator;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;
import de.unisb.cs.st.javalanche.mutation.results.MutationCoverageFile;
import de.unisb.cs.st.javalanche.mutation.results.MutationTestResult;
import de.unisb.cs.st.javalanche.mutation.runtime.testDriver.MutationTestDriver;

public class FragilityFitnessFunction extends HOMFitnessFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected List<Double> fragility = new ArrayList<Double>();

	protected List<Mutation> dead_mutants = new ArrayList<Mutation>();

	public FragilityFitnessFunction(MutationTestDriver driver) {
		super(driver);
		// analyzeFOMs();
	}

	public List<Mutation> analyzeFOMs() {
		dead_mutants.clear();

		List<Mutation> mutants = hom_switcher.getMutants();
		boolean old_value = ConfigurationLocator.getJavalancheConfiguration().stopAfterFirstFail();
		// TODO MutationProperties.STOP_AFTER_FIRST_FAIL = false;
		int num = 0;
		for (Mutation m : mutants) {
			logger.info("Analyzing mutant " + num + "/" + mutants.size());
			num++;
			MutationTestResult result = m.getMutationResult();
			if ((result == null) || (result.getRuns() != tests.size())) {
				logger.info("Mutation hasn't been analyzed - running test cases");
				Set<String> coveredTests = MutationCoverageFile.getCoverageData(m);
				Set<String> testsForThisRun = coveredTests.size() > 0 ? coveredTests : new HashSet<String>(tests);
				hom_switcher.switchOn(m);
				result = test_driver.runTests(testsForThisRun);
				hom_switcher.switchOff();
			}
			logger.info("Executed tests: " + result.getRuns());
			logger.info("Passed tests: " + result.getPassing().size());
			logger.info("Failed tests: " + (result.getNumberOfFailures() + result.getNumberOfErrors()));
			if (result.getRuns() != result.getPassing().size()) {
				logger.info("This mutant is killed!");
				dead_mutants.add(m);
			}
			double f = (result.getNumberOfFailures() + result.getNumberOfErrors()) / (1.0 * result.getRuns());
			fragility.add(f);
			logger.debug("Fragility of FOM " + f);
		}
		// TODO: MutationProperties.STOP_AFTER_FIRST_FAIL = old_value;

		return dead_mutants;
	}

	@Override
	public double getFitness(Chromosome individual) {
		HOMChromosome chromosome = (HOMChromosome) individual;
		MutationTestResult result = runHOM(chromosome);

		logger.info("Number of results: " + fragility.size());
		logger.info("Number of mutants: " + chromosome.size());
		logger.info("Order of mutant: " + chromosome.getNumberOfMutations());
		logger.info("Passed tests: " + result.getPassing().size());
		logger.info("Failed tests: " + (result.getRuns() - result.getPassing().size()));

		double other_fragility = 0.0;
		double own_fragility = (result.getNumberOfErrors() + result.getNumberOfFailures()) / (result.getRuns() * 1.0);

		for (int i = 0; i < chromosome.size(); i++) {
			if (chromosome.get(i)) {
				other_fragility += fragility.get(i);
			}
		}
		double fitness = 0.0;

		if (own_fragility != 0.0) {
			fitness = other_fragility / own_fragility;
		}

		logger.info("Own fragility: " + own_fragility);
		logger.info("Other fragility: " + other_fragility);
		logger.info("Fitness: " + fitness);

		return fitness;
	}

	@Override
	protected void updateIndividual(Chromosome individual, double fitness) {
		individual.setFitness(fitness);

	}

}
