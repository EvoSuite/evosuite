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
public class TestClass {

	private int someField = 0;
	private int someOtherField = 0;
	private int yetAnotherField = 0;
	private int targetField = 0;
	private static int staticField = 0;
	
	// constructors
	
	public TestClass() {
	}
	
	public TestClass(int anInt) {
		targetField = anInt;
	}
	
	public TestClass(int anInt, int anotherInt) {
		targetField = anInt+anotherInt;
	}
	
	public TestClass(String aString) {
		targetField = (aString==null?-1:aString.length());
	}
	
	public TestClass(Object anObject) {
		targetField = (anObject==null?-1:(anObject.toString().length()));
	}
	
	
	// getter + setter

	public static int getStaticField() {
		return staticField;
	}
	
	public void setSomeField(int field) {
		someField = field;
	}
	
	public void setSomeOtherField(int field) {
		someOtherField = field;
	}
	
	public void setYetAnotherField(int field) {
		yetAnotherField = field;
	}
	
	public void setTargetField(int field) {
		targetField = field;
	}
	
	public int getSomeField() {
		return someField;
	}
	
	public int getSomeOtherField() {
		return someOtherField;
	}
	
	public int getYetAnotherField() {
		return yetAnotherField;
	}
	
	public int getTargetField() {
		return targetField;
	}
	
	public int getEmAll() {
		return someField+someOtherField+yetAnotherField+targetField+staticField;
	}
	
	// a little harder
	
	public void branchDef() {
	
		if(someField < 0) {
			targetField = -someField;
		}
	}
	
	public void branchUse() {
		if(someField < 0) {
			if(targetField>0) {
				// impossible to match this definition with the use of someField in the conditional in branchDef:
				//someField = targetField;
				
				// since i downt want to wait that long:
				someField = yetAnotherField;
			}
		}
	}
	
	// hard methods
	
	public void hard1() {
		
		if(someField>0) {
			targetField = 3; // Def
		}
		
		if(targetField == 0) { // Use
			yetAnotherField++;
		}
	}
	
	/**
	 * Introduces DefUse-Pairs that are not reachable (commented).
	 * Also supposed to show that it doesn't suffice to just reach 
	 * some specific branch in order to reach a Use.
	 */
	public void hard2() {
		
		targetField = someField;
		
		if(someField == 13) {
			targetField = 2; // Def
		}
		
		if(targetField != 2) {
			someField = targetField; // Use
		}
		
	}
	
	// mean methods
	
	/**
	 * Only two of the four introduced DefUse-Pairs are reachable
	 * Also the uses of targetField can never be paired with a def from another method
	 */
	public void mean1() {		
		
		if(someField<0) {
			targetField = 1; 
		} else {
			targetField = -1;
		}
		
		if(someField<0) {
			someField = targetField;
		} else {
			someField = -targetField;
		}
	}

	/**
	 * Supposed to show that you might have to follow an arbitrarily 
	 * complex path in order to not block the targetUse by another def.
	 * 
	 *  Or in other words: Satisfying a DefUse-Pair is as hard as satisfying a specific path.
	 */
	public void mean2() {
		
		if(someField == 0) {
			targetField = 1;
		} else {
			if(someOtherField % 13 == 0) {
				targetField = 2;
			} else {
				if(yetAnotherField != someField + someOtherField) {
					targetField = 3;
				}
			}
		}
		
		if(targetField == 0) { // target Use
			someField = 3;
		}
	}
}
