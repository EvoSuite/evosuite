/**
 * 
 */
package com.examples.with.different.packagename;

/**
 * @author Gordon Fraser
 * 
 */
public class FactoryExample {

	public boolean setMe = false;
	
	public byte testByte(byte x, byte y) {
		return (byte) (x + y);
	}

	public static void testStatic() {
		// no-op
	}
}
