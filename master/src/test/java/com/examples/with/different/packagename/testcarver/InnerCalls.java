package com.examples.with.different.packagename.testcarver;

public class InnerCalls {

	public void printA(){
		System.out.println("A");
	}
	
	public void printB(){
		System.out.println("B");
	}
	
	public void printAandB(){
		printA();
		printB();
	}
}
