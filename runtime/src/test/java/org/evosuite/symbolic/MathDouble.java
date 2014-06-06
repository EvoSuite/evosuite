package org.evosuite.symbolic;

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
