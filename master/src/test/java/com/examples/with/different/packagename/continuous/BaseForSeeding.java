package com.examples.with.different.packagename.continuous;

public class BaseForSeeding {

	public void directInput(NoBranches obj){			
		//used inside, but not as input
		MoreBranches mb = new MoreBranches();
		mb.foo(0);
	}
	
	public void interfaceAsInput(SomeInterface si){		
	}
	
	public void usingCast(Object obj){
		
		SomeBranches sb = (SomeBranches) obj;
		sb.foo(0);
	}
	
	public void usingCastOnAbstract(Object obj){
	
		OnlyAbstract oa = (OnlyAbstract) obj;
		oa.foo();
	}
}
