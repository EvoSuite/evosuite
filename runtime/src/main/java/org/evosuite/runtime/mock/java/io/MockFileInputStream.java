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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.evosuite.runtime.LeakingResource;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.mock.java.lang.MockNullPointerException;
import org.evosuite.runtime.vfs.VFile;
import org.evosuite.runtime.vfs.VirtualFileSystem;

public class MockFileInputStream extends FileInputStream implements LeakingResource, OverrideMock{


	private FileChannel channel = null;

	/**
	 * Is this stream closed?
	 */
	private volatile boolean closed = false;

	/**
	 * The path to the file
	 */
	private final String path;

	/**
	 * The position to read in the stream next
	 */
	private final AtomicInteger position = new AtomicInteger(0);

	// ----- constructors -------------

	public MockFileInputStream(String name) throws FileNotFoundException {
		this( name != null ? 
						(!MockFramework.isEnabled() ? new File(name) : new MockFile(name)) : 
				(File)null
				);
	}

	
	public MockFileInputStream(File file) throws FileNotFoundException {
		super(!MockFramework.isEnabled() ?
						file :
							VirtualFileSystem.getInstance().getRealTmpFile()  // just to make compiler happy
				);
		if(!MockFramework.isEnabled()){
			path = null;
			return;
		}
		
		VirtualFileSystem.getInstance().addLeakingResource(this);

		path = (file != null ? file.getAbsolutePath() : null);
		if (path == null) {
			throw new MockNullPointerException();
		}

		VFile vf = NativeMockedIO.getFileForReading(path);
		if(vf==null){
			throw new FileNotFoundException();
		}
	}

	/*
	 * we do not really handle this constructor.
	 * 
	 * TODO: we could, by using StaticReplacementMock and reflection, but anyway it is very rare
	 */
	public MockFileInputStream(FileDescriptor fdObj) {
		super(fdObj);
		path = "";
	}

	// ----  read methods  ----------

	public int read() throws IOException{

		if(!MockFramework.isEnabled()){
			return super.read();
		}
		
		throwExceptionIfClosed();

		return NativeMockedIO.read(path, position); 
	}

	private  int readBytes(byte[] b, int off, int len) throws IOException{

		if(!MockFramework.isEnabled()){
			return super.read(b, off, len);
		}
		
		int counter = 0;
		for(int i=0; i<len; i++){
			int v = read();
			if(v == -1){  //EOF
				if(i==0){
					//no data to read
					return -1;
				} else {
					return counter;
				}
			}

			b[off+i] = (byte) v;
			counter++;
		}

		return counter; 
	}

	@Override
	public  long skip(long n) throws IOException{
		
		if(!MockFramework.isEnabled()){
			return super.skip(n);
		}
		
		if(n<0){
			throw new MockIOException();
		}

		throwExceptionIfClosed();

		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);
		position.addAndGet((int)n);
		return n; 
	}

	@Override
	public int available() throws IOException{

		if(!MockFramework.isEnabled()){
			return super.available();
		}
		
		throwExceptionIfClosed();

		VFile vf = NativeMockedIO.getFileForReading(path);
		if(vf==null){
			throw new MockIOException();
		}

		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);

		int size = vf.getDataSize();
		int available = size - position.get();

		return available; 
	}

	@Override
	public int read(byte[] b) throws IOException {
		return readBytes(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return readBytes(b, off, len);
	}


	//-------- other methods ------------

	@Override
	public void close() throws IOException {
		super.close();

		if(!MockFramework.isEnabled()){
			return;
		}
		
		if (closed) {
			return;
		}
		closed = true;
		if (channel != null) {
			channel.close();
		}

		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);
	}

	/*  //Cannot be overriden
    public final FileDescriptor getFD() throws IOException {
        if (fd != null) return fd;
        throw new IOException();
    }
	 */

	@Override
	public FileChannel getChannel() {
		if(!MockFramework.isEnabled()){
			return super.getChannel();
		}
		
		synchronized (this) {
			if (channel == null) {
				channel = new EvoFileChannel(position,path,true,false); 
			}
			return channel;
		}
	}

	private void throwExceptionIfClosed() throws IOException{
		if(closed){
			throw new MockIOException();
		}
	}

	@Override
	public void release() throws Exception {		
		super.close();
	}
}
