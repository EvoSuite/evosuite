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
package com.examples.with.different.packagename.concolic;

public class MathDouble {

	public double divide(double a, double b) {
		return a/b;
	}
	
	public double remainder(double a,double b) {
		return a%b;
	}
	
	public double multiply(double a, double b) {
		return a*b;
	}
	
	public double sum(double a, double b) {
		return a+b;
	}
	
	public double substract(double a, double b) {
		return a-b;
	}
	
	private Double f = new Double(3.1416);
	
	public void unreach() {
		if (f==null) {
			f = new Double(1.5);
		}
	}
	
	public int castToInt(double f) {
		return (int)f;
	}
	
	public long castToLong(double f) {
		return (long)f;
	}
	
	public char castToChar(double f) {
	  return (char)f;
	}
	
	public short castToShort(double f) {
		  return (short)f;
	}
	
	public byte castToByte(double f) {
		  return (byte)f;
	}
	
	public float castToFloat(double f) {
		  return (float)f;
	}

}
