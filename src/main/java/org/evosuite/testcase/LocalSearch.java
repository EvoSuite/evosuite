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

import org.evosuite.Properties;
import org.evosuite.ga.LocalSearchObjective;

/**
 * <p>
 * LocalSearch interface.
 * </p>
 * 
 * @author Gordon Fraser
 */
public abstract class LocalSearch {

	/**
	 * <p>
	 * doSearch
	 * </p>
	 * 
	 * @param test
	 *            a {@link org.evosuite.testcase.TestChromosome} object.
	 * @param statement
	 *            a int.
	 * @param objective
	 *            a {@link org.evosuite.ga.LocalSearchObjective} object.
	 */
	public abstract boolean doSearch(TestChromosome test, int statement,
	        LocalSearchObjective<TestChromosome> objective);

	/**
	 * If the position of the statement on which the local search was performed
	 * has changed, then we need to tell this to the outside world
	 * 
	 * @return
	 */
	public int getPositionDelta() {
		return 0;
	}

	public static LocalSearch getLocalSearchFor(StatementInterface statement) {
		LocalSearch search = null;
		if (statement instanceof NullStatement) {
			if (Properties.LOCAL_SEARCH_REFERENCES == false)
				return null;

			search = new ReferenceLocalSearch();
			//search = new NullReferenceSearch();
		} else if (statement instanceof PrimitiveStatement<?>) {
			if (Properties.LOCAL_SEARCH_PRIMITIVES == false)
				return null;

			Class<?> type = statement.getReturnValue().getVariableClass();
			if (type.equals(Integer.class) || type.equals(int.class)) {
				//search = new IntegerLocalSearch<Integer>();
				search = new DSELocalSearch();
			} else if (type.equals(Byte.class) || type.equals(byte.class)) {
				// search = new IntegerLocalSearch<Byte>();
				search = new DSELocalSearch();
			} else if (type.equals(Short.class) || type.equals(short.class)) {
				// search = new IntegerLocalSearch<Short>();
				search = new DSELocalSearch();
			} else if (type.equals(Long.class) || type.equals(long.class)) {
				// search = new IntegerLocalSearch<Long>();
				search = new DSELocalSearch();
			} else if (type.equals(Character.class) || type.equals(char.class)) {
				// search = new IntegerLocalSearch<Character>();
				search = new DSELocalSearch();
			} else if (type.equals(Float.class) || type.equals(float.class)) {
				// search = new FloatLocalSearch<Float>();
				search = new DSELocalSearch();
			} else if (type.equals(Double.class) || type.equals(double.class)) {
				// search = new FloatLocalSearch<Double>();
				search = new DSELocalSearch();
			} else if (type.equals(String.class)) {
				// search = new StringLocalSearch();
				search = new DSELocalSearch();
			} else if (type.equals(Boolean.class)) {
				search = new BooleanLocalSearch();
			} else if (statement instanceof EnumPrimitiveStatement) {
				search = new EnumLocalSearch();
			}
		} else if (statement instanceof ArrayStatement) {
			if (Properties.LOCAL_SEARCH_ARRAYS == false)
				return null;

			search = new ArrayLocalSearch();
		} else if (statement instanceof MethodStatement) {
			if (Properties.LOCAL_SEARCH_REFERENCES == false)
				return null;

			search = new ReferenceLocalSearch();
		} else if (statement instanceof ConstructorStatement) {
			if (Properties.LOCAL_SEARCH_REFERENCES == false)
				return null;

			search = new ReferenceLocalSearch();
		} else if (statement instanceof FieldStatement) {
			if (Properties.LOCAL_SEARCH_REFERENCES == false)
				return null;

			search = new ReferenceLocalSearch();
		}
		return search;
	}

}
