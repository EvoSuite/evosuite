package org.evosuite.symbolic;

public class MathFloat {

	public float divide(float a, float b) {
		return a/b;
	}
	
	public float remainder(float a,float b) {
		return a%b;
	}
	
	public float multiply(float a, float b) {
		return a*b;
	}
	
	public float sum(float a, float b) {
		return a+b;
	}
	
	public float substract(float a, float b) {
		return a-b;
	}
	
	private Float f = new Float(3.1416);
	
	public void unreach() {
		if (f==null) {
			f = new Float(1.5);
		}
	}
	
	public int castToInt(float f) {
		return (int)f;
	}
	
	public long castToLong(float f) {
		return (long)f;
	}
	
	public char castToChar(float f) {
	  return (char)f;
	}
	
	public short castToShort(float f) {
		  return (short)f;
	}
	
	public short castToByte(float f) {
		  return (byte)f;
	}
}
