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
package com.examples.with.different.packagename.context;

public class SubClass2 extends ISubClass{

	/**
	 * 
	 * 	1 double example(int x, int y, double z) {
		2 boolean flag = y > 1000;
		3 // ...
		4 if(x + y == 1024)
		5 if(flag)
		6 if(Math.cos(z)−0.95 < Math.exp(z))
		7 // target branch
		8 // ...
		9 }
	 * 
	 */
	
 	
	public boolean checkFiftneen(int i){
		boolean bol = bla(i);
		if(bol)
			return true;
		return false;
	}
	

	
	private boolean bla(int i){
		boolean bol = false;
		if(i*2==6){
			bol = true;
		}
		return bol;
	}
	
}
