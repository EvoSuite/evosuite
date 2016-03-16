/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.utils;
public enum TriBoolean {
	True, False, Maybe;

	/**
	 * <p>fromBoolean</p>
	 *
	 * @param value a boolean.
	 * @return a {@link org.evosuite.utils.TriBoolean} object.
	 */
	public static TriBoolean fromBoolean(boolean value) {
		return value ? True : False;
	}
	
	/**
	 * <p>and</p>
	 *
	 * @param other a {@link org.evosuite.utils.TriBoolean} object.
	 * @return a {@link org.evosuite.utils.TriBoolean} object.
	 */
	public TriBoolean and(TriBoolean other) {
		if (this == False || other == False) return False;
		if (this == Maybe || other == Maybe) return Maybe;
		return True;
	}

	/**
	 * <p>and</p>
	 *
	 * @param other a boolean.
	 * @return a {@link org.evosuite.utils.TriBoolean} object.
	 */
	public TriBoolean and(boolean other) {
		return and(fromBoolean(other));
	}

	/**
	 * <p>or</p>
	 *
	 * @param other a {@link org.evosuite.utils.TriBoolean} object.
	 * @return a {@link org.evosuite.utils.TriBoolean} object.
	 */
	public TriBoolean or(TriBoolean other) {
		if (this == True  || other == True)  return True; 
		if (this == Maybe || other == Maybe) return Maybe;
		return False;
	}

	/**
	 * <p>or</p>
	 *
	 * @param other a boolean.
	 * @return a {@link org.evosuite.utils.TriBoolean} object.
	 */
	public TriBoolean or(boolean other) {
		return or(fromBoolean(other));
	}

	/**
	 * <p>negated</p>
	 *
	 * @return a {@link org.evosuite.utils.TriBoolean} object.
	 */
	public TriBoolean negated() {
		return (this == Maybe) ? Maybe : fromBoolean(this == False);
	}
	
	/**
	 * <p>andNot</p>
	 *
	 * @param other a {@link org.evosuite.utils.TriBoolean} object.
	 * @return a {@link org.evosuite.utils.TriBoolean} object.
	 */
	public TriBoolean andNot(TriBoolean other) {
		return and(other.negated());
	}

	/**
	 * <p>andNot</p>
	 *
	 * @param other a boolean.
	 * @return a {@link org.evosuite.utils.TriBoolean} object.
	 */
	public TriBoolean andNot(boolean other) {
		return andNot(fromBoolean(other));
	}

	/**
	 * <p>orNot</p>
	 *
	 * @param other a {@link org.evosuite.utils.TriBoolean} object.
	 * @return a {@link org.evosuite.utils.TriBoolean} object.
	 */
	public TriBoolean orNot(TriBoolean other) {
		return or(other.negated());
	}

	/**
	 * <p>orNot</p>
	 *
	 * @param other a boolean.
	 * @return a {@link org.evosuite.utils.TriBoolean} object.
	 */
	public TriBoolean orNot(boolean other) {
		return orNot(fromBoolean(other));
	}
	
	/**
	 * <p>notAnd</p>
	 *
	 * @param other a {@link org.evosuite.utils.TriBoolean} object.
	 * @return a {@link org.evosuite.utils.TriBoolean} object.
	 */
	public TriBoolean notAnd(TriBoolean other) {
		return and(other).negated();
	}

	/**
	 * <p>notAnd</p>
	 *
	 * @param other a boolean.
	 * @return a {@link org.evosuite.utils.TriBoolean} object.
	 */
	public TriBoolean notAnd(boolean other) {
		return notAnd(fromBoolean(other));
	}

	/**
	 * <p>notOr</p>
	 *
	 * @param other a {@link org.evosuite.utils.TriBoolean} object.
	 * @return a {@link org.evosuite.utils.TriBoolean} object.
	 */
	public TriBoolean notOr(TriBoolean other) {
		return or(other).negated();
	}

	/**
	 * <p>notOr</p>
	 *
	 * @param other a boolean.
	 * @return a {@link org.evosuite.utils.TriBoolean} object.
	 */
	public TriBoolean notOr(boolean other) {
		return notOr(fromBoolean(other));
	}
	
	/**
	 * <p>isPossiblyTrue</p>
	 *
	 * @return a boolean.
	 */
	public boolean isPossiblyTrue() {
		return this == True || this == Maybe; 
	}
	
	/**
	 * <p>isPossiblyFalse</p>
	 *
	 * @return a boolean.
	 */
	public boolean isPossiblyFalse() {
		return this == False || this == Maybe;
	}
	
	/**
	 * <p>isCertainlyTrue</p>
	 *
	 * @return a boolean.
	 */
	public boolean isCertainlyTrue() {
		return this == True;
	}
	
	/**
	 * <p>isCertainlyFalse</p>
	 *
	 * @return a boolean.
	 */
	public boolean isCertainlyFalse() {
		return this == False;
	}
}
