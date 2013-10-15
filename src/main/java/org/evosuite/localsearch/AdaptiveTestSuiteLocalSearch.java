package org.evosuite.localsearch;

import java.util.ArrayList;
import java.util.List;

import org.evosuite.Properties;
import org.evosuite.Properties.DSEType;
import org.evosuite.testcase.StatementInterface;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestMutationHistoryEntry;
import org.evosuite.testcase.VariableReference;
import org.evosuite.testsuite.TestCaseExpander;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.TestSuiteFitnessFunction;

public class AdaptiveTestSuiteLocalSearch extends TestSuiteLocalSearch {

	private boolean applyDSE(TestSuiteChromosome individual, LocalSearchObjective<TestSuiteChromosome> objective) {
		boolean hasRelevantTests = false;
		for(TestChromosome test : individual.getTestChromosomes()) {
			if(test.hasRelevantMutations()) {
				hasRelevantTests = true;
				break;
			}
		}
		if(!hasRelevantTests) 
			return false;
		
		// DSEBudget.DSEStarted();
		TestSuiteDSE dse = new TestSuiteDSE();
		// TestSuiteDSE will report attempt to LocalSearchBudget
		return dse.applyDSE(individual, (TestSuiteFitnessFunction) objective.getFitnessFunction());
//				
//				if(success) {
//					Properties.DSE_ADAPTIVE_PROBABILITY *= Properties.DSE_ADAPTIVE_RATE;
//					Properties.DSE_ADAPTIVE_PROBABILITY = Math.min(Properties.DSE_ADAPTIVE_PROBABILITY, 1.0);
//				} else {
//					Properties.DSE_ADAPTIVE_PROBABILITY /= Properties.DSE_ADAPTIVE_RATE;
//					Properties.DSE_ADAPTIVE_PROBABILITY = Math.max(Properties.DSE_ADAPTIVE_PROBABILITY, 0.0);
//				}

			//}
	}
	
	private List<TestChromosome> getCandidateTests(TestSuiteChromosome individual) {
		List<TestChromosome> candidates = new ArrayList<TestChromosome>();
		for(TestChromosome test : individual.getTestChromosomes()) {
			logger.info("Checking test with history entries: "+test.getMutationHistory().size()+": "+test.getMutationHistory());
			if(test.hasRelevantMutations()) {
				TestCaseExpander expander = new TestCaseExpander();
				TestChromosome clone = new TestChromosome();
				clone.setTestCase(expander.expandTestCase(test.getTestCase()));
				for (TestMutationHistoryEntry mutation : test.getMutationHistory()) {
					if(mutation.getMutationType() == TestMutationHistoryEntry.TestMutation.DELETION) {
						clone.getMutationHistory().addMutationEntry(mutation.clone(clone.getTestCase()));
					} else {
						StatementInterface s1 = mutation.getStatement();
						if(expander.variableMapping.containsKey(s1.getPosition())) {
							for(VariableReference var : expander.variableMapping.get(s1.getPosition())) {
								clone.getMutationHistory().addMutationEntry(new TestMutationHistoryEntry(mutation.getMutationType(), clone.getTestCase().getStatement(var.getStPosition())));
							}
						} else {
							clone.getMutationHistory().addMutationEntry(new TestMutationHistoryEntry(mutation.getMutationType(), clone.getTestCase().getStatement(s1.getPosition())));
						}
					}
				}
				logger.info("Mutation history before expansion: "+test.getMutationHistory().size()+", after: "+clone.getMutationHistory().size());
				candidates.add(clone);
			}
		}
		return candidates;
	}
	
	private boolean applyLocalSearchToTest(TestChromosome clone, TestSuiteChromosome individual, LocalSearchObjective<TestSuiteChromosome> objective) {
		 individual.addTest(clone);
		 TestSuiteLocalSearchObjective testObjective = new TestSuiteLocalSearchObjective((TestSuiteFitnessFunction) objective.getFitnessFunction(), individual, individual.size() - 1);
		 logger.info("Applying DSE to test: " + clone.getTestCase().toCode());
		 AdaptiveTestCaseLocalSearch localSearch = new AdaptiveTestCaseLocalSearch();
		 boolean result = localSearch.doSearch(clone, testObjective);
		 LocalSearchBudget.getInstance().countLocalSearchOnTestSuite();
		 
		 return result;
	}
	
	@Override
	public boolean doSearch(TestSuiteChromosome individual,
			LocalSearchObjective<TestSuiteChromosome> objective) {

		if (!individual.hasFitnessChanged()) {
			logger.info("Fitness has not changed, so not applying local search");
			return false;
		}
		
		logger.info("Fitness has changed, applying local search with fitness "
		        + individual.getFitness());

		if(Properties.LOCAL_SEARCH_DSE == DSEType.SUITE) {
			// Apply standard DSE on entire suite if it has relevant mutations
			return applyDSE(individual, objective);
		} else {
			// Determine tests that were changed
			List<TestChromosome> candidates = getCandidateTests(individual);
			boolean result = false;
			
			// Apply local search on individual tests
			for(TestChromosome clone : candidates) {
				if(applyLocalSearchToTest(clone, individual, objective))
					result = true;
			}
			return result;
		}
	}

}
