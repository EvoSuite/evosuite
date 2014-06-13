/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.testcase;

import java.lang.reflect.Type;

import org.evosuite.utils.GenericClass;
import org.evosuite.utils.NumberFormatter;


/**
 * <p>ConstantValue class.</p>
 *
 * @author Gordon Fraser
 */
public class ConstantValue extends VariableReferenceImpl {

	private static final long serialVersionUID = -3760942087575495415L;

	/**
	 * <p>Constructor for ConstantValue.</p>
	 *
	 * @param testCase a {@link org.evosuite.testcase.TestCase} object.
	 * @param type a {@link org.evosuite.utils.GenericClass} object.
	 */
	public ConstantValue(TestCase testCase, GenericClass type) {
		super(testCase, type);
	}

	/**
	 * <p>Constructor for ConstantValue.</p>
	 *
	 * @param testCase a {@link org.evosuite.testcase.TestCase} object.
	 * @param type a {@link java.lang.reflect.Type} object.
	 */
	public ConstantValue(TestCase testCase, Type type) {
		this(testCase, new GenericClass(type));
	}

	/**
	 * {@inheritDoc}
	 *
	 * Create a copy of the current variable
	 */
	@Override
	public VariableReference copy(TestCase newTestCase, int offset) {
		ConstantValue ret = new ConstantValue(newTestCase, type);
		ret.setValue(value);
		return ret;
	}

	private Object value;

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Object} object.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * <p>Setter for the field <code>value</code>.</p>
	 *
	 * @param value a {@link java.lang.Object} object.
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * {@inheritDoc}
	 *
	 * The position of the statement, defining this VariableReference, in the
	 * testcase.
	 */
	@Override
	public int getStPosition() {
		for (int i = 0; i < testCase.size(); i++) {
			if (testCase.getStatement(i).references(this)) {
				return i;
			}
		}

		throw new AssertionError(
		        "A ConstantValue position is only defined if the VariableReference is defined by a statement");
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return name for source code representation
	 */
	@Override
	public String getName() {
		return NumberFormatter.getNumberString(value);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return the actual object represented by this variable for a given scope
	 */
	@Override
	public Object getObject(Scope scope) {
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public boolean same(VariableReference r) {
		if (r == null)
			return false;

		if (!this.type.equals(r.getGenericClass()))
			return false;

		if (r instanceof ConstantValue) {
			ConstantValue v = (ConstantValue) r;
			if (this.value == null) {
				if (v.getValue() == null)
					return true;
			} else {
				if (this.value.equals(v.getValue()))
					return true;
			}
		}

		return false;
	}

}
