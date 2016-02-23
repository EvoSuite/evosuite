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
package com.examples.with.different.packagename.stable;

public class StringUser {

	private final String myStr = "Hello World!";
	private final String emptyStr ="";
	private final String trueStr = "True";

	
	public StringUser() {
	}
	
	public boolean isEmptyShouldReturnFalse() {
		return myStr.isEmpty();
	}
	
	public boolean isEmptyShouldReturnTrue() {
		return emptyStr.isEmpty();
	}

	public boolean equalsShouldReturnFalse() {
		return myStr.equals(emptyStr);
	}

	public boolean equalsShouldReturnTrue() {
		return myStr.equals(myStr);
	}

	public boolean equalsIgnoreCaseShouldReturnFalse() {
		return myStr.equalsIgnoreCase(emptyStr);
	}

	public boolean equalsIgnoreCaseShouldReturnTrue() {
		return myStr.equalsIgnoreCase(myStr);
	}

	public boolean startsWithShouldReturnFalse() {
		return emptyStr.startsWith(myStr);
	}

	public boolean startsWithShouldReturnTrue() {
		return myStr.startsWith(emptyStr);
	}

	public boolean endsWithShouldReturnFalse() {
		return emptyStr.endsWith(myStr);
	}

	public boolean endsWithShouldReturnTrue() {
		return myStr.endsWith(emptyStr);
	}
	
	public boolean matchesShouldReturnTrue() {
		return trueStr.endsWith("[tT]rue");
	}

	public boolean matchesShouldReturnFalse() {
		return myStr.endsWith("[tT]rue");
	}

}
