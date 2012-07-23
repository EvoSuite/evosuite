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
 * <p>Tuple class.</p>
 *
 * @author Gordon Fraser
 */
package org.evosuite.callgraph;

import java.io.Serializable;
public class Tuple implements Serializable {
	private static final long serialVersionUID = 1L;

	MethodDescription start;

	MethodDescription end;

	/**
	 * <p>Constructor for Tuple.</p>
	 *
	 * @param m1 a {@link org.evosuite.callgraph.MethodDescription} object.
	 * @param m2 a {@link org.evosuite.callgraph.MethodDescription} object.
	 */
	public Tuple(MethodDescription m1, MethodDescription m2) {
		super();
		this.start = m1;
		this.end = m2;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tuple other = (Tuple) obj;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		return true;
	}

	/**
	 * <p>Getter for the field <code>start</code>.</p>
	 *
	 * @return a {@link org.evosuite.callgraph.MethodDescription} object.
	 */
	public MethodDescription getStart() {
		return start;
	}

	/**
	 * <p>Getter for the field <code>end</code>.</p>
	 *
	 * @return a {@link org.evosuite.callgraph.MethodDescription} object.
	 */
	public MethodDescription getEnd() {
		return end;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return start + " - " + end;
	}
}
