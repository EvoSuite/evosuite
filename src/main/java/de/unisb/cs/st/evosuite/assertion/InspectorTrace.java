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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.OutputTrace;
import de.unisb.cs.st.evosuite.testcase.TestCase;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

public class InspectorTrace extends OutputTrace {

	private final Logger logger = Logger.getLogger(InspectorTrace.class);

	private final InspectorManager manager = InspectorManager.getInstance();

	// TODO: Add inspectors on public fields?
	Map<Integer, VariableReference> return_values = new HashMap<Integer, VariableReference>();
	Map<Integer, List<Object>> inspector_results = new HashMap<Integer, List<Object>>();

	public Map<Integer, Map<Inspector, Object>> calleeMap = new HashMap<Integer, Map<Inspector, Object>>();

	public void clear() {
		return_values.clear();
		inspector_results.clear();
		calleeMap.clear();
	}

	@Override
	public InspectorTrace clone() {
		InspectorTrace trace = new InspectorTrace();

		Set<Entry<Integer, List<Object>>> set1 = inspector_results.entrySet();
		for (Entry<Integer, List<Object>> e : set1)
			trace.inspector_results.put(e.getKey(), new ArrayList<Object>(e.getValue()));

		for (Entry<Integer, VariableReference> e : return_values.entrySet())
			trace.return_values.put(e.getKey(), e.getValue().clone());
		trace.calleeMap.putAll(calleeMap);

		//trace.return_values.putAll(return_values);
		//trace.inspector_results.putAll(inspector_results);
		return trace;
	}

	@Override
	public boolean differs(OutputTrace other_trace) {
		if (other_trace.getClass() != this.getClass())
			return true;

		InspectorTrace other = (InspectorTrace) other_trace;

		//if(return_values.size() != other.return_values.size())
		//	return true;

		for (Integer line : return_values.keySet()) {
			List<Object> own_results = inspector_results.get(line);
			if (!other.inspector_results.containsKey(line))
				continue;
			List<Object> other_results = other.inspector_results.get(line);
			for (int i = 0; i < Math.min(other_results.size(), own_results.size()); i++) {
				if (own_results.get(i) == null)
					return (other_results.get(i) != null);
				if (!own_results.get(i).equals(other_results.get(i))) {
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

		InspectorTrace other = (InspectorTrace) other_trace;
		int num_assertions = 0;
		for (int line = 0; line < test.size(); line++) {
			if (inspector_results.containsKey(line)
			        && other.inspector_results.containsKey(line)) {

				List<Object> own_results = inspector_results.get(line);
				List<Object> other_results = other.inspector_results.get(line);
				for (int i = 0; i < other_results.size() && i < own_results.size(); i++) {
					if ((own_results.get(i) == null && other_results.get(i) != null)
					        || (own_results.get(i) != null && !own_results.get(i).equals(other_results.get(i)))) {
						logger.debug("Found inspector assertion: ");

						InspectorAssertion assertion = new InspectorAssertion();
						assertion.source = return_values.get(line);
						List<Inspector> inspectors = manager.getInspectors(assertion.source.getVariableClass());
						//logger.info("Creating inspector assertion for class "+assertion.source.getVariableClass());
						assertion.inspector = inspectors.get(i);
						assertion.num_inspector = i;
						assertion.result = own_results.get(i);
						if (!other.isDetectedBy(assertion)) {
							logger.error("Invalid inspector assertion generated!");
							if (line != assertion.source.statement)
								logger.error("...because line doesn't match!");
							logger.error("" + own_results.get(i) + " / "
							        + other.inspector_results.get(i));
						} else {
							test.getStatement(line).addAssertion(assertion);
							num_assertions++;
						}

					}
				}
			}
			if (calleeMap.containsKey(line) && other.calleeMap.containsKey(line)) {
				Map<Inspector, Object> own_values = calleeMap.get(line);
				Map<Inspector, Object> other_values = other.calleeMap.get(line);
				MethodStatement ms = (MethodStatement) test.getStatement(line);
				VariableReference callee = ms.getCallee();
				for (Entry<Inspector, Object> value : own_values.entrySet()) {
					if (other_values.containsKey(value.getKey())) {

						if ((value.getValue() == null && other_values.get(value.getKey()) != null)
						        || (value.getValue() != null && !value.getValue().equals(other_values.get(value.getKey())))) {

							InspectorAssertion assertion = new InspectorAssertion();
							assertion.source = callee;
							assertion.inspector = value.getKey();
							assertion.result = value.getValue();
							test.getStatement(line).addAssertion(assertion);
							num_assertions++;
						}
					}
				}
			}
		}
		return num_assertions;
	}

	@Override
	public int numDiffer(OutputTrace other_trace) {
		InspectorTrace other = (InspectorTrace) other_trace;

		int num = 0;

		for (Entry<Integer, List<Object>> entry : inspector_results.entrySet()) {
			if (other.inspector_results.containsKey(entry.getKey())) {
				if (entry.getValue() == null
				        && other.inspector_results.get(entry.getKey()) != null) {
					num++; // Shouldn't happen
				} else if (entry.getValue() != null) {
					List<Object> other_results = other.inspector_results.get(entry.getKey());
					if (other_results.size() != entry.getValue().size())
						logger.warn("Number of inspectors does not match, ignoring");
					else {
						int pos = 0;
						for (Object o : entry.getValue()) {
							//							if(o != null && other_results.size() > pos && (other_results.get(pos) == null || !o.equals(other_results.get(pos))))
							if (o != null && other_results.size() > pos
							        && (!o.equals(other_results.get(pos)))) {
								logger.debug("Difference found at " + entry.getKey()
								        + ", inspector " + pos + ": " + o + "/"
								        + other_results.get(pos));
								num++;
							}
							pos++;
						}
					}
				}
			}

		}

		/*
		for(Entry<Integer, List<Object>> entry : other.inspector_results.entrySet()) {
			if(!inspector_results.containsKey(entry.getKey()))
				num++;

		}
		*/

		return num;
	}

	@Override
	public boolean isDetectedBy(Assertion assertion) {
		if (!(assertion instanceof InspectorAssertion))
			return false;

		InspectorAssertion p = (InspectorAssertion) assertion;
		if (inspector_results.containsKey(p.source.statement)) {
			List<Object> own_results = inspector_results.get(p.source.statement);
			List<Inspector> inspectors = manager.getInspectors(p.source.getVariableClass());
			if (inspectors.size() != own_results.size())
				logger.error("Size of inspectors and inspector results does not match!");
			if (p.result == null)
				return (own_results.size() > p.num_inspector && own_results.get(p.num_inspector) != null);
			else
				return (own_results.size() > p.num_inspector && !p.result.equals(own_results.get(p.num_inspector)));
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.testcase.OutputTrace#getAllAssertions(de.unisb.cs.st.evosuite.testcase.TestCase)
	 */
	@Override
	public int getAllAssertions(TestCase test) {
		int num_assertions = 0;
		for (int line = 0; line < test.size(); line++) {
			if (inspector_results.containsKey(line)) {

				List<Object> own_results = inspector_results.get(line);
				for (int i = 0; i < own_results.size(); i++) {
					InspectorAssertion assertion = new InspectorAssertion();
					assertion.source = return_values.get(line);
					List<Inspector> inspectors = manager.getInspectors(assertion.source.getVariableClass());
					//logger.info("Creating inspector assertion for class "+assertion.source.getVariableClass());
					assertion.inspector = inspectors.get(i);
					assertion.result = own_results.get(i);
					test.getStatement(line).addAssertion(assertion);
					num_assertions++;
				}
			}
			if (calleeMap.containsKey(line)) {
				Map<Inspector, Object> values = calleeMap.get(line);
				MethodStatement ms = (MethodStatement) test.getStatement(line);
				VariableReference callee = ms.getCallee();
				for (Entry<Inspector, Object> value : values.entrySet()) {
					InspectorAssertion assertion = new InspectorAssertion();
					assertion.source = callee;
					assertion.inspector = value.getKey();
					assertion.result = value.getValue();
					test.getStatement(line).addAssertion(assertion);
					num_assertions++;
				}
			}
		}
		return num_assertions;
	}

}
