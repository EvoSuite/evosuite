package de.unisb.cs.st.evosuite.testcase;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class NullOutputTrace extends OutputTrace {

	private static Logger logger = Logger.getLogger(NullOutputTrace.class);

	public Map<Integer, Boolean> trace = new HashMap<Integer, Boolean>();
	
	public NullOutputTrace clone() {
		NullOutputTrace t = new NullOutputTrace();
		t.trace.putAll(trace);
		return t;
	}
	
	@Override
	public boolean differs(OutputTrace other_trace) {
		NullOutputTrace other = (NullOutputTrace)other_trace;
		
		for(Entry<Integer, Boolean> entry : trace.entrySet()) {
			if(!other.trace.containsKey(entry.getKey()))
				continue;
			
			if(!entry.getValue().equals(other.trace.get(entry.getKey()))) {
				return true;
			}
			
		}
	
		return false;
	}

	@Override
	public int getAssertions(TestCase test, OutputTrace other_trace) {
		NullOutputTrace other = (NullOutputTrace)other_trace;
		
		int num_assertions = 0;
		for(int i=0; i<test.size(); i++) {
			if(trace.containsKey(i) && other.trace.containsKey(i)) {
				if(!trace.get(i).equals(other.trace.get(i))) {
					Assertion assertion = new NullAssertion();
					assertion.source = test.getReturnValue(i);
					assertion.value = trace.get(i);
					test.statements.get(i).addAssertion(assertion);
					if(!other.isDetectedBy(assertion))
						logger.error("Invalid null assertion generated (A)!");

					num_assertions++;
				}		
			}
		}
		
		return num_assertions;
	}

	@Override
	public boolean isDetectedBy(Assertion assertion) {
		if(!(assertion instanceof NullAssertion))
			return false;
		
		NullAssertion p = (NullAssertion)assertion;
		if(trace.containsKey(p.source.statement)) {
			if(((Boolean)p.value).booleanValue()) 
				return (trace.get(p.source.statement) == null);
			else {
				return (trace.get(p.source.statement) != null);
			}
		}
		return false;
	}

	@Override
	public int numDiffer(OutputTrace other_trace) {
		int num = 0;
		
		NullOutputTrace other = (NullOutputTrace)other_trace;
		
		for(Entry<Integer, Boolean> entry : trace.entrySet()) {
			if(!other.trace.containsKey(entry.getKey()))
				continue;
			
			if(!entry.getValue().equals(other.trace.get(entry.getKey()))) {
				num++;
			}
			
		}
		return num;
	}

}
