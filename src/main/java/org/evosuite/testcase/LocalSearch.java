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
	        LocalSearchObjective objective);

	public static LocalSearch getLocalSearchFor(StatementInterface statement) {
		LocalSearch search = null;
		if (statement instanceof NullStatement) {
			search = new ReferenceLocalSearch();
			//search = new NullReferenceSearch();
		} else if (statement instanceof PrimitiveStatement<?>) {
			Class<?> type = statement.getReturnValue().getVariableClass();
			if (type.equals(Integer.class) || type.equals(int.class)) {
				search = new IntegerLocalSearch<Integer>();
			} else if (type.equals(Byte.class) || type.equals(byte.class)) {
				search = new IntegerLocalSearch<Byte>();
			} else if (type.equals(Short.class) || type.equals(short.class)) {
				search = new IntegerLocalSearch<Short>();
			} else if (type.equals(Long.class) || type.equals(long.class)) {
				search = new IntegerLocalSearch<Long>();
			} else if (type.equals(Character.class) || type.equals(char.class)) {
				search = new IntegerLocalSearch<Character>();
			} else if (type.equals(Float.class) || type.equals(float.class)) {
				search = new FloatLocalSearch<Float>();
			} else if (type.equals(Double.class) || type.equals(double.class)) {
				search = new FloatLocalSearch<Double>();
			} else if (type.equals(String.class)) {
				search = new StringLocalSearch();
			} else if (type.equals(Boolean.class)) {
				search = new BooleanLocalSearch();
			} else if (statement instanceof EnumPrimitiveStatement) {
				search = new EnumLocalSearch();
			}
		} else if (statement instanceof ArrayStatement) {
			search = new ArrayLocalSearch();
		} else if (statement instanceof MethodStatement) {
			search = new ReferenceLocalSearch();
		} else if (statement instanceof ConstructorStatement) {
			search = new ReferenceLocalSearch();
		} else if (statement instanceof FieldStatement) {
			search = new ReferenceLocalSearch();
		}
		return search;
	}

}
