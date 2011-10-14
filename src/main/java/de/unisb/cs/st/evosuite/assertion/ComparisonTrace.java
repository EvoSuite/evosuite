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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.testcase.OutputTrace;
import de.unisb.cs.st.evosuite.testcase.TestCase;

public class ComparisonTrace extends OutputTrace {

	private final static Logger logger = LoggerFactory.getLogger(ComparisonTrace.class);

	Map<Integer, Map<Integer, Boolean>> equals_map = new HashMap<Integer, Map<Integer, Boolean>>();
	Map<Integer, Map<Integer, Integer>> compare_map = new HashMap<Integer, Map<Integer, Integer>>();

	public void clear() {
		equals_map.clear();
		compare_map.clear();
	}

	@Override
	public ComparisonTrace clone() {
		ComparisonTrace t = new ComparisonTrace();
		t.equals_map.putAll(equals_map);
		t.compare_map.putAll(compare_map);
		return t;
	}

	@Override
	public boolean differs(OutputTrace other_trace) {
		if (other_trace.getClass() != this.getClass())
			return true;

		ComparisonTrace other = (ComparisonTrace) other_trace;

		//if(return_values.size() != other.return_values.size()) {
		//	return true;
		//}

		for (Integer line : equals_map.keySet()) {
			if (!other.equals_map.containsKey(line))
				continue;
			if (!other.compare_map.containsKey(line))
				continue;

			Map<Integer, Boolean> other_map = other.equals_map.get(line);
			for (Entry<Integer, Boolean> entry : equals_map.get(line).entrySet()) {
				if (!other_map.containsKey(entry.getKey()))
					continue;
				if (other_map.get(entry.getKey()) == null)
					if (entry.getValue() != null)
						return true;
				if (!other_map.get(entry.getKey()).equals(entry.getValue())) {
					return true;
				}
			}

			Map<Integer, Integer> other_map2 = other.compare_map.get(line);
			for (Entry<Integer, Integer> entry : compare_map.get(line).entrySet()) {
				if (!other_map2.containsKey(entry.getKey()))
					continue;
				if (other_map.get(entry.getKey()) == null)
					if (entry.getValue() != null)
						return true;
				if (!other_map2.get(entry.getKey()).equals(entry.getValue())) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public int getAssertions(TestCase test, OutputTrace other_trace) {
		if (other_trace.getClass() != this.getClass())
			return 0;

		ComparisonTrace other = (ComparisonTrace) other_trace;
		int num_assertions = 0;

		for (int line = 0; line < test.size(); line++) {
			boolean have_assertion = false;

			if (compare_map.containsKey(line) && other.compare_map.containsKey(line)) {
				logger.debug("Checking comparisons");
				Map<Integer, Integer> other_map2 = other.compare_map.get(line);
				for (Entry<Integer, Integer> entry : compare_map.get(line).entrySet()) {
					if (!other_map2.containsKey(entry.getKey())) {
						logger.info("Other map does not contain this result.");
						continue;
					}
					if (!other_map2.get(entry.getKey()).equals(entry.getValue())) {
						logger.debug("Found compare assertion");
						CompareAssertion assertion = new CompareAssertion();
						assertion.source = test.getReturnValue(line);
						assertion.dest = test.getReturnValue(entry.getKey());
						assertion.value = entry.getValue();
						if (!other.isDetectedBy(assertion)) {
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
			if (have_assertion)
				continue;

			if (equals_map.containsKey(line) && other.equals_map.containsKey(line)) {
				Map<Integer, Boolean> other_map = other.equals_map.get(line);
				for (Entry<Integer, Boolean> entry : equals_map.get(line).entrySet()) {
					if (!other_map.containsKey(entry.getKey()))
						continue;
					if (!other_map.get(entry.getKey()).equals(entry.getValue())) {
						logger.debug("Found equals assertion");
						EqualsAssertion assertion = new EqualsAssertion();
						assertion.source = test.getReturnValue(line);
						assertion.dest = test.getReturnValue(entry.getKey());
						assertion.value = entry.getValue();
						if (!other.isDetectedBy(assertion)) {
							logger.error("Invalid equals assertion generated at position "
							        + line);
							logger.error(assertion.getCode());
							logger.error(test.toCode());
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

		logger.debug("numDiffer: " + equals_map.entrySet().size());
		for (Entry<Integer, Map<Integer, Integer>> entry : compare_map.entrySet()) {
			if (other.compare_map.containsKey(entry.getKey())) {
				logger.debug("Checking comparisons");

				//if(entry.getValue() == null && other.compare_map.get(entry.getKey()) != null) {
				//	num++;
				//}
				//else 
				if (entry.getValue() != null) {
					for (Entry<Integer, Integer> centry : entry.getValue().entrySet()) {
						if (other.compare_map.get(entry.getKey()).containsKey(centry.getKey())
						        && !centry.getValue().equals(other.compare_map.get(entry.getKey()).get(centry.getKey()))) {
							logger.debug("Retval of " + entry.getKey()
							        + " comparing with " + centry.getKey());
							logger.debug("CompareTo: "
							        + centry.getValue()
							        + "/"
							        + other.compare_map.get(entry.getKey()).get(centry.getKey()));
							num++;
						} else {
							logger.debug("Other trace does not contain key");
						}
					}
				}
				//} else {
				//	logger.info("Other map does not contain key " + entry.getKey());
			}
		}
		for (Entry<Integer, Map<Integer, Boolean>> entry : equals_map.entrySet()) {
			if (other.equals_map.containsKey(entry.getKey())) {
				//if(entry.getValue() == null && other.equals_map.get(entry.getKey()) != null) {
				//	num++;
				//}
				//else 
				if (entry.getValue() != null) {
					for (Entry<Integer, Boolean> centry : entry.getValue().entrySet()) {
						if (other.equals_map.get(entry.getKey()).containsKey(centry.getKey())
						        && !centry.getValue().equals(other.equals_map.get(entry.getKey()).get(centry.getKey()))) {
							logger.debug("Equals: "
							        + centry.getValue()
							        + "/"
							        + other.equals_map.get(entry.getKey()).get(centry.getKey()));
							num++;
						} else {
							if (!other.equals_map.get(entry.getKey()).containsKey(centry.getKey())) {
								logger.debug("Other trace does not contain key "
								        + centry.getKey());
							}
						}
					}
				}
			} else {
				//logger.info("Other map does not contain key " + entry.getKey());
				//logger.info(equals_map.keySet().toString());
				//logger.info(other.equals_map.keySet().toString());
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
		if (assertion instanceof CompareAssertion) {
			CompareAssertion c = (CompareAssertion) assertion;
			if (compare_map.containsKey(c.source.getStPosition())) {
				if (compare_map.get(c.source.getStPosition()).containsKey(c.dest.getStPosition()))
					if (!c.value.equals(compare_map.get(c.source.getStPosition()).get(c.dest.getStPosition())))
						return true;
			}
			return false;
		} else if (assertion instanceof EqualsAssertion) {
			EqualsAssertion e = (EqualsAssertion) assertion;
			if (equals_map.containsKey(e.source.getStPosition())) {
				if (equals_map.get(e.source.getStPosition()).containsKey(e.dest.getStPosition())) {
					if (!e.value.equals(equals_map.get(e.source.getStPosition()).get(e.dest.getStPosition())))
						return true;
				}
			}
			return false;
		} else
			return false;

	}

	@Override
	public String toString() {
		return "Comparison trace of length " + equals_map.size();
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.OutputTrace#getAllAssertions(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public int getAllAssertions(TestCase test) {
		int num_assertions = 0;

		for (int line = 0; line < test.size(); line++) {

			if (compare_map.containsKey(line)) {
				for (Entry<Integer, Integer> entry : compare_map.get(line).entrySet()) {
					logger.debug("Found compare assertion");
					CompareAssertion assertion = new CompareAssertion();
					assertion.source = test.getReturnValue(line);
					assertion.dest = test.getReturnValue(entry.getKey());
					assertion.value = entry.getValue();
					test.getStatement(line).addAssertion(assertion);
					num_assertions++;
				}
			}

			if (equals_map.containsKey(line)) {
				for (Entry<Integer, Boolean> entry : equals_map.get(line).entrySet()) {
					logger.debug("Found equals assertion");
					EqualsAssertion assertion = new EqualsAssertion();
					assertion.source = test.getReturnValue(line);
					assertion.dest = test.getReturnValue(entry.getKey());
					assertion.value = entry.getValue();
					test.getStatement(line).addAssertion(assertion);
					num_assertions++;
				}
			}
		}
		return num_assertions;

	}

}
