/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.assertion;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.OutputTrace;
import de.unisb.cs.st.evosuite.testcase.TestCase;

public class PrimitiveOutputTrace extends OutputTrace {

	private static Logger logger = Logger.getLogger(PrimitiveOutputTrace.class);

	public Map<Integer, Object> trace = new HashMap<Integer, Object>();

	@Override
	public PrimitiveOutputTrace clone() {
		PrimitiveOutputTrace t = new PrimitiveOutputTrace();
		t.trace.putAll(trace);
		return t;
	}

	@Override
	public boolean differs(OutputTrace other_trace) {
		PrimitiveOutputTrace other = (PrimitiveOutputTrace) other_trace;
		//if(trace.size() != other.trace.size()) {
		//	return true;
		//}

		for (Entry<Integer, Object> entry : trace.entrySet()) {
			if (!other.trace.containsKey(entry.getKey()))
				continue;

			if (entry.getValue() == null) {
				if (other.trace.get(entry.getKey()) != null)
					return true;
			}

			if (!entry.getValue().equals(other.trace.get(entry.getKey()))) {
				//logger.debug("TG: Traces differ at position "+entry.getKey()+" : "+entry.getValue() + "<->" + other.trace.get(entry.getKey()));
				return true;
			}

		}

		return false;

	}

	@Override
	public int getAssertions(TestCase test, OutputTrace other_trace) {
		PrimitiveOutputTrace other = (PrimitiveOutputTrace) other_trace;

		int num_assertions = 0;
		for (int i = 0; i < test.size(); i++) {
			/*
			if((trace.containsKey(i) != other.trace.containsKey(i))) {
				//logger.info("Found primitive assertion");
				Assertion assertion = new PrimitiveAssertion();
				assertion.source = test.getReturnValue(i);
				assertion.value = trace.get(i);
				test.statements.get(i).addAssertion(assertion);
			}
			else 
			*/

			if ((trace.containsKey(i) && other.trace.containsKey(i)
			        && trace.get(i) == null && other.trace.get(i) != null)) {
				logger.debug("Found primitive assertion: null vs " + other.trace.get(i));
				Assertion assertion = new PrimitiveAssertion();
				assertion.source = test.getReturnValue(i);
				assertion.value = trace.get(i);
				test.getStatement(i).addAssertion(assertion);
				if (!other.isDetectedBy(assertion))
					logger.error("Invalid primitive assertion generated (A)!");

				num_assertions++;
			} else if ((trace.containsKey(i) && other.trace.containsKey(i)
			        && trace.get(i) != null && other.trace.get(i) == null)) {
				logger.debug("Found primitive assertion: " + trace.get(i)
				        + " vs null (2)");
				Assertion assertion = new PrimitiveAssertion();
				assertion.source = test.getReturnValue(i);
				assertion.value = trace.get(i);
				test.getStatement(i).addAssertion(assertion);
				if (!other.isDetectedBy(assertion))
					logger.error("Invalid primitive assertion generated (B)!");

				num_assertions++;
			} else if ((trace.containsKey(i) && other.trace.containsKey(i)
			        && trace.get(i) == null && other.trace.get(i) == null)) {
				//logger.info("Null in both traces");
				continue;
			} else if ((trace.containsKey(i) && other.trace.containsKey(i) && !trace.get(i).equals(other.trace.get(i)))) {
				logger.debug("Found primitive assertion (non-null): " + trace.get(i)
				        + " / " + other.trace.get(i));
				Assertion assertion = new PrimitiveAssertion();
				assertion.source = test.getReturnValue(i);
				if (assertion.source.getStPosition() != i) {
					logger.error("Statement id does not match!");
				}
				assertion.value = trace.get(i);
				test.getStatement(i).addAssertion(assertion);
				num_assertions++;
				if (!other.isDetectedBy(assertion)) {
					logger.error("Invalid primitive assertion generated (C)!");
					logger.error("Assertion.value: " + assertion.value);
					logger.error("other.trace.value: "
					        + other.trace.get(assertion.source.getStPosition()));
				}

				//} else {
				//	logger.info("No value at statement "+i);
			}
			/*
								if((trace.containsKey(i) != other.trace.containsKey(i)) ||
										   (trace.containsKey(i) && other.trace.containsKey(i) && trace.get(i) == null && other.trace.get(i) != null) ||
										   (trace.containsKey(i) && other.trace.containsKey(i) && !trace.get(i).equals(other.trace.get(i)))) {
												Assertion assertion = new PrimitiveAssertion();
												assertion.source = test.getReturnValue(i);
												assertion.value = trace.get(i);
												test.statements.get(i).addAssertion(assertion);
										}
										*/
		}
		return num_assertions;
	}

	@Override
	public int numDiffer(OutputTrace other_trace) {
		int num = 0;
		PrimitiveOutputTrace other = (PrimitiveOutputTrace) other_trace;

		for (Entry<Integer, Object> entry : trace.entrySet()) {
			if (other.trace.containsKey(entry.getKey())) {
				if (entry.getValue() == null && other.trace.get(entry.getKey()) != null) {
					num++;
				} else if (entry.getValue() != null
				        && !entry.getValue().equals(other.trace.get(entry.getKey()))) {
					//logger.debug("TG: Traces differ at position "+entry.getKey()+" : "+entry.getValue() + "<->" + other.trace.get(entry.getKey()));
					num++;
				}
			}

		}

		/*
		for(Entry<Integer, Object> entry : other.trace.entrySet()) {
			if(!trace.containsKey(entry.getKey()))
				num++;
		}
		*/

		return num;
	}

	@Override
	public boolean isDetectedBy(Assertion assertion) {
		if (!(assertion instanceof PrimitiveAssertion))
			return false;

		PrimitiveAssertion p = (PrimitiveAssertion) assertion;
		if (trace.containsKey(p.source.getStPosition())) {
			if (p.value == null)
				return (trace.get(p.source.getStPosition()) != null);
			else {
				return !p.value.equals(trace.get(p.source.getStPosition()));
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.OutputTrace#getAllAssertions(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public int getAllAssertions(TestCase test) {
		int num_assertions = 0;
		for (int i = 0; i < test.size(); i++) {
			if (trace.containsKey(i) && trace.get(i) != null) {
				Assertion assertion = new PrimitiveAssertion();
				assertion.source = test.getReturnValue(i);
				assertion.value = trace.get(i);
				test.getStatement(i).addAssertion(assertion);

				num_assertions++;
			}
		}
		return num_assertions;
	}

}
