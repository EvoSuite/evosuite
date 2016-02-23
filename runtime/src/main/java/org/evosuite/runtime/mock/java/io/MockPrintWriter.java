/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.mock.java.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.mock.java.lang.MockNullPointerException;

public class MockPrintWriter extends PrintWriter  implements OverrideMock{

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
		this(new BufferedWriter(new OutputStreamWriter(
				(!MockFramework.isEnabled() ? 
						new FileOutputStream(fileName) :
							new MockFileOutputStream(fileName))
				)),
				false);
	}

	/* Private constructor */
	private MockPrintWriter(Charset charset, File file)
			throws FileNotFoundException {
		this(new BufferedWriter(new OutputStreamWriter(
				(!MockFramework.isEnabled() ? 
						new FileOutputStream(file) :
							new MockFileOutputStream(file))
							, charset)),
							false);
	}

	public MockPrintWriter(String fileName, String csn)
			throws FileNotFoundException, UnsupportedEncodingException{
		this(toCharset(csn), 
				(!MockFramework.isEnabled() ? 
						new File(fileName) :
							new MockFile(fileName))
				);
	}


	public MockPrintWriter(File file) throws FileNotFoundException {
		this(new BufferedWriter(new OutputStreamWriter(
				(!MockFramework.isEnabled() ? 
						new FileOutputStream(file) :
							new MockFileOutputStream(file))
				)),
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
			throw new MockNullPointerException("charsetName");

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
