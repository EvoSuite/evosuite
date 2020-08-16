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
public class MeanTestClass {

	private int someField = 0;
	private int someOtherField = 0;
	private int yetAnotherField = 0;
	private int targetField = 0;
	
	private static boolean didIt = false;
	
	// constructors
	
	public MeanTestClass() {
	}
	
	// target method
	
	/**
	 * Supposed to show that you might have to follow an arbitrarily 
	 * complex path in order to not block the targetUse by another def.
	 * 
	 *  Or in other words: Satisfying a DefUse-Pair is as hard as satisfying a specific path.
	 *  
	 *  ... turns out this also shows that covering a certain BranchCoverageGoal can be that hard :D
	 */
	public void mean() {

		if(someField == 0) {
			targetField = 1;
		} else {
			if(someOtherField == 0) {
				targetField = 2;
			} else {
				// the following two uses for someField and someOtherField can 
				// not be paired with their definitions in the constructor
				if((yetAnotherField != someField + someOtherField) || yetAnotherField == 0) {
					targetField = 3;
				}
			}
		}
		
		if(targetField == 0) { // target Use
			someField = 3;
			if(!didIt)
				System.out.println("Tests covered the mean DUPair!");
			didIt = true;
		}
	}
	
	// aux methods
	
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
}
