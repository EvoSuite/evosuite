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
package org.evosuite.symbolic.expr;

public class IntegerVariable extends IntegerExpression implements
		Variable<Long> {

	private static final long serialVersionUID = 6302073364874210525L;

	protected String name;

	protected long concreteValue;

	protected long minValue;

	protected long maxValue;

	/**
	 * <p>
	 * Constructor for IntegerVariable.
	 * </p>
	 * 
	 * @param name
	 *            a {@link java.lang.String} object.
	 * @param conV
	 *            a long.
	 * @param minValue
	 *            a long.
	 * @param maxValue
	 *            a long.
	 */
	public IntegerVariable(String name, long conV, long minValue, long maxValue) {
		super();
		this.name = name;
		this.concreteValue = conV;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.containsSymbolicVariable = true;
	}

	/** {@inheritDoc} */
	@Override
	public Long getConcreteValue() {
		return concreteValue;
	}

	/**
	 * <p>
	 * Setter for the field <code>concreteValue</code>.
	 * </p>
	 * 
	 * @param con
	 *            a {@link java.lang.Long} object.
	 */
	public void setConcreteValue(Long con) {
		concreteValue = con;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public Long getMinValue() {
		return minValue;
	}

	/** {@inheritDoc} */
	@Override
	public Long getMaxValue() {
		return maxValue;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.name + "(" + concreteValue + ")";
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IntegerVariable) {
			Variable<Integer> v = (Variable<Integer>) obj;
			return this.getName().equals(v.getName());
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		if (hash == 0) {
			hash = this.name.hashCode();
		}
		return hash;
	}

	/** {@inheritDoc} */
	@Override
	public int getSize() {
		return 1;
	}

	/** {@inheritDoc} */
	@Override
	public Long execute() {
		return concreteValue;
	}

}
