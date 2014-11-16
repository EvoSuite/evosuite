package com.examples.with.different.packagename.coverage;

public class MethodReturnsPrimitive {

	public boolean testBoolean(int x) {
		return (x > 0);
	}
	
    public int testInt(int x, int y) {
        return x + y;
    }

	public byte testByte(byte x, byte y) {
		return (byte) (x + y);
	}
	
	public long testLong(long x, long y) {
		return (long) (x - y);
	}
	
	public char testChar(int x, int y) {
		if (x == y) 
			return 'a';
		else if (x > y)
			return '1';
		else 
			return ' ';
	}
}
