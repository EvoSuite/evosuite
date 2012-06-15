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
package de.unisb.cs.st.evosuite.testcase;


public class ArrayReference extends VariableReferenceImpl {

	private static final long serialVersionUID = 3309591356542131910L;

	protected int array_length;

	public ArrayReference(TestCase tc, GenericClass clazz, int array_length) {
		super(tc, clazz);
		assert (array_length >= 0);
		this.array_length = array_length;
	}

	public int getArrayLength() {
		return array_length;
	}

	public void setArrayLength(int l) {
		assert (l >= 0);
		array_length = l;
	}

	/**
	 * Create a copy of the current variable
	 */
	@Override
	public VariableReference copy(TestCase newTestCase, int offset) {
		VariableReference newRef = newTestCase.getStatement(getStPosition() + offset).getReturnValue();
		if (newRef instanceof ArrayReference) {
			ArrayReference otherArray = (ArrayReference) newRef;
			otherArray.setArrayLength(array_length);
			return otherArray;
		} else {

			// FIXXME: This part should be redundant

			if (newRef.getComponentType() != null) {
				ArrayReference otherArray = new ArrayReference(newTestCase, type,
				        array_length);
				otherArray.setArrayLength(array_length);
				newTestCase.getStatement(getStPosition() + offset).setRetval(otherArray);
				return otherArray;
			} else {
				// This may happen when cloning a method statement which returns an Object that in fact is an array
				// We'll just create a new ArrayReference in this case.
				ArrayReference otherArray = new ArrayReference(newTestCase, type,
				        array_length);
				otherArray.setArrayLength(array_length);
				newTestCase.getStatement(getStPosition() + offset).setRetval(otherArray);
				return otherArray;
				//				throw new RuntimeException("After cloning the array disappeared: "
				//				        + getName() + "/" + newRef.getName() + " in test "
				//				        + newTestCase.toCode() + " / old test: " + testCase.toCode());
			}
		}
	}
}
