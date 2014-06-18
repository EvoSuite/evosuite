package org.evosuite.testcarver.exception;

@SuppressWarnings("serial")
public final class CapturerException extends RuntimeException
{
	public CapturerException() {}
	
	public CapturerException(final String msg) 
	{
		super(msg);
	}
	
	public CapturerException(final Throwable t) 
	{
		super(t);
	}
	
	public CapturerException(final String msg, final Throwable t) 
	{
		super(msg, t);
	}
}
