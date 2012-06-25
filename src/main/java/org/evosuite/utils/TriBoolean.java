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
package org.evosuite.utils;

public enum TriBoolean {
	True, False, Maybe;

	public static TriBoolean fromBoolean(boolean value) {
		return value ? True : False;
	}
	
	public TriBoolean and(TriBoolean other) {
		if (this == False || other == False) return False;
		if (this == Maybe || other == Maybe) return Maybe;
		return True;
	}

	public TriBoolean and(boolean other) {
		return and(fromBoolean(other));
	}

	public TriBoolean or(TriBoolean other) {
		if (this == True  || other == True)  return True; 
		if (this == Maybe || other == Maybe) return Maybe;
		return False;
	}

	public TriBoolean or(boolean other) {
		return or(fromBoolean(other));
	}

	public TriBoolean negated() {
		return (this == Maybe) ? Maybe : fromBoolean(this == False);
	}
	
	public TriBoolean andNot(TriBoolean other) {
		return and(other.negated());
	}

	public TriBoolean andNot(boolean other) {
		return andNot(fromBoolean(other));
	}

	public TriBoolean orNot(TriBoolean other) {
		return or(other.negated());
	}

	public TriBoolean orNot(boolean other) {
		return orNot(fromBoolean(other));
	}
	
	public TriBoolean notAnd(TriBoolean other) {
		return and(other).negated();
	}

	public TriBoolean notAnd(boolean other) {
		return notAnd(fromBoolean(other));
	}

	public TriBoolean notOr(TriBoolean other) {
		return or(other).negated();
	}

	public TriBoolean notOr(boolean other) {
		return notOr(fromBoolean(other));
	}
	
	public boolean isPossiblyTrue() {
		return this == True || this == Maybe; 
	}
	
	public boolean isPossiblyFalse() {
		return this == False || this == Maybe;
	}
	
	public boolean isCertainlyTrue() {
		return this == True;
	}
	
	public boolean isCertainlyFalse() {
		return this == False;
	}
}
