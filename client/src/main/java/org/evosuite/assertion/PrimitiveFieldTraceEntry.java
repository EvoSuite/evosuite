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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * <p>PrimitiveFieldTraceEntry class.</p>
 *
 * @author fraser
 */
public class PrimitiveFieldTraceEntry implements OutputTraceEntry {

    private final Map<Field, Object> fieldMap = new HashMap<>();

    private final VariableReference var;

    /**
     * <p>Constructor for PrimitiveFieldTraceEntry.</p>
     *
     * @param var a {@link org.evosuite.testcase.variable.VariableReference} object.
     */
    public PrimitiveFieldTraceEntry(VariableReference var) {
        this.var = var;
    }

    /**
     * Insert a new value into the map
     *
     * @param field a {@link java.lang.reflect.Field} object.
     * @param value a {@link java.lang.Object} object.
     */
    public void addValue(Field field, Object value) {
        fieldMap.put(field, value);
    }

    /* (non-Javadoc)
     * @see org.evosuite.assertion.OutputTraceEntry#differs(org.evosuite.assertion.OutputTraceEntry)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean differs(OutputTraceEntry other) {
        if (other instanceof PrimitiveFieldTraceEntry) {
            PrimitiveFieldTraceEntry otherEntry = (PrimitiveFieldTraceEntry) other;
            for (Field field : fieldMap.keySet()) {
                if (otherEntry.fieldMap.containsKey(field)) {
                    Object o1 = fieldMap.get(field);
                    Object o2 = otherEntry.fieldMap.get(field);
                    if (o1 == null) {
                        return o2 != null;
                    } else {
                        return !o1.equals(o2);
                    }
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

        if (other instanceof PrimitiveFieldTraceEntry) {
            PrimitiveFieldTraceEntry otherEntry = (PrimitiveFieldTraceEntry) other;

            for (Field field : fieldMap.keySet()) {
                if (!otherEntry.fieldMap.containsKey(field)) {
                    continue;
                }

                if (!otherEntry.fieldMap.get(field).equals(fieldMap.get(field))) {
                    PrimitiveFieldAssertion assertion = new PrimitiveFieldAssertion();
                    assertion.value = fieldMap.get(field);
                    assertion.field = field;
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
        for (Field field : fieldMap.keySet()) {
            PrimitiveFieldAssertion assertion = new PrimitiveFieldAssertion();
            assertion.value = fieldMap.get(field);
            assertion.field = field;
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
        if (assertion instanceof PrimitiveFieldAssertion) {
            PrimitiveFieldAssertion ass = (PrimitiveFieldAssertion) assertion;
            if (ass.source.equals(var) && fieldMap.containsKey(ass.field)) {
                return !fieldMap.get(ass.field).equals(ass.value);
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
        PrimitiveFieldTraceEntry copy = new PrimitiveFieldTraceEntry(var);
        copy.fieldMap.putAll(fieldMap);
        return copy;
    }

}
