package com.examples.with.different.packagename;

public class ImplicitExplicitException {

	public ImplicitExplicitException(){
		
	}
	
	public void implicit(String x){
		x.toString();
	}
	
	public void explicit(String x){
		if(x==null){
			throw new NullPointerException();
		}
		x.toString();
	}
	
	public void implicitDeclared(String x) throws NullPointerException{
		x.toString();
	}
	
	public void explicitDeclared(String x) throws NullPointerException{
		if(x==null){
			throw new NullPointerException();
		}
		x.toString();
	}
	
	public void shouldBeIgnored() throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}
	
}
