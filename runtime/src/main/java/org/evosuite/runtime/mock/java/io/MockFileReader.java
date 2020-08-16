/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.vfs.VirtualFileSystem;

public class MockFileReader extends FileReader  implements OverrideMock{

	/**
	 * As all the constructors of FileReader instantiate
	 * a FileInputStream, none of them can be used to create
	 * a usable instance of MockFileReader.
	 * So, we need to create an instance of MockFileInputStream,
	 * a redirect all calls of MockFileReader to this stream 
	 */
	private InputStreamReader stream;
		
	/*
	 * -- constructors ----------------------
	 * 
	 * FileReader defines only constructors, no methods
	 */
	
    public MockFileReader(String fileName) throws FileNotFoundException {
    		this(fileName != null ? 
    				(!MockFramework.isEnabled() ? new File(fileName) : new MockFile(fileName) ): 
    				null
    			);
    }

    public MockFileReader(File file) throws FileNotFoundException {
		super(!MockFramework.isEnabled() ?
				file : 
				VirtualFileSystem.getInstance().getRealTmpFile()); // just to make compiler happy
		
		if(!MockFramework.isEnabled()){
			return;
		}
		
		MockFileInputStream mock = new MockFileInputStream(file);
		
		stream = new InputStreamReader(mock);
		
		VirtualFileSystem.getInstance().addLeakingResource(mock);
    }

    //we do not really handle this constructor
    public MockFileReader(FileDescriptor fd) {
        super(fd);
    }

    //-- methods from InputStreamReader -------------

    @Override
    public String getEncoding() {
        return super.getEncoding();
    }

    @Override
    public int read() throws IOException {
		if(!MockFramework.isEnabled()){
			return super.read();
		}
		
    		return stream.read();
    }

    @Override
    public int read(char[] cbuf, int offset, int length) throws IOException {
		if(!MockFramework.isEnabled()){
			return super.read(cbuf, offset, length);
		}
        return stream.read(cbuf, offset, length);
    }

    @Override
    public boolean ready() throws IOException {
		if(!MockFramework.isEnabled()){
			return super.ready();
		}
		return stream.ready();
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
