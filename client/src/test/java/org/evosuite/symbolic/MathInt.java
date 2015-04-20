package org.evosuite.symbolic;

public class MathInt {

	public int divide(int a, int b) {
		return a/b;
	}
	
	public int remainder(int a,int b) {
		return a%b;
	}
	
	public int multiply(int a, int b) {
		return a*b;
	}
	
	public int sum(int a, int b) {
		return a+b;
	}
	
	public int substract(int a, int b) {
		return a-b;
	}
	
	private Object f = new Object();
	
	public void unreach() {
		if (f==null) {
			f = new Object();
		}
	}
	
	public int castToInt(int f) {
		return (int)f;
	}
	
	public long castToLong(int f) {
		return (long)f;
	}
	
	public char castToChar(int f) {
	  return (char)f;
	}
	
	public short castToShort(int f) {
		  return (short)f;
	}
	
	public short castToByte(int f) {
		  return (byte)f;
	}
	
	public int shiftLeft(int a, int b) {
		return a << b;
	}
	
	public int shiftRight(int a, int b) {
		return a >> b;
	}
	
	public int unsignedShiftRight(int a, int b) {
		return a >>> b;
	}
}
