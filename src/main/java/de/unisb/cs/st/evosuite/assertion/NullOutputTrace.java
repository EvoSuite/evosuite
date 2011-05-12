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

public class NullOutputTrace extends OutputTrace {

	private static Logger logger = Logger.getLogger(NullOutputTrace.class);

	public Map<Integer, Boolean> trace = new HashMap<Integer, Boolean>();

	@Override
	public NullOutputTrace clone() {
		NullOutputTrace t = new NullOutputTrace();
		t.trace.putAll(trace);
		return t;
	}

	@Override
	public boolean differs(OutputTrace other_trace) {
		NullOutputTrace other = (NullOutputTrace) other_trace;

		for (Entry<Integer, Boolean> entry : trace.entrySet()) {
			if (!other.trace.containsKey(entry.getKey()))
				continue;

			if (!entry.getValue().equals(other.trace.get(entry.getKey()))) {
				return true;
			}

		}

		return false;
	}

	@Override
	public int getAssertions(TestCase test, OutputTrace other_trace) {
		NullOutputTrace other = (NullOutputTrace) other_trace;

		int num_assertions = 0;
		for (int i = 0; i < test.size(); i++) {
			if (trace.containsKey(i) && other.trace.containsKey(i)) {
				if (!trace.get(i).equals(other.trace.get(i))) {
					logger.debug("Found null assertion");

					Assertion assertion = new NullAssertion();
					assertion.source = test.getReturnValue(i);
					assertion.value = trace.get(i);
					test.getStatement(i).addAssertion(assertion);
					if (!other.isDetectedBy(assertion))
						logger.error("Invalid null assertion generated: " + trace.get(i)
						        + "/" + other.trace.get(i));

					num_assertions++;
				}
			}
		}

		return num_assertions;
	}

	@Override
	public boolean isDetectedBy(Assertion assertion) {
		if (!(assertion instanceof NullAssertion))
			return false;

		NullAssertion p = (NullAssertion) assertion;
		if (trace.containsKey(p.source.getStPosition())) {
			return !p.value.equals(trace.get(p.source.getStPosition()));
		}
		return false;
	}

	@Override
	public int numDiffer(OutputTrace other_trace) {
		int num = 0;

		NullOutputTrace other = (NullOutputTrace) other_trace;

		for (Entry<Integer, Boolean> entry : trace.entrySet()) {
			if (!other.trace.containsKey(entry.getKey()))
				continue;

			if (!entry.getValue().equals(other.trace.get(entry.getKey()))) {
				//logger.info("Difference at: "+entry.getKey()+": "+entry.getValue() +" -> "+other.trace.get(entry.getKey()));
				num++;
			}

		}
		return num;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.OutputTrace#getAllAssertions(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public int getAllAssertions(TestCase test) {
		int num_assertions = 0;
		for (int i = 0; i < test.size(); i++) {
			if (trace.containsKey(i)) {
				Assertion assertion = new NullAssertion();
				assertion.source = test.getReturnValue(i);
				assertion.value = trace.get(i);
				test.getStatement(i).addAssertion(assertion);
				num_assertions++;
			}
		}

		return num_assertions;
	}

}
