package com.examples.with.different.packagename;

public class ImplicitExplicitException {

	public ImplicitExplicitException(){
		
	}
	
	public void implicit(Object x){
		x.toString();
	}
	
	public void explicit(Object x){
		if(x==null){
			throw new NullPointerException();
		}
		x.toString();
	}
	
	public void implicitDeclared(Object x) throws NullPointerException{
		x.toString();
	}
	
	public void explicitDeclared(Object x) throws NullPointerException{
		if(x==null){
			throw new NullPointerException();
		}
		x.toString();
	}
	
	public void shouldBeIgnored() throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}
	
}
