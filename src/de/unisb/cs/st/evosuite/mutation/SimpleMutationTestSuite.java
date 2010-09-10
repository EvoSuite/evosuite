package de.unisb.cs.st.evosuite.mutation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.Assertion;
import de.unisb.cs.st.evosuite.testcase.AssertionGenerator;
import de.unisb.cs.st.evosuite.testcase.AssertionObserver;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.TestCaseExecutor;
import de.unisb.cs.st.evosuite.testcase.TestSuite;
import de.unisb.cs.st.javalanche.HOM.HOMObserver;
import de.unisb.cs.st.javalanche.HOM.HOMSwitcher;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

public class SimpleMutationTestSuite extends TestSuite {

	private Logger logger = Logger.getLogger(SimpleMutationTestSuite.class);
	private HOMSwitcher hom_switcher = new HOMSwitcher();
	private TestCaseExecutor executor = new TestCaseExecutor();
	private AssertionObserver assertion_observer = new AssertionObserver();
	
	Map<Mutation, Integer> mapping = new HashMap<Mutation, Integer>();
	
	public SimpleMutationTestSuite() {
		executor.addObserver(assertion_observer);
	}
	
	public void addTestCase(TestCase t, Mutation m) {
		int num = insertTest(t);
		mapping.put(m, num);
	}
	
	@Override
	protected String getInformation(int num) {
		StringBuilder builder = new StringBuilder();
		for(Entry<Mutation, Integer> entry : mapping.entrySet()) {
			if(entry.getValue().equals(num)) {
				builder.append(entry.getKey().getId());
				builder.append(" ");
			}
		}
		return builder.toString();
	}

	public int addAssertions() {
		Set<Long> killed = new HashSet<Long>();
		AssertionGenerator asserter = new AssertionGenerator();
		for(TestCase test : test_cases) {
			asserter.addAssertions(test, killed);
		}
		return killed.size();
	}
	
	private List<Integer> runTest(TestCase test, Mutation mutant) {
		List<Integer> failed_assertions = new ArrayList<Integer>();
		try {
	        logger.debug("Executing test");
			HOMObserver.resetTouched(); // TODO - is this the right place?
			if(mutant != null) {
				hom_switcher.switchOn(mutant);
				executor.setLogging(false);
			}
			executor.runWithTrace(test);
			executor.setLogging(true);
			hom_switcher.switchOff(mutant);
			
		} catch(Exception e) {
			logger.error("TG: Exception caught: "+e);
			e.printStackTrace();
			System.exit(1);
		}
		return failed_assertions;
	}
	
	private void minimize() {
		Map<Integer, List<Assertion> > assertions = new HashMap<Integer, List<Assertion> >();
		Map<Integer, Map<Integer, Set<Mutation> > > assertion_status = new HashMap<Integer, Map<Integer, Set<Mutation> > >();
		
		for(int i = 0; i<test_cases.size(); i++) {
			assertions.put(i, test_cases.get(i).getAssertions());
			Map<Integer, Set<Mutation> > status_map = new HashMap<Integer, Set<Mutation> >();
			for(int j = 0; j<assertions.get(i).size(); j++) {
				status_map.put(j, new HashSet<Mutation>());
			}
			assertion_status.put(i, status_map);
		}
		// run each test case against each mutant
		int num_test = 0;
		for(TestCase test : test_cases) {
			for(Mutation m : hom_switcher.getMutants()) {
				runTest(test, m);

				// record which assertion kills which mutant
				List<Boolean> status = assertion_observer.getStatus();
				for(int i = 0; i<status.size(); i++) {
					if(status.get(i) == false) {
						assertion_status.get(num_test).get(i).add(m);
					}
				}
			}
			num_test++;
		}
		// TODO
		// calculate subset of test cases such that there is an assertion for every mutant?
	}
	
	@Override
	public List<TestCase> getTestCases() {
		addAssertions();
		return test_cases;
	}
	
	public boolean hasTest(Mutation m) {
		return mapping.containsKey(m);
	}

}
