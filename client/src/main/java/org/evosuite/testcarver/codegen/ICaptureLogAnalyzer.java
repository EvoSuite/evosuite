package org.evosuite.testcarver.codegen;

import org.evosuite.testcarver.capture.CaptureLog;

public interface ICaptureLogAnalyzer
{
	public void analyze(final CaptureLog log, @SuppressWarnings("rawtypes") ICodeGenerator generator, final Class<?>...observedClasses);
}
