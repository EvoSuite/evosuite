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
package org.evosuite.eclipse.replace;

public class TypeMatcher {
	
	public int RewardChooser(String originalType, String replaceType){
		int reward=5;
		if(originalType.equals(replaceType)){
			if((originalType.equals("literal") && replaceType.equals("var")) ||
					(originalType.equals("var") && replaceType.equals("literal")))
				reward= -3;
			else
				reward= -5;

				
		}
		return reward;
	}
	
	
}
