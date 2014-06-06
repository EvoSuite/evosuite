package org.evosuite.mock.java.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class MockPrintStream extends PrintStream{

	
	/* -- constructors  from PrintStream ----------
	 * 
	 * here, we just need to replace FileOutputStream with
	 * MockFileOutputStream.
	 * This is simple as PrintStream does have a constructor
	 * that takes as input an OutputStream
	 */
	
    public MockPrintStream(OutputStream out) {
        super(out);
    }

    public MockPrintStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public MockPrintStream(OutputStream out, boolean autoFlush, String encoding)
        throws UnsupportedEncodingException{
    		super(out,autoFlush,encoding);
    }

    public MockPrintStream(String fileName) throws FileNotFoundException {
    		this(new MockFileOutputStream(fileName));
    }

    public MockPrintStream(String fileName, String csn)
        throws FileNotFoundException, UnsupportedEncodingException {
        this(new MockFileOutputStream(fileName),false,csn);
    }

    public MockPrintStream(File file) throws FileNotFoundException {
        this(new MockFileOutputStream(file));
    }

    public MockPrintStream(File file, String csn)
        throws FileNotFoundException, UnsupportedEncodingException {
        // ensure charset is checked before the file is opened
        this(new MockFileOutputStream(file),false,csn);
    }
}
