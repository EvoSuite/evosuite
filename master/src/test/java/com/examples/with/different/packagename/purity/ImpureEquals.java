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
package com.examples.with.different.packagename.purity;

public class ImpureEquals {

	private boolean flag =false;
	private final int value;
	
	public int getValue() {
		return value;
	}
	
	public boolean getFlag() {
		return flag;
	}
	
	public ImpureEquals(int value) {
		this.value = value;
	}
	@Override
	public int hashCode() {
		return this.value;
	}
	
	@Override
	public boolean equals(Object o) {
		setFlag();
		if (o==this)
			return true;
		if (o==null)
			return false;
		if (o.getClass().equals(ImpureEquals.class)) {
			ImpureEquals that =(ImpureEquals)o;
			return this.value==that.value;
		} else {
			return false;
		}
	}

	public  void setFlag() {
		flag = true;
	}

	public void clearFlag() {
		flag = false;
	}

}
