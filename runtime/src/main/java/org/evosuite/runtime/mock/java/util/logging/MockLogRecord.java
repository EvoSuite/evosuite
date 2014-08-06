package org.evosuite.runtime.mock.java.util.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.evosuite.runtime.mock.OverrideMock;

public class MockLogRecord extends LogRecord  implements OverrideMock{

	public MockLogRecord(Level level, String msg) {
		super(level, msg);
		setMillis(org.evosuite.runtime.System.currentTimeMillis());
		setSequenceNumber(0L);
		setThreadID(0);
	}

	private static final long serialVersionUID = -1511890873640420434L;

}
