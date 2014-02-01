package org.evosuite.mock.java.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

public class MockPrintWriter extends PrintWriter{

	/*
	 * -- constructors from PrintWriter
	 *
	 *  Just need to replace FileOutputStream with
	 *  MockFileOutputStream in some of these constructors
	 */

	public MockPrintWriter (Writer out) {
		this(out, false);
	}

	public MockPrintWriter(Writer out,
			boolean autoFlush) {
		super(out,autoFlush);
	}

	public MockPrintWriter(OutputStream out) {
		this(out, false);
	}

	public MockPrintWriter(OutputStream out, boolean autoFlush) {
		super(out,autoFlush);
	}

	public MockPrintWriter(String fileName) throws FileNotFoundException {
		this(new BufferedWriter(new OutputStreamWriter(new MockFileOutputStream(fileName))),
				false);
	}

	/* Private constructor */
	private MockPrintWriter(Charset charset, File file)
			throws FileNotFoundException {
		this(new BufferedWriter(new OutputStreamWriter(new MockFileOutputStream(file), charset)),
				false);
	}

	public MockPrintWriter(String fileName, String csn)
			throws FileNotFoundException, UnsupportedEncodingException{
		this(toCharset(csn), new MockFile(fileName));
	}


	public MockPrintWriter(File file) throws FileNotFoundException {
		this(new BufferedWriter(new OutputStreamWriter(new MockFileOutputStream(file))),
				false);
	}

	public MockPrintWriter(File file, String csn)
			throws FileNotFoundException, UnsupportedEncodingException {
		this(toCharset(csn), file);
	}


	// -- private static methods  -------------

	private static Charset toCharset(String csn)
			throws UnsupportedEncodingException {
		// Objects.requireNonNull(csn, "charsetName");
		if(csn == null)
			throw new NullPointerException("charsetName");
		
		try {
			return Charset.forName(csn);
		} catch (IllegalCharsetNameException unused) {
			// UnsupportedEncodingException should be thrown
			throw new UnsupportedEncodingException(csn);
		} catch (UnsupportedCharsetException unused) {
			// UnsupportedEncodingException should be thrown
			throw new UnsupportedEncodingException(csn);
		}
	}

}
