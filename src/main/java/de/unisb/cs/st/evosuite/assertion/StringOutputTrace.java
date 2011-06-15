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

public class StringOutputTrace extends OutputTrace {

	private static Logger logger = Logger.getLogger(StringOutputTrace.class);

	public Map<Integer, String> trace;

	public StringOutputTrace(Map<Integer, String> trace) {
		this.trace = new HashMap<Integer, String>();
		for (Entry<Integer, String> entry : trace.entrySet()) {
			this.trace.put(new Integer(entry.getKey()), new String(entry.getValue()));
		}
		// this.trace = trace;
	}

	@Override
	public boolean differs(OutputTrace other_trace) {
		StringOutputTrace other = (StringOutputTrace) other_trace;
		// if(trace.size() != other.trace.size()) {
		// //logger.debug("TG: Traces have different length "+trace.size()+"/"+other.trace.size());
		// return true;
		// }

		for (Entry<Integer, String> entry : trace.entrySet()) {
			if (!other.trace.containsKey(entry.getKey())) {
				continue;
			}

			if (!entry.getValue().equals(other.trace.get(entry.getKey()))) {
				// logger.debug("TG: Traces differ at position "+entry.getKey()+" : "+entry.getValue()
				// + "<->" + other.trace.get(entry.getKey()));
				return true;
			}

		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.testcase.OutputTrace#getAllAssertions(de.unisb
	 * .cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public int getAllAssertions(TestCase test) {
		int num_assertions = 0;
		for (int i = 0; i < test.size(); i++) {
			if (trace.containsKey(i)) {
				Assertion assertion = new StringAssertion();
				assertion.source = test.getReturnValue(i);
				assertion.value = new String(trace.get(i));
				test.getStatement(i).addAssertion(assertion);
				num_assertions++;
			}
		}
		return num_assertions;
	}

	@Override
	public int getAssertions(TestCase test, OutputTrace other_trace) {
		StringOutputTrace other = (StringOutputTrace) other_trace;
		/*
		 * if(trace.size() != other.trace.size()) {
		 * logger.info("Traces differ in length: "
		 * +trace.size()+"/"+other.trace.size()); } if(trace.size() !=
		 * test.size()) logger.info("Orig trace differs in length to test: "
		 * +trace.size()+"/"+test.size()); if(other.trace.size() != test.size())
		 * logger.info("Other trace differs in length to test: "
		 * +other.trace.size()+"/"+test.size()); logger.info(trace.toString());
		 * logger.info(other.trace.toString()); logger.info(test.toCode());
		 */
		int num_assertions = 0;
		for (int i = 0; i < test.size(); i++) {
			if ((trace.containsKey(i) && other.trace.containsKey(i) && !trace.get(i).equals(other.trace.get(i)))
					|| (trace.containsKey(i) && !other.trace.containsKey(i))) {
				// ||
				logger.info("Generated string assertion");
				// (!trace.containsKey(i) && other.trace.containsKey(i))) {
				Assertion assertion = new StringAssertion();
				assertion.source = test.getReturnValue(i);
				assertion.value = new String(trace.get(i));
				test.getStatement(i).addAssertion(assertion);
				if (!other.isDetectedBy(assertion)) {
					logger.error("Invalid string assertion generated!");
					// else
					// logger.info("Valid string assertion generated");
				}

				num_assertions++;
			}
		}
		/*
		 * for(int i=0; i<trace.size() && i<other.trace.size() && i<test.size();
		 * i++) { if(!trace.get(i).equals(other.trace.get(i))) { Assertion
		 * assertion = new StringAssertion(); assertion.source =
		 * test.getReturnValue(i); assertion.value = trace.get(i);
		 * test.statements.get(i).addAssertion(assertion); } }
		 */
		return num_assertions;
	}

	@Override
	public boolean isDetectedBy(Assertion assertion) {
		if (!(assertion instanceof StringAssertion)) {
			return false;
		}
		StringAssertion p = (StringAssertion) assertion;

		if (!p.value.equals(trace.get(p.source.getStPosition()))) {
			// logger.info("Inequal "+p.value+" to "+trace.get(p.source.statement));
			return true;
		} else {
			// logger.info("Equal "+p.value+" to "+trace.get(p.source.statement));
			return false;
		}
	}

	@Override
	public int numDiffer(OutputTrace other_trace) {
		StringOutputTrace other = (StringOutputTrace) other_trace;
		int num = 0;
		// if(trace.size() != other.trace.size()) {
		// //logger.debug("TG: Traces have different length "+trace.size()+"/"+other.trace.size());
		// num += Math.abs(trace.size() - other.trace.size());
		// }

		for (Entry<Integer, String> entry : trace.entrySet()) {
			if (other.trace.containsKey(entry.getKey())) {
				if (!entry.getValue().equals(other.trace.get(entry.getKey()))) {
					// logger.debug("TG: Traces differ at position "+entry.getKey()+" : "+entry.getValue()
					// + "<->" + other.trace.get(entry.getKey()));
					num++;
				}
			}
		}

		/*
		 * for(Entry<Integer, String> entry : other.trace.entrySet()) {
		 * if(!trace.containsKey(entry.getKey())) num++; }
		 */

		return num;
	}

	@Override
	public String toString() {
		String ret = "";
		for (String line : trace.values()) {
			ret += line + "\n";
		}
		return ret;
	}

}
