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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ListContainerUser {

	private final List<Object> myList = new LinkedList<Object>();

	private final List<Object> myEmptyList = new LinkedList<Object>();

	private final boolean isMyContainsFlag;
	private final boolean isMyContainsAllFlag;
	private final boolean isMyEmptyFlag;

	private final Object myObject;

	public ListContainerUser() {
		myObject = new Object();
		myList.add(myObject);
		isMyEmptyFlag = myList.isEmpty();
		Object otherObject = new Object();

		isMyContainsFlag = myList.contains(otherObject);
		List<Object> otherList = Arrays.asList(otherObject, myObject);
		isMyContainsAllFlag = myList.containsAll(otherList);
		
	}

	public String toString() {
		return "isMyEmpty(myList)=" + myList.isEmpty() + " ,size(myList)="
				+ myList.size();
	}

	public boolean containsShouldReturnFalse() {
		return isMyContainsFlag;
	}

	public boolean containsAllShouldReturnFalse() {
		return isMyContainsAllFlag;
	}

	public boolean isEmptyShouldReturnFalse() {
		return isMyEmptyFlag;
	}
	
	public boolean isEmptyShouldReturnTrue() {
		return myEmptyList.isEmpty();
	}
	
	public boolean containsShouldReturnTrue() {
		return myList.contains(myObject);
	}

	public boolean containsAllOnEmptyShouldReturnTrue() {
		return myList.containsAll(myEmptyList);
	}

	public boolean containsAllOnNonEmptyShouldReturnTrue() {
		return myList.containsAll(myList);
	}

}
