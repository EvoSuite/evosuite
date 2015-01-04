package com.examples.with.different.packagename.agent;

import org.evosuite.runtime.agent.InstrumentingAgent_exceptionsIT;

public class ExceptionHolder extends Exception{

	private static final long serialVersionUID = -2460331308621553548L;

	
	public static class StaticPublicException extends Exception{
		private static final long serialVersionUID = -3176736235485114939L;		
	}
	
	
	public Throwable getMockedThrowable(){
		return new Throwable();
	}
	
	
	public NullPointerException getNonMockedNPE(){
		return InstrumentingAgent_exceptionsIT.getNPE();
	}
	
	
	public StackTraceElement[] getTracesWhenCast(){		
		Exception exception = (Exception) getNonMockedNPE();		
		return exception.getStackTrace();
	}
	
	
	public StackTraceElement[] getTraces(){
		return getNonMockedNPE().getStackTrace();
	}
}
