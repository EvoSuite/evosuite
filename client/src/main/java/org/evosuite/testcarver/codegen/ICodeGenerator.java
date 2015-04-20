package org.evosuite.testcarver.codegen;



import org.evosuite.testcarver.capture.CaptureLog;


public interface ICodeGenerator<T> {
	
	public void before(final CaptureLog log);
	
	public void createFieldReadAccessStmt(final CaptureLog log, final int currentLogRecNo);
	public void createFieldWriteAccessStmt(final CaptureLog log, final int currentLogRecNo);
	public void createMethodCallStmt(final CaptureLog log, final int currentLogRecNo);
	public void createPlainInitStmt(final CaptureLog log, final int currentLogRecNo);
	public void createUnobservedInitStmt(final CaptureLog log, final int currentLogRecNo);

	public void createArrayInitStmt(final CaptureLog log, final int currentLogRecNo);
	public void createCollectionInitStmt(final CaptureLog log, final int currentLogRecNo);
	public void createMapInitStmt(final CaptureLog log, final int currentLogRecNo);

	public boolean isMaximumLengthReached();
	
	public void after(final CaptureLog log);
	
	public T getCode();
	
	public void clear();

}
