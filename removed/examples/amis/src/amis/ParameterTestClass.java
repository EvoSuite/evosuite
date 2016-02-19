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
public class ParameterTestClass {

	private int field;
	
	public ParameterTestClass(int anInt) {
		
		field = anInt;
	}
	
	public int aMethod(int param1, int param2, int param3) {
		
		if(field%2 == 0) {
			field = param1+param2;
		} else {
			field = param2*param3;
		}
		
		return param1+param2+param3 - field;
	}
	
	public void anotherMethod(int param1, int param2, String param3, String param4) {
		
		if(param3 == null || param4 == null)
			return;
		
		if(param3.length() != 0)
			field = param3.length();
		else if(param4.length() != 0)
			field = param4.length();
		else 
			field = param1+param2;
	}
	
	public void stringNullMethod(String aString) {
		if(aString == null) // TODO EvoSuite seems not to give null as an argument
			field = 0;
	}
	
	public void objectNullMethod(Object o) {
		if(o==null)
			field = 0;
	}
	
	public void setField(int val) {
		field = val;
	}
	
	public int getField() {
		return field;
	}
	
}
