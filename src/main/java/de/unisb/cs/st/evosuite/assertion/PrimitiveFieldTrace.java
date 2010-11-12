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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.OutputTrace;
import de.unisb.cs.st.evosuite.testcase.TestCase;

public class PrimitiveFieldTrace extends OutputTrace {

	private Logger logger = Logger.getLogger(PrimitiveFieldTrace.class);
	
	public Map<Integer, List<Object> > trace = new HashMap<Integer, List<Object> >();
	public Map<Type, List<Field> > fields = new HashMap<Type, List<Field> >();

	public PrimitiveFieldTrace clone() {
		PrimitiveFieldTrace t = new PrimitiveFieldTrace();
		for(Entry<Integer, List<Object> > entry : trace.entrySet()) {
			List<Object> copy = new ArrayList<Object>();
			copy.addAll(entry.getValue());
			t.trace.put(new Integer(entry.getKey()), copy);
		}
		return t;
	}
	
	@Override
	public boolean differs(OutputTrace other_trace) {
		PrimitiveFieldTrace other = (PrimitiveFieldTrace) other_trace;
		
		//if(trace.size() != other.trace.size()) {
		//	return true;
		//}
		
		for(Entry<Integer, List<Object> > entry : trace.entrySet()) {
			if(!other.trace.containsKey(entry.getKey()))
				continue;
			
			if(entry.getValue().size() != other.trace.get(entry.getKey()).size()) {
				//logger.debug("TG: Traces differ at position "+entry.getKey()+" : "+entry.getValue() + "<->" + other.trace.get(entry.getKey()));
				return true;
			}
			
			for(int i = 0; i<entry.getValue().size(); i++) {
				if(!entry.getValue().get(i).equals(other.trace.get(entry.getKey()).get(i))) {
					//logger.debug("TG: Traces differ at position "+entry.getKey()+" : "+entry.getValue() + "<->" + other.trace.get(entry.getKey()));
					return true;
				}
			}
		}
	
		return false;	
	}

	@Override
	public int getAssertions(TestCase test, OutputTrace other_trace) {
		PrimitiveFieldTrace other = (PrimitiveFieldTrace) other_trace;
		
		int num_assertions = 0;
		
		for(int i=0; i<test.size(); i++) {
			if(trace.containsKey(i) && other.trace.containsKey(i)) {
				List<Object> list1 = trace.get(i);
				List<Object> list2 = other.trace.get(i);
				if(list1.size() != list2.size()) {
					logger.error("Size of lists does not match: ");
					for(Object o1 : list1) {
						logger.error(" -> "+o1);
					}
					logger.error("List2");
					for(Object o1 : list2) {
						logger.error(" -> "+o1);
					}
				} else {
					for(int j = 0; j<list1.size();j++) {
						if(!list1.get(j).equals(list2.get(j))) {
							if(!fields.containsKey(test.getReturnValue(i).getType()))
								logger.error("Have no records of field");
							else {
								logger.debug("Generated primitive field assertion");

								PrimitiveFieldAssertion assertion = new PrimitiveFieldAssertion();
								assertion.source = test.getReturnValue(i);
								assertion.value  = list1.get(j);
								assertion.field  = fields.get(test.getReturnValue(i).getType()).get(j);
								test.getStatement(i).addAssertion(assertion);
								num_assertions++;
								if(!other.isDetectedBy(assertion))
									logger.error("Invalid primitive field assertion generated!");
							}

						}
							
					}
				}
			}
		}
		return num_assertions;
	}

	@Override
	public int numDiffer(OutputTrace other_trace) {
		PrimitiveFieldTrace other = (PrimitiveFieldTrace) other_trace;
		
		int num = 0;
		
		for(Entry<Integer, List<Object> > entry : trace.entrySet()) {
			if(other.trace.containsKey(entry.getKey())) {
				if(entry.getValue() == null && other.trace.get(entry.getKey()) != null) {
					num++;
				}
				else if(entry.getValue() != null) {
					int pos = 0;
					for(Object o : entry.getValue()) {
						if(other.trace.get(entry.getKey()).size() > pos) {
							Object other_o = other.trace.get(entry.getKey()).get(pos);
							if(o == null) {
								if(other_o != null)
									num++;
							} else if(!o.equals(other_o))
								num++;
						}
						pos++;
					}
				}
			}
			
		}

		/*
		for(Entry<Integer, List<Object>> entry : other.trace.entrySet()) {
			if(!trace.containsKey(entry.getKey()))
				num++;
		}
		*/
		
		return num;
	}

	@Override
	public boolean isDetectedBy(Assertion assertion) {
		if(!(assertion instanceof PrimitiveFieldAssertion))
			return false;
		
		PrimitiveFieldAssertion p = (PrimitiveFieldAssertion)assertion;
		if(!p.value.equals(trace.get(p.source.statement)))
			return true;
		else
			return false;
	}

}
