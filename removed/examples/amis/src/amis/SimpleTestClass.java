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
package amis;

/**
 * @author Andre Mis
 *
 */
public class SimpleTestClass {

	private int field;
	private boolean flag = false;
	
	public SimpleTestClass(int anInt) {
		field = anInt;
	}
	
	public void setField(int val) {
		field = val;
	}
	
	public int getField() {
		return field;
	}
	
	public void simpleMean(int param) {
		if(param % 3 == 0)
			field=1;
		else if(param % 2 != 0)
			field=2;
		
		if(field == 0) // target use
			field = 3;
	}
	
	public int aMethod(int val) {
		if(flag)
			return field;
		flag = true;
		int i = val;
		if(i % 2 == 0)
			field = val;
		i=field;
		return i;
	}

	public void defThenUseThenDefAgain(int val) {
		field = val;
		if(field % 13 == 0)
			field = 1;
	}
	
	public void useThenDef(int val) {
		if(field % 13 == 0)
			field = val;
	}
	
}
