
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
 *
 * @author Gordon Fraser
 */
package org.evosuite.testcase;

import java.util.List;
public class ArrayReference extends VariableReferenceImpl {

	private static final long serialVersionUID = 3309591356542131910L;

	private int[] lengths;
	
	/**
	 * <p>Constructor for ArrayReference.</p>
	 *
	 * @param tc a {@link org.evosuite.testcase.TestCase} object.
	 * @param clazz a {@link java.lang.Class} object.
	 */
	public ArrayReference(TestCase tc, Class<?> clazz){
		this(tc, new GenericClass(clazz), new int[ArrayStatement.determineDimensions(clazz)]);
	}
	
	/**
	 * <p>Constructor for ArrayReference.</p>
	 *
	 * @param tc a {@link org.evosuite.testcase.TestCase} object.
	 * @param clazz a {@link org.evosuite.testcase.GenericClass} object.
	 * @param lengths an array of int.
	 */
	public ArrayReference(TestCase tc, GenericClass clazz, int[] lengths) {
		super(tc, clazz);
		assert (lengths.length > 0);
		this.lengths = lengths;
	}

	/**
	 * <p>Constructor for ArrayReference.</p>
	 *
	 * @param tc a {@link org.evosuite.testcase.TestCase} object.
	 * @param clazz a {@link org.evosuite.testcase.GenericClass} object.
	 * @param array_length a int.
	 */
	public ArrayReference(TestCase tc, GenericClass clazz, int array_length) {
		this(tc, clazz, new int[]{array_length});
	}

	/**
	 * <p>getArrayLength</p>
	 *
	 * @return a int.
	 */
	public int getArrayLength() {
		assert lengths.length == 1;
		return lengths[0];
	}

	/**
	 * <p>setArrayLength</p>
	 *
	 * @param l a int.
	 */
	public void setArrayLength(int l) {
		assert (l >= 0);
		assert lengths.length == 1;
		lengths[0] = l;
	}

	/**
	 * <p>Getter for the field <code>lengths</code>.</p>
	 *
	 * @return an array of int.
	 */
	public int[] getLengths() {
		return lengths;
	}

	/**
	 * <p>Setter for the field <code>lengths</code>.</p>
	 *
	 * @param lengths an array of int.
	 */
	public void setLengths(int[] lengths) {
		this.lengths = lengths;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Create a copy of the current variable
	 */
	@Override
	public VariableReference copy(TestCase newTestCase, int offset) {
		VariableReference newRef = newTestCase.getStatement(getStPosition() + offset).getReturnValue();
		if (newRef instanceof ArrayReference) {
			ArrayReference otherArray = (ArrayReference) newRef;
			otherArray.setLengths(lengths);
			return otherArray;
		} else {

			// FIXXME: This part should be redundant

			if (newRef.getComponentType() != null) {
				ArrayReference otherArray = new ArrayReference(newTestCase, type, lengths);
				newTestCase.getStatement(getStPosition() + offset).setRetval(otherArray);
				return otherArray;
			} else {
				// This may happen when cloning a method statement which returns an Object that in fact is an array
				// We'll just create a new ArrayReference in this case.
				ArrayReference otherArray = new ArrayReference(newTestCase, type, lengths);
				newTestCase.getStatement(getStPosition() + offset).setRetval(otherArray);
				return otherArray;
				//				throw new RuntimeException("After cloning the array disappeared: "
				//				        + getName() + "/" + newRef.getName() + " in test "
				//				        + newTestCase.toCode() + " / old test: " + testCase.toCode());
			}
		}
	}

	/**
	 * <p>getArrayDimensions</p>
	 *
	 * @return a int.
	 */
	public int getArrayDimensions() {
		return lengths.length;
	}

	/**
	 * <p>Setter for the field <code>lengths</code>.</p>
	 *
	 * @param lengths a {@link java.util.List} object.
	 */
	public void setLengths(List<Integer> lengths) {
		this.lengths = new int[lengths.size()];
		int idx = 0;
		for (Integer length : lengths) {
			this.lengths[idx] = length;
			idx++;
		}
	}
}
