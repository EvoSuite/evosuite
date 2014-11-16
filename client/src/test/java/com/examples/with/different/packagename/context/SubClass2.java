package com.examples.with.different.packagename.context;

public class SubClass2 extends ISubClass{

	/**
	 * 
	 * 	1 double example(int x, int y, double z) {
		2 boolean flag = y > 1000;
		3 // ...
		4 if(x + y == 1024)
		5 if(flag)
		6 if(Math.cos(z)−0.95 < Math.exp(z))
		7 // target branch
		8 // ...
		9 }
	 * 
	 */
	
 	
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
