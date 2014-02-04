package org.evosuite.mock.java.util;

@SuppressWarnings("deprecation")
public class MockDate extends java.util.Date {

	private static final long serialVersionUID = 6252798426594925071L;

	public MockDate() {
		super(org.evosuite.runtime.System.currentTimeMillis());
	}
	
	public MockDate(long time) {
		super(time);
	}
	
	public MockDate(int year, int month, int date) {
		super(year, month, date);
	}
	
	public MockDate(int year, int month, int date, int hrs, int min) {
		super(year, month, date, hrs, min);
	}
	
	public MockDate(int year, int month, int date, int hrs, int min, int sec) {
		super(year, month, date, hrs, min, sec);
	}
	
	public MockDate(String s) {
		super(s);
	}
	
	public static long parse(String s) {
		return java.util.Date.parse(s);
	}
	
	public static long UTC(int year,
            int month,
            int date,
            int hrs,
            int min,
            int sec) {
		return java.util.Date.UTC(year, month, date, hrs, min, sec);
	}
}
