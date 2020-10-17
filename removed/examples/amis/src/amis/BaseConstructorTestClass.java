/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
public class BaseConstructorTestClass {

	private int targetField = 0;
	
	// constructors

	public BaseConstructorTestClass() {
        }

	
	public BaseConstructorTestClass(int anInt) {
		targetField = anInt;
	}
	
	public BaseConstructorTestClass(int anInt, int anotherInt) {
		this(anInt+anotherInt);
		
		targetField = 1;
		new Object();
		setTargetField(imPrivate());
		targetField=-1;
		new BaseConstructorTestClass(3);
	}
////	
////	public BaseConstructorTestClass(int anInt, int anotherInt, int andAnotherInt) {
////		targetField = anInt+anotherInt+andAnotherInt;
////	}
////	
//	public BaseConstructorTestClass(String aString) {
//		this(aString.length()%2==0?-1:aString.length());
//	}
//	
//	public BaseConstructorTestClass(int i, int j) {
//		this(new Object() == null ? i : j);
//	}
////
	public void setTargetField(int field) {
		targetField = field;
	}
	public int getTargetField() {
		return targetField;
	}
	private int imPrivate() {
		return 7;
	}
	
}
