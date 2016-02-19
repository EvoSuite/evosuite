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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;

public class MockPrintStream extends PrintStream  implements OverrideMock{


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
		this(!MockFramework.isEnabled() ?
				new FileOutputStream(fileName) : 
					new MockFileOutputStream(fileName));
	}

	public MockPrintStream(String fileName, String csn)
			throws FileNotFoundException, UnsupportedEncodingException {
		this( (!MockFramework.isEnabled() ?
				new FileOutputStream(fileName) : 
					new MockFileOutputStream(fileName))
					,false,csn);
	}

	public MockPrintStream(File file) throws FileNotFoundException {
		this(!MockFramework.isEnabled() ?
				new FileOutputStream(file) : 
					new MockFileOutputStream(file));
	}

	public MockPrintStream(File file, String csn)
			throws FileNotFoundException, UnsupportedEncodingException {
		// ensure charset is checked before the file is opened
		this((!MockFramework.isEnabled() ?
				new FileOutputStream(file) : 
					new MockFileOutputStream(file))
					,false,csn);
	}
}
