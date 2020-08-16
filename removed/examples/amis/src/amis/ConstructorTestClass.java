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
public class ConstructorTestClass extends BaseConstructorTestClass {

	private int targetField = 0;
	
	// constructors
	
	public ConstructorTestClass() {
	}
	
	public ConstructorTestClass(int anInt) {
		super(anInt);
	}
	
	public ConstructorTestClass(int anInt, int anotherInt) {
		super(anInt+anotherInt);
		
		targetField = 1;
		new Object();
//		setTargetField(imPrivate());
		setTargetField(targetField);
		new ConstructorTestClass(3);
	}
	
	public ConstructorTestClass(String s) {
		super(s==null?-1:s.length());
	}
	
//	
//	public ConstructorTestClass(int anInt, int anotherInt, int andAnotherInt) {
//		targetField = anInt+anotherInt+andAnotherInt;
//	}
//	
//	public ConstructorTestClass(String aString) {
//		targetField = aString.length();
//	}
//
//	public int withinPairsTest() {
//		targetField = 1;
//		targetField++;
//		
//		setsetTargetField(targetField);
//		
//		return targetField;
//	}
	
	public void setTargetField(int field) {
		targetField = field;
//		new ConstructorTestClass();
	}
	
//	public int setsetMethod() {
//		setsetTargetField(3);
//		return targetField;
//	}
//	
//	public void setsetTargetField(int field) {
//		setTargetField(field);
//	}
	
	public int getTargetField() {
		return targetField;
	}
//	private int imPrivate() {
//		return 7;
//	}
	
}
