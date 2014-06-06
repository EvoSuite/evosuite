package com.examples.with.different.packagename;

public class SimpleInteger {

	public int testInt(int x, int y) {
		return x + y;
	}
	
	public short testShort(short x, short y) {
		return (short) (x + y);
	}

	public byte testByte(byte x, byte y) {
		return (byte) (x + y);
	}

	public long testLong(long x, long y) {
		return (x + y);
	}

	public float testFloat(float x, float y) {
		return (x + y);
	}

	public double testDouble(double x, double y) {
		return x + y;
	}

	public char testChar(char x, char y) {
		return (char) (x + y);
	}

	public int[] testIntArray(int x, int y) {
		return new int[] {x, y};
	}

}
