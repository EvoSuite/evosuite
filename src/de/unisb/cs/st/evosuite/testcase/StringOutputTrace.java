package de.unisb.cs.st.evosuite.testcase;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

public class StringOutputTrace extends OutputTrace {

	private static Logger logger = Logger.getLogger(StringOutputTrace.class);

	public Map<Integer, String> trace;
	
	public StringOutputTrace(Map<Integer, String> trace) {
		this.trace = new HashMap<Integer, String>();
		for(Entry<Integer, String> entry : trace.entrySet()) {
			this.trace.put(new Integer(entry.getKey()), new String(entry.getValue()));
		}
		//this.trace = trace;
	}
	
	@Override
	public int numDiffer(OutputTrace other_trace) {
		StringOutputTrace other = (StringOutputTrace)other_trace;
		int num = 0;
		//if(trace.size() != other.trace.size()) {
		//	//logger.debug("TG: Traces have different length "+trace.size()+"/"+other.trace.size());
		//	num += Math.abs(trace.size() - other.trace.size());
		//}
		
		for(Entry<Integer, String> entry : trace.entrySet()) {
			if(other.trace.containsKey(entry.getKey())) {
				if(!entry.getValue().equals(other.trace.get(entry.getKey()))) {
				//logger.debug("TG: Traces differ at position "+entry.getKey()+" : "+entry.getValue() + "<->" + other.trace.get(entry.getKey()));
					num++;
				}
			}			
		}

		/*
		for(Entry<Integer, String> entry : other.trace.entrySet()) {
			if(!trace.containsKey(entry.getKey()))
				num++;
		}
		*/
		
		return num;
	}

	
	public boolean differs(OutputTrace other_trace) {
		StringOutputTrace other = (StringOutputTrace)other_trace;
		//if(trace.size() != other.trace.size()) {
			////logger.debug("TG: Traces have different length "+trace.size()+"/"+other.trace.size());
			//return true;
		//}
		
		for(Entry<Integer, String> entry : trace.entrySet()) {
			if(!other.trace.containsKey(entry.getKey()))
				continue;
			
			if(!entry.getValue().equals(other.trace.get(entry.getKey()))) {
				//logger.debug("TG: Traces differ at position "+entry.getKey()+" : "+entry.getValue() + "<->" + other.trace.get(entry.getKey()));
				return true;
			}
			
		}
	
		return false;
	}
	
	public int getAssertions(TestCase test, OutputTrace other_trace) {
		StringOutputTrace other = (StringOutputTrace)other_trace;
/*
		if(trace.size() != other.trace.size()) {
			logger.info("Traces differ in length: " +trace.size()+"/"+other.trace.size());
		}
		if(trace.size() != test.size())
			logger.info("Orig trace differs in length to test: " +trace.size()+"/"+test.size());
		if(other.trace.size() != test.size())
			logger.info("Other trace differs in length to test: " +other.trace.size()+"/"+test.size());
		logger.info(trace.toString());
		logger.info(other.trace.toString());
		logger.info(test.toCode());
		*/
		int num_assertions = 0;
		for(int i=0; i<test.size(); i++) {
			if((trace.containsKey(i) && other.trace.containsKey(i) && !trace.get(i).equals(other.trace.get(i))) ||
			   (trace.containsKey(i) && !other.trace.containsKey(i))) {
				//||
			
			//   (!trace.containsKey(i) && other.trace.containsKey(i))) {
					Assertion assertion = new StringAssertion();
					assertion.source = test.getReturnValue(i);
					assertion.value = new String(trace.get(i));
					test.statements.get(i).addAssertion(assertion);
					if(!other.isDetectedBy(assertion))
						logger.error("Invalid string assertion generated!");
					//else
					//	logger.info("Valid string assertion generated");

					num_assertions++;
			}
		}
		/*
		for(int i=0; i<trace.size() && i<other.trace.size() && i<test.size(); i++) {
			if(!trace.get(i).equals(other.trace.get(i))) {
				Assertion assertion = new StringAssertion();
				assertion.source = test.getReturnValue(i);
				assertion.value = trace.get(i);
				test.statements.get(i).addAssertion(assertion);
			}
		}	
		*/	
		return num_assertions;
	}
	
	public String toString() {
		String ret = new String("");
		for(String line : trace.values()) {
			ret += line + "\n";
		}
		return ret;
	}

	@Override
	public boolean isDetectedBy(Assertion assertion) {
		if(!(assertion instanceof StringAssertion))
			return false;
		StringAssertion p = (StringAssertion)assertion;
		
		if(!p.value.equals(trace.get(p.source.statement))) {
			//logger.info("Inequal "+p.value+" to "+trace.get(p.source.statement));
			return true;
		}
		else {
			//logger.info("Equal "+p.value+" to "+trace.get(p.source.statement));
			return false;
		}
	}


}
