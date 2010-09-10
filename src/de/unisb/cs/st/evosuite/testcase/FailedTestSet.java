package de.unisb.cs.st.evosuite.testcase;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class FailedTestSet extends TestSuite {

	private Logger logger = Logger.getLogger(FailedTestSet.class);
	
	//List<TestCase> test_cases = new ArrayList<TestCase>();
	List<String> messages = new ArrayList<String>();
	
	private static FailedTestSet instance = null;
	
	private FailedTestSet() {
		
	}
	
	public static FailedTestSet getInstance() {
		if(instance == null)
			instance = new FailedTestSet();
		
		return instance;
	}
	
	public void addTest(TestCase test, String reason) {
		// TODO: Chop test case at exception statement
		logger.info("Adding failed test to: "+test_cases.size());
		for(int i = 0; i<test_cases.size(); i++) {
			if(test.size() < test_cases.get(i).size()) {
				if(test.isPrefix(test_cases.get(i))) {
					// It's shorter than an existing one
					test_cases.set(i, test);
					messages.set(i, reason);
					return;
				}
				
			} else {
				// Already have that one...
				if(test_cases.get(i).isPrefix(test))
					return;
			}
		}
		test_cases.add(test);
		messages.add(reason);
	}
	
	
	public int size() {
		return test_cases.size();
	}

	@Override
	protected String getInformation(int num) {
		return messages.get(num);
	}

	@Override
	public List<TestCase> getTestCases() {
		return test_cases;
	}
}
