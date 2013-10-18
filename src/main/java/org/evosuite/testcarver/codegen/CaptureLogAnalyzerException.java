package org.evosuite.testcarver.codegen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaptureLogAnalyzerException extends RuntimeException{
	
	private static final long serialVersionUID = 4585552843370187739L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CaptureLogAnalyzerException.class);
	
	public CaptureLogAnalyzerException(final String msg)
	{
		super(msg);
	}
	
	public CaptureLogAnalyzerException(final Throwable e)
	{
		super(e);
	}

	public static void check(final boolean expr, final String msg, final Object...msgArgs)
	throws CaptureLogAnalyzerException
	{
		if(! expr)
		{
			final String finalMsg = String.format(msg, msgArgs);
			LOGGER.info(finalMsg);
			throw new CaptureLogAnalyzerException(finalMsg);
		}
	}
	
	
	public static void propagateError(final Throwable t, final String msg, final Object...msgArgs)
    throws CaptureLogAnalyzerException
	{
		final String finalMsg = String.format(msg, msgArgs);
		LOGGER.info(finalMsg, t);
		throw new CaptureLogAnalyzerException(finalMsg);
	}
}
