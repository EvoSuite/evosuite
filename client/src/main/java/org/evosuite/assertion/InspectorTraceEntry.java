/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.assertion;

import org.evosuite.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * <p>InspectorTraceEntry class.</p>
 *
 * @author fraser
 */
public class InspectorTraceEntry implements OutputTraceEntry {

    private final static Logger logger = LoggerFactory.getLogger(InspectorTraceEntry.class);
    private final Map<Inspector, Object> inspectorMap = new HashMap<>();
    private final Map<String, Inspector> methodInspectorMap = new HashMap<>();
    private final VariableReference var;

    /**
     * <p>Constructor for InspectorTraceEntry.</p>
     *
     * @param var a {@link org.evosuite.testcase.variable.VariableReference} object.
     */
    public InspectorTraceEntry(VariableReference var) {
        this.var = var;
    }

    /**
     * <p>addValue</p>
     *
     * @param inspector a {@link org.evosuite.assertion.Inspector} object.
     * @param value     a {@link java.lang.Object} object.
     */
    public void addValue(Inspector inspector, Object value) {
        inspectorMap.put(inspector, value);
        methodInspectorMap.put(inspector.getClassName() + " " + inspector.getMethodCall(), inspector);
    }

    /**
     * <p>size</p>
     *
     * @return a int.
     */
    public int size() {
        return inspectorMap.size();
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#differs(org.evosuite.assertion.OutputTraceEntry)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean differs(OutputTraceEntry other) {
        if (other instanceof InspectorTraceEntry) {
            if (!((InspectorTraceEntry) other).var.equals(var)) {
                return false;
            }

            InspectorTraceEntry otherEntry = (InspectorTraceEntry) other;
            for (Inspector inspector : inspectorMap.keySet()) {
                logger.debug("Current inspector: " + inspector);
                if (!otherEntry.inspectorMap.containsKey(inspector)
                        || otherEntry.inspectorMap.get(inspector) == null
                        || inspectorMap.get(inspector) == null) {
                    logger.debug("Other trace does not have " + inspector);
                    continue;
                }

                if (!otherEntry.inspectorMap.get(inspector).equals(inspectorMap.get(inspector))) {
                    return true;
                } else {
                    logger.debug("Value is equal: " + inspector);
                }
            }

        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#getAssertions(org.evosuite.assertion.OutputTraceEntry)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Assertion> getAssertions(OutputTraceEntry other) {
        Set<Assertion> assertions = new HashSet<>();

        if (other instanceof InspectorTraceEntry) {
            InspectorTraceEntry otherEntry = (InspectorTraceEntry) other;
            for (String inspector : methodInspectorMap.keySet()) {
                if (!otherEntry.inspectorMap.containsKey(otherEntry.methodInspectorMap.get(inspector))
                        || otherEntry.inspectorMap.get(otherEntry.methodInspectorMap.get(inspector)) == null
                        || inspectorMap.get(methodInspectorMap.get(inspector)) == null) {
                    continue;
                }

                if (!otherEntry.inspectorMap.get(otherEntry.methodInspectorMap.get(inspector))
                        .equals(inspectorMap.get(methodInspectorMap.get(inspector)))) {
                    InspectorAssertion assertion = new InspectorAssertion();
                    assertion.value = inspectorMap.get(methodInspectorMap.get(inspector));
                    assertion.inspector = methodInspectorMap.get(inspector);
                    assertion.source = var;
                    assertions.add(assertion);
                    assert (assertion.isValid());

                }
            }
        }
        return assertions;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#getAssertions()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Assertion> getAssertions() {
        Set<Assertion> assertions = new HashSet<>();

        for (Inspector inspector : inspectorMap.keySet()) {
            InspectorAssertion assertion = new InspectorAssertion();
            assertion.value = inspectorMap.get(inspector);
            assertion.inspector = inspector;
            assertion.source = var;
            assertions.add(assertion);
            assert (assertion.isValid());
        }
        return assertions;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#isDetectedBy(org.evosuite.assertion.Assertion)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDetectedBy(Assertion assertion) {
        if (assertion instanceof InspectorAssertion) {
            InspectorAssertion ass = (InspectorAssertion) assertion;
            if (ass.source.same(var)) {
                if (inspectorMap.containsKey(ass.inspector)
                        && inspectorMap.get(ass.inspector) != null && ass.value != null) {
                    return !inspectorMap.get(ass.inspector).equals(ass.value);
                }
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#cloneEntry()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputTraceEntry cloneEntry() {
        InspectorTraceEntry copy = new InspectorTraceEntry(var);
        copy.inspectorMap.putAll(inspectorMap);
        copy.methodInspectorMap.putAll(methodInspectorMap);
        return copy;
    }

}
