package com.examples.with.different.packagename;


public class StatInitIssue {
	
	 private static final String name = StatInitIssue.class.getPackage().getName();
	 
	 public static void foo(){
		 System.out.println(name);
	 }
	 
	 public static void main(String[] args){
		 foo();
	 }
}
