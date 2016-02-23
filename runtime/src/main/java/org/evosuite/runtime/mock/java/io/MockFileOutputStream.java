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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.evosuite.runtime.LeakingResource;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.vfs.FSObject;
import org.evosuite.runtime.vfs.VFile;
import org.evosuite.runtime.vfs.VirtualFileSystem;

public class MockFileOutputStream extends FileOutputStream implements LeakingResource , OverrideMock{

	/**
	 * The path to the file
	 */
	private final String path;

	/**
	 * The associated channel, initialized lazily.
	 */
	private FileChannel channel;

	private volatile boolean closed = false;

	/**
	 * The position to write in the stream next
	 */
	private final AtomicInteger position = new AtomicInteger(0);

	//-------- constructors  ----------------

	public MockFileOutputStream(String name) throws FileNotFoundException {
		this(name != null ? 
				(!MockFramework.isEnabled() ? new File(name) : new MockFile(name) ): 
					null, 
					false);
	}


	public MockFileOutputStream(String name, boolean append) throws FileNotFoundException {
		this(name != null ? 
				(!MockFramework.isEnabled() ? new File(name) : new MockFile(name)) : 
					null, 
					append);
	}


	public MockFileOutputStream(File file) throws FileNotFoundException {
		this(file, false);
	}


	public MockFileOutputStream(File file, boolean append) throws FileNotFoundException{

		super(!MockFramework.isEnabled() ? 
				file : 
					VirtualFileSystem.getInstance().getRealTmpFile(),
					append); //just to make the compiler happy

		if(!MockFramework.isEnabled()){
			path = null;
			return;
		}

		VirtualFileSystem.getInstance().addLeakingResource(this);

		path = (file != null ? file.getAbsolutePath() : null);

		FSObject target = VirtualFileSystem.getInstance().findFSObject(path);
		if(target==null){
			boolean created = VirtualFileSystem.getInstance().createFile(path);
			if(!created){
				throw new FileNotFoundException();
			}
			target = VirtualFileSystem.getInstance().findFSObject(path);
		}
		if(target==null || target.isDeleted() || target.isFolder() || !target.isWritePermission()){
			throw new FileNotFoundException();
		}

		if(!append){
			((VFile)target).eraseData();
		}
	}

	// we do not really handle this constructor, but anyway FileDescriptor is rare
	public MockFileOutputStream(FileDescriptor fdObj) {
		super(fdObj);
		this.path = "";
	}


	//----------  write methods  --------------



	private void writeBytes(byte b[], int off, int len)
			throws IOException{

		throwExceptionIfClosed();

		NativeMockedIO.writeBytes(path, position, b, off, len);
	}

	@Override
	public void write(int b) throws IOException {

		if(!MockFramework.isEnabled()){
			super.write(b);
			return;
		}

		write(new byte[]{(byte)b},0,1);
	}

	@Override
	public void write(byte b[]) throws IOException {
		if(!MockFramework.isEnabled()){
			super.write(b);
			return;
		}
		writeBytes(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		if(!MockFramework.isEnabled()){
			super.write(b, off, len);
			return;
		}

		writeBytes(b, off, len);
	}


	//-----  other methods ------

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

	/* 
	 * it is final, so cannot do anything about it :(
	 * apart from bytecode instrumentation replacement. 
	 * 
	 * but it seems called only 5 times in all SF110, on which
	 * sync() is directly called. So does not really seem to be
	 * any reason to mock it
	 */
	/*
     public final FileDescriptor getFD()  throws IOException {
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
				channel = new EvoFileChannel(position,path,false,true);  
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
		if(!MockFramework.isEnabled()){			
			return;
		}

		super.close();
	}
}
