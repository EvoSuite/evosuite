package de.unisb.cs.st.evosuite.mutation;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseMinimizer;
import de.unisb.cs.st.evosuite.testcase.TestChromosome;
import de.unisb.cs.st.evosuite.testcase.FixedLengthTestChromosomeFactory;
import de.unisb.cs.st.ga.ChromosomeFactory;
import de.unisb.cs.st.ga.GeneticAlgorithm;
import de.unisb.cs.st.ga.OnePlusOneEA;
import de.unisb.cs.st.ga.Randomness;
import de.unisb.cs.st.ga.RankSelection;
import de.unisb.cs.st.ga.SelectionFunction;
import de.unisb.cs.st.ga.StandardGA;
import de.unisb.cs.st.ga.SteadyStateGA;

import de.unisb.cs.st.javalanche.mutation.results.Mutation;

public class TestGAStrategy extends TestGenerationStrategy {

	SimpleMutationTestSuite test_suite = new SimpleMutationTestSuite();
	
	private int getMutationNumber() {
		int num = mutants.size();
		if(num == 0) {
			logger.error("No mutants found!");
			return 0;
		}
		
		String target = System.getProperty("target.mutation");
		if(target != null) {
			try {
				long id = Integer.parseInt(target);
				num = 0;
				for(Mutation m : mutants) {
					if(m.getId() == id) {
						break;
					}
					num++;
				}
			} catch(NumberFormatException e) {
			}
		} 
		
		if(num == mutants.size()) {
			logger.info("Selecting random mutant");
			Randomness randomness = Randomness.getInstance();
			num = randomness.nextInt(mutants.size());
		}
		return num;

	}
	
	/**
	 * Choose the search algorithm
	 * @return
	 */
	protected GeneticAlgorithm getGeneticAlgorithm() {

		GeneticAlgorithm ga = null;
		ChromosomeFactory factory = new MutationTestChromosomeFactory(null);

		String search_algorithm = System.getProperty("GA.algorithm");
		if(search_algorithm.equals("(1+1)EA")) {
			logger.info("Chosen search algorithm: (1+1)EA");
			ga = new OnePlusOneEA(factory);
		} else if(search_algorithm.equals("SteadyStateGA")) {
			logger.info("Chosen search algorithm: SteadyStateGA");
			ga = new SteadyStateGA(factory);
		} else {
			logger.info("Chosen search algorithm: StandardGA");
			ga = new StandardGA(factory);
		}
		
		// TODO: Make this a parameter
		SelectionFunction selection_function = new RankSelection();
		ga.setSelectionFunction(selection_function);

		MutationStatistics statistics = MutationStatistics.getInstance();
		ga.addListener(statistics);
		
		return ga;
	}
	
	/**
	 * Perform monitoring of remaining mutants
	 * @param mutants
	 * @param test
	 */
	protected void checkMutants(List<Mutation> mutants, TestChromosome test) {
		int num = 0;
		while(num < mutants.size()) {
			Mutation m = mutants.get(num);
			
			TestImpactFitness fitness_function = new TestImpactFitness(m);
			fitness_function.getFitness(test);
			
			if(test.hasException()) {
				logger.info("Test raises exception on mutation "+m.getId()); // TODO: Delete mutation that has exceptions?				
			} else if(test.isSolution()) {
				// TODO: Add assertion only if necessary
				
				mutants.remove(num);
			}
			num++;
		}
	}

	/**
	 * Main method - generate all required tests
	 */
	@Override
	public void generateTests() {
		
		GeneticAlgorithm ga = getGeneticAlgorithm();
		
		List<Mutation> target_mutations = new ArrayList<Mutation>();
					
		// How many mutants - which mutants?
		String target = System.getProperty("target.mutation");
		if(target != null) {
			target_mutations.add(mutants.get(getMutationNumber()));	
		} else {
			logger.info("Killing all mutants");
			target_mutations.addAll(mutants);
		}
		
		// Do we recheck after every new test case?
		boolean monitoring = false; // TODO make this a parameter
		
		// Do we generate tests for all mutants or only live mutants?
		boolean force = false;
		
		// TODO: Make this a parameter
		boolean minimize = true;
		
		int num = 0;
		
		// Now do some killing
		while(!target_mutations.isEmpty()) {
			Mutation m = target_mutations.remove(0);
			ga.setChromosomeFactory(new MutationTestChromosomeFactory(m));
			
			logger.info("Generating test case for mutant "+num+"/"+mutants.size()+" (ID: "+m.getId()+")");
			logger.info("Mutation in "+m.getClassName()+"."+m.getMethodName()+":"+m.getLineNumber());
			num++;
			
			// If this is already covered and we're not forcing generation, then skip
			if(m.isKilled() && !force) {
				logger.info("Mutation is already killed");
				continue;
			}
			
			// Create a new fitness function for the current mutation
			TestImpactFitness fitness_function = new TestImpactFitness(m);
			ga.setFitnessFunction(fitness_function);
			
			// Perform search
			ga.generateSolution();
			
			TestChromosome best = (TestChromosome) ga.getBestIndividual();
			logger.info("Finished search for mutant "+m.getId()+" in "+m.getClassName()+"."+m.getMethodName());
			
			AssertionGenerator asserter = new AssertionGenerator();

			//if(best.hasException()) {
			//	logger.info("Best candidate raises exception");				
			//}
			if(best.isSolution()) {
				logger.info("Found solution with fitness "+best.getFitness());
				asserter.addAssertions(best.test, m);
				if(minimize) {
					logger.info("Minimizing result");
					TestCaseMinimizer minimizer = new TestCaseMinimizer(fitness_function);
					best.test.removeAssertions();
					minimizer.minimize(best);
					asserter.addAssertions(best.test, m);
					logger.info("Best individual minimized: ");
					logger.info(best.test.toCode());
				}
				asserter.addAssertions(best.test, m);
//				MutationStatistics.getInstance().searchFinished(best);
				MutationStatistics.getInstance().searchFinished(ga.getPopulation());
				test_suite.addTestCase(best.test, m);
				
			} else {
				logger.info("Found no solution. Best individual has fitness "+best.getFitness());
				//test_suite.addTestCase(best.test, m);
			}
			
			// Check remaining mutants against this test
			if(monitoring) {
				checkMutants(target_mutations, best);
			}
			
		}
		MutationStatistics statistics = MutationStatistics.getInstance();
		statistics.writeReport();
	}

	@Override
	public List<TestCase> getFailedTests() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TestCase> getTests() {
		return test_suite.getTestCases();
	}

	@Override
	public void writeTestSuite(String filename, String directory) {
		if(!test_suite.isEmpty()) {
			//test_suite.addAssertions();
			test_suite.writeTestSuite(filename, directory);
		}
	}

}
