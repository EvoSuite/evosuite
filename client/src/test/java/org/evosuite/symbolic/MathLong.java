package org.evosuite.symbolic;

public class MathLong {

	public long divide(long a, long b) {
		return a/b;
	}
	
	public long remainder(long a,long b) {
		return a%b;
	}
	
	public long multiply(long a, long b) {
		return a*b;
	}
	
	public long sum(long a, long b) {
		return a+b;
	}
	
	public long substract(long a, long b) {
		return a-b;
	}
	
	private Long f = new Long(3215155154115L);
	
	public void unreach() {
		if (f==null) {
			f = new Long(3215155154115L);
		}
	}
	
	public int castToInt(long f) {
		return (int)f;
	}
	
	public long castToLong(long f) {
		return (long)f;
	}
	
	public char castToChar(long f) {
	  return (char)f;
	}
	
	public short castToShort(long f) {
		  return (short)f;
	}
	
	public byte castToByte(long f) {
		  return (byte)f;
	}
	
	public float castToFloat(long f) {
		  return (float)f;
	}
	
	public long shiftLeft(long a, long b) {
		return a << b;
	}
	
	public long shiftRight(long a, long b) {
		return a >> b;
	}
	
	public long unsignedShiftRight(long a, long b) {
		return a >>> b;
	}
}
