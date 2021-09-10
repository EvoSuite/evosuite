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

import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.execution.CodeUnderTestException;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.utils.NumberFormatter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

public class PrimitiveFieldAssertion extends Assertion {

    private static final long serialVersionUID = 2827276810722210456L;

    protected transient Field field;

    /**
     * <p>
     * Getter for the field <code>field</code>.
     * </p>
     *
     * @return a {@link java.lang.reflect.Field} object.
     */
    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCode() {
        if (value == null) {
            return "assertNull(" + source.getName() + "." + field.getName() + ");";
        } else if (value.getClass().equals(Long.class)) {
            return "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
                    + source.getName() + "." + field.getName() + ");";
        } else if (value.getClass().equals(Float.class)) {
            return "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
                    + source.getName() + "." + field.getName() + ", 0.01F);";
        } else if (value.getClass().equals(Double.class)) {
            return "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
                    + source.getName() + "." + field.getName() + ", 0.01D);";
        } else if (value.getClass().equals(Character.class)) {
            return "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
                    + source.getName() + "." + field.getName() + ");";
        } else if (value.getClass().equals(String.class)) {
            return "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
                    + source.getName() + "." + field.getName() + ");";
        } else
            return "assertEquals(" + NumberFormatter.getNumberString(value) + ", "
                    + source.getName() + "." + field.getName() + ");";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Assertion copy(TestCase newTestCase, int offset) {
        PrimitiveFieldAssertion s = new PrimitiveFieldAssertion();
        s.source = newTestCase.getStatement(source.getStPosition() + offset).getReturnValue();
        s.value = value;
        s.field = field;
        s.comment = comment;
        s.killedMutants.addAll(killedMutants);
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean evaluate(Scope scope) {
        try {
            Object obj = source.getObject(scope);
            if (obj != null) {
                try {
                    Object val = field.get(obj);
                    if (val != null)
                        return val.equals(value);
                    else
                        return value == null;
                } catch (Exception e) {
                    return true;
                }
            } else
                return true;
        } catch (CodeUnderTestException e) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PrimitiveFieldAssertion other = (PrimitiveFieldAssertion) obj;
        if (field == null) {
            return other.field == null;
        } else return field.equals(other.field);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        // Write/save additional fields
        oos.writeObject(field.getDeclaringClass().getName());
        oos.writeObject(field.getName());
    }

    // assumes "static java.util.Date aDate;" declared
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException,
            IOException {
        ois.defaultReadObject();

        // Read/initialize additional fields
        Class<?> methodClass = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass((String) ois.readObject());
        String fieldName = (String) ois.readObject();

        try {
            field = methodClass.getField(fieldName);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
