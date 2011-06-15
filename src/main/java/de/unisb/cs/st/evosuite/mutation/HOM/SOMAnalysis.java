package de.unisb.cs.st.evosuite.mutation.HOM;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.javalanche.mutation.results.Mutation;
import de.unisb.cs.st.javalanche.mutation.results.MutationTestResult;
import de.unisb.cs.st.javalanche.mutation.results.TestMessage;
import de.unisb.cs.st.javalanche.mutation.runtime.testDriver.MutationDriverShutdownHook;
import de.unisb.cs.st.javalanche.mutation.runtime.testDriver.MutationTestDriver;

/**
 * Perform exhaustive analysis of second order mutants
 * 
 * @author Gordon Fraser
 * 
 */
public class SOMAnalysis {

	private Thread shutDownThread;

	private HOMSwitcher hom_switcher = new HOMSwitcher();

	private MutationTestDriver test_driver;

	protected Set<String> tests;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SOMAnalysis.class);

	private Map<Mutation, Set<String>> kill_map = new HashMap<Mutation, Set<String>>();

	public SOMAnalysis(MutationTestDriver driver) {
		test_driver = driver;
		List<String> test_list = test_driver.getAllTests();
		tests = new HashSet<String>();
		for (String test : test_list) {
			tests.add(test);
		}

		// TODO: Check whether we have results or have to execute all tests
		// first?
		getInitialSets();
	}

	public void pulldown() {
		// MutationObserver.reportAppliedMutations();
		Runtime.getRuntime().removeShutdownHook(shutDownThread);
	}

	public void runMutations() {
		List<Mutation> mutants = hom_switcher.getMutants();
		int num_mutants = mutants.size();

		for (int m1 = 0; m1 < num_mutants; m1++) {
			for (int m2 = 0; m2 < num_mutants; m2++) {
				if (m1 == m2) {
					continue;
				}

				MutationTestResult result = runHOM(getHOM(m1, m2));

				Set<String> killed = new HashSet<String>();
				for (TestMessage message : result.getErrors()) {
					killed.add(message.getTestCaseName());
				}
				for (TestMessage message : result.getFailures()) {
					killed.add(message.getTestCaseName());
				}

				Set<String> superset = getKillingTests(m1, m2);

				boolean killed_in_superset = superset.containsAll(killed);
				boolean superset_in_killed = killed.containsAll(superset);

				if (killed_in_superset && superset_in_killed) {
					// Equal sets
				} else if (killed_in_superset) {
					// Superset contains some extra tests, so this must be
					// strongly subsuming

				} else if (superset_in_killed) {
					// Killed contains some extra tests, so the HOM is weaker

				} else {
					// Both sets have some extra tests - what does that mean?
					// Not coupled?

				}
			}
		}
	}

	public void setup() {
		shutDownThread = new Thread(new MutationDriverShutdownHook(test_driver));
		// addMutationTestListener(new MutationObserver());
		// addListenersFromProperty();
		Runtime.getRuntime().addShutdownHook(shutDownThread);
	}

	protected MutationTestResult runHOM(HOMChromosome hom) {
		hom_switcher.switchOn(hom);
		MutationTestResult mutationTestResult = test_driver.runTests(tests);
		hom_switcher.switchOff();

		return mutationTestResult;
	}

	private HOMChromosome getHOM(int m1, int m2) {
		HOMChromosome hom = new HOMChromosome(hom_switcher.getMutants());
		hom.set(m1, true);
		hom.set(m2, true);
		return hom;
	}

	private void getInitialSets() {
		List<Mutation> mutants = hom_switcher.getMutants();
		for (Mutation m : mutants) {
			Set<String> killed = new HashSet<String>();
			MutationTestResult result = m.getMutationResult();
			for (TestMessage message : result.getErrors()) {
				killed.add(message.getTestCaseName());
			}
			for (TestMessage message : result.getFailures()) {
				killed.add(message.getTestCaseName());
			}
			kill_map.put(m, killed);
		}
	}

	private Set<String> getKillingTests(int m1, int m2) {
		Set<String> killed = new HashSet<String>();
		Mutation mutation1 = HOMSwitcher.mutants.get(m1);
		killed.addAll(kill_map.get(mutation1));
		Mutation mutation2 = HOMSwitcher.mutants.get(m2);
		killed.addAll(kill_map.get(mutation2));
		return killed;
	}

}
