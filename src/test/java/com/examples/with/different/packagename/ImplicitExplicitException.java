package com.examples.with.different.packagename;

public class ImplicitExplicitException {

	public ImplicitExplicitException(){
		
	}
	
	public void implicit(Integer x){
		x.toString();
	}
	
	public void explicit(Integer x){
		if(x==null){
			throw new NullPointerException();
		}
		x.toString();
	}
	
	public void implicitDeclared(Integer x) throws NullPointerException{
		x.toString();
	}
	
	public void explicitDeclared(Integer x) throws NullPointerException{
		if(x==null){
			throw new NullPointerException();
		}
		x.toString();
	}
	
	public void shouldBeIgnored() throws IllegalArgumentException {
		throw new IllegalArgumentException();
	}
	
}
