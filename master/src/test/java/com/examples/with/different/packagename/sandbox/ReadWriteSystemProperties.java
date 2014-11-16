package com.examples.with.different.packagename.sandbox;

public class ReadWriteSystemProperties {

	public static final String A_PROPERTY = "a property with a ridiculosly long value and different characters $#%@!*$";
	public static final String USER_DIR = "user.dir";
	
	public boolean foo(String s){
		
		String dir = System.getProperty(USER_DIR);
		System.setProperty(USER_DIR, A_PROPERTY);//any value here would do
		System.setProperty(A_PROPERTY, dir);
		String readBack = System.getProperty(A_PROPERTY);
		
		if(readBack.equals(s)){
			return true;
		} else {
			return false;
		}
	}
}
