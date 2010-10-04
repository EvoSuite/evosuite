/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.unisb.cs.st.evosuite.assertion;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.OutputTrace;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

public class ComparisonTrace extends OutputTrace {

	private Logger logger = Logger.getLogger(ComparisonTrace.class);
	
	Map<Integer, VariableReference> return_values = new HashMap<Integer, VariableReference>();
	Map<Integer, Map<VariableReference, Boolean > > equals_map = new HashMap<Integer, Map<VariableReference, Boolean > >();
	Map<Integer, Map<VariableReference, Integer > > compare_map = new HashMap<Integer, Map<VariableReference, Integer > >();

	public void clear() {
		return_values.clear();
		equals_map.clear();
		compare_map.clear();
	}
	
	public ComparisonTrace clone() {
		ComparisonTrace t = new ComparisonTrace();
		t.return_values.putAll(return_values);
		t.equals_map.putAll(equals_map);
		t.compare_map.putAll(compare_map);
		return t;
	}
	
	@Override
	public boolean differs(OutputTrace other_trace) {
		if(other_trace.getClass() != this.getClass())
			return true;
		
		ComparisonTrace other = (ComparisonTrace) other_trace;
		assert(return_values.size() == equals_map.size());
		assert(return_values.size() == compare_map.size());
		
		//if(return_values.size() != other.return_values.size()) {
		//	return true;
		//}
		
		for(Integer line : return_values.keySet()) {
			if(!other.equals_map.containsKey(line))
				continue;
			if(!other.compare_map.containsKey(line))
				continue;
			
			Map<VariableReference, Boolean> other_map = other.equals_map.get(line);
			for(Entry<VariableReference, Boolean> entry : equals_map.get(line).entrySet()) {
				if(!other_map.containsKey(entry.getKey()))
					continue;
				if(other_map.get(entry.getKey()) == null)
					return (entry.getValue() != null);
				if(!other_map.get(entry.getKey()).equals(entry.getValue())) {
					return true;
				}
			}

			Map<VariableReference, Integer> other_map2 = other.compare_map.get(line);
			for(Entry<VariableReference, Integer> entry : compare_map.get(line).entrySet()) {
				if(!other_map2.containsKey(entry.getKey()))
					continue;
				if(other_map.get(entry.getKey()) == null)
					return (entry.getValue() != null);
				if(!other_map2.get(entry.getKey()).equals(entry.getValue())) {
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public int getAssertions(TestCase test, OutputTrace other_trace) {
		if(other_trace.getClass() != this.getClass())
			return 0;
		
		ComparisonTrace other = (ComparisonTrace) other_trace;		
		int num_assertions = 0;
		
		for(int line=0; line<test.size(); line++) {
			boolean have_assertion = false;
			
			if(compare_map.containsKey(line) && other.compare_map.containsKey(line)) {
				Map<VariableReference, Integer> other_map2 = other.compare_map.get(line);
				for(Entry<VariableReference, Integer> entry : compare_map.get(line).entrySet()) {
					if(!other_map2.containsKey(entry.getKey())) {
						//logger.info("Other map does not contain this result.");
						continue;
					}
					if(!other_map2.get(entry.getKey()).equals(entry.getValue())) {
						logger.debug("Found compare assertion");
						CompareAssertion assertion = new CompareAssertion();
						assertion.source = return_values.get(line);
						assertion.dest   = entry.getKey();
						assertion.value  = entry.getValue();
						if(!other.isDetectedBy(assertion)) {
							logger.error("Invalid comparison assertion generated!");
							logger.error(assertion.getCode());
						} else {
							test.getStatement(line).addAssertion(assertion);
							have_assertion = true;
							num_assertions++;							
						}
					//} else {
					//	logger.info("Values equal");
					}
				}
			//} else {
			//	logger.info("One of the maps doesn't contain the value");
			}
			
			// Only add equals calls if we have no compare calls (else we'll get both)
			if(have_assertion)
				continue;
			
			if(equals_map.containsKey(line) && other.equals_map.containsKey(line)) {
				Map<VariableReference, Boolean> other_map = other.equals_map.get(line);
				for(Entry<VariableReference, Boolean> entry : equals_map.get(line).entrySet()) {
					if(!other_map.containsKey(entry.getKey()))
						continue;
					if(!other_map.get(entry.getKey()).equals(entry.getValue())) {
						logger.debug("Found equals assertion");
						EqualsAssertion assertion = new EqualsAssertion();
						assertion.source = return_values.get(line);
						assertion.dest   = entry.getKey();
						assertion.value  = entry.getValue();
						if(!other.isDetectedBy(assertion)) {
							logger.error("Invalid equals assertion generated!");
							logger.error(assertion.getCode());
						} else {
							test.getStatement(line).addAssertion(assertion);
							num_assertions++;							
						}

					}
				}
			//} else {
			//	logger.info("One of the maps doesn't contain the value");
			}

		}
		return num_assertions;
	}

	@Override
	public int numDiffer(OutputTrace other_trace) {
		ComparisonTrace other = (ComparisonTrace) other_trace;
		
		int num = 0;
		
		for(Entry<Integer, Map<VariableReference, Integer > > entry : compare_map.entrySet()) {
			if(other.compare_map.containsKey(entry.getKey())) {
				if(entry.getValue() == null && other.compare_map.get(entry.getKey()) != null) {
					num++;
				}
				else if(entry.getValue() != null) {
					for(Entry<VariableReference, Integer> centry : entry.getValue().entrySet()) {
						if(!centry.getValue().equals(other.compare_map.get(entry.getKey()).get(centry.getKey())))
							num++;
					}
				}
			}			
		}
		for(Entry<Integer, Map<VariableReference, Boolean > > entry : equals_map.entrySet()) {
			if(other.compare_map.containsKey(entry.getKey())) {
				if(entry.getValue() == null && other.equals_map.get(entry.getKey()) != null) {
					num++;
				}
				else if(entry.getValue() != null) {
					for(Entry<VariableReference, Boolean> centry : entry.getValue().entrySet()) {
						if(!centry.getValue().equals(other.equals_map.get(entry.getKey()).get(centry.getKey())))
							num++;
					}
				}
			}			
		}

		/*
		for(Entry<Integer, Map<VariableReference, Integer > > entry : other.compare_map.entrySet()) {
			if(!compare_map.containsKey(entry.getKey()))
				num++;
		}
		for(Entry<Integer, Map<VariableReference, Boolean > > entry : other.equals_map.entrySet()) {
			if(!compare_map.containsKey(entry.getKey()))
				num++;
		}
		*/
				
		return num;
	}

	@Override
	public boolean isDetectedBy(Assertion assertion) {
		if(assertion instanceof CompareAssertion) {
			CompareAssertion c = (CompareAssertion)assertion;
			if(compare_map.containsKey(c.source.statement)) {
				if(compare_map.get(c.source.statement).containsKey(c.dest)) 
					if(!c.value.equals(compare_map.get(c.source.statement).get(c.dest)))
						return true;
			}
			return false;
		} else if(assertion instanceof EqualsAssertion) {
			EqualsAssertion e = (EqualsAssertion)assertion;
			if(equals_map.containsKey(e.source.statement)) {
				if(equals_map.get(e.source.statement).containsKey(e.dest)) 
					if(!e.value.equals(equals_map.get(e.source.statement).get(e.dest)))
						return true;
			}
			return false;
		} else
			return false;

	}

}
