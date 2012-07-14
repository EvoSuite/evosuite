package de.unisb.cs.st.testcarver.codegen;

import de.unisb.cs.st.testcarver.capture.CaptureLog;

public interface ICaptureLogAnalyzer
{
	public void analyze(final CaptureLog log, @SuppressWarnings("rawtypes") ICodeGenerator generator, final Class<?>...observedClasses);
}
