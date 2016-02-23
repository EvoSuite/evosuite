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
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.vfs.VirtualFileSystem;

public class MockFileWriter extends FileWriter  implements OverrideMock{

	/*
	 * This class is specular to MockFileReader
	 */

	private OutputStreamWriter stream;

	/*
	 *  -- constructors --------
	 */

	public MockFileWriter(String fileName) throws IOException {
		this(fileName != null ? 
				(!MockFramework.isEnabled() ? new File(fileName) : new MockFile(fileName)) : 
					null);
	}

	public MockFileWriter(String fileName, boolean append) throws IOException {
		this(fileName != null ? 
				(!MockFramework.isEnabled() ? new File(fileName) : new MockFile(fileName)) : 
					null, append);
	}

	public MockFileWriter(File file) throws IOException {
		this(file,false);
	}

	public MockFileWriter(File file, boolean append) throws IOException {
		super(!MockFramework.isEnabled() ? 
				file : 
					VirtualFileSystem.getInstance().getRealTmpFile(),
					append);

		if(!MockFramework.isEnabled()){
			return;
		}

		MockFileOutputStream mock = new MockFileOutputStream(file,append);

		stream = new OutputStreamWriter(mock);

		VirtualFileSystem.getInstance().addLeakingResource(mock);
	}

	// we do not handle this constructor
	public MockFileWriter(FileDescriptor fd) {
		super(fd);
	}



	// ---- methods from  OutputStreamWriter -----------

	@Override
	public String getEncoding() {
		if(!MockFramework.isEnabled()){
			return super.getEncoding();
		}

		return stream.getEncoding();
	}

	/*
	 * cannot override a package-level method...
	 * 
	 * but this is not a problem:
	 * 1) only called by PrintStream
	 * 2) anyway, the goal would be to mock all objects in
	 *    a package 
	 * 
    void flushBuffer() throws IOException {
        stream.flushBuffer();
    }
	 */

	@Override
	public void write(int c) throws IOException {
		if(!MockFramework.isEnabled()){
			super.write(c);
			return;
		}

		stream.write(c);
	}

	@Override
	public void write(char cbuf[], int off, int len) throws IOException {
		if(!MockFramework.isEnabled()){
			super.write(cbuf, off, len);
			return;
		}

		stream.write(cbuf, off, len);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		if(!MockFramework.isEnabled()){
			super.write(str, off, len);
			return;
		}
		stream.write(str, off, len);
	}

	@Override
	public void flush() throws IOException {
		if(!MockFramework.isEnabled()){
			super.flush();
			return;
		}
		stream.flush();
	}

	@Override
	public void close() throws IOException {
		if(!MockFramework.isEnabled()){
			super.close();
			return;
		}
		stream.close();
	}

}
