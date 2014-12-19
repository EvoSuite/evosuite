package com.examples.with.different.packagename.context.complex;

public class SubClass2 extends ISubClass{
 
 	
	public boolean checkFiftneen(int i){
		boolean bol = bla(i);
		if(bol)
			return true;
		return false;
	}
	

	
	private boolean bla(int i){
		boolean bol = false;
		if(i*2==6){
			bol = true;
		}
		return bol;
	}
	
}
