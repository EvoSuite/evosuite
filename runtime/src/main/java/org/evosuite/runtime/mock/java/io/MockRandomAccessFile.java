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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.evosuite.runtime.LeakingResource;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.mock.java.lang.MockIllegalArgumentException;
import org.evosuite.runtime.mock.java.lang.MockNullPointerException;
import org.evosuite.runtime.vfs.VFile;
import org.evosuite.runtime.vfs.VirtualFileSystem;

public class MockRandomAccessFile extends RandomAccessFile implements LeakingResource,  OverrideMock{

	private FileChannel channel = null;
	private final Object closeLock = new Object();
	private boolean canRead;
	private boolean canWrite;

	private volatile boolean closed = false;


	/**
	 * The path to the file
	 */
	private final String path;
	
	/**
	 * The position in the file
	 */
	private final AtomicInteger position = new AtomicInteger(0);
	
	// ----------- constructors  ----------------

	public MockRandomAccessFile(String name, String mode)
			throws FileNotFoundException{
		this(name != null ? 
				(!MockFramework.isEnabled() ? 
						 new File(name) :
							 new MockFile(name)): 
				null, mode);
	}


	public MockRandomAccessFile(File file, String mode) throws FileNotFoundException {
		
		super((!MockFramework.isEnabled() ?
				file :
				VirtualFileSystem.getInstance().getRealTmpFile()),mode); //just to make the compiler happy

		if(!MockFramework.isEnabled()){
			path = null;
			return;
		}
		
		VirtualFileSystem.getInstance().addLeakingResource(this);
		
		String name = (file != null ? file.getPath() : null);
		
		if (mode==null || (!mode.equals("r") && !mode.equals("rw") && !mode.equals("rws") && !mode.equals("rwd")) ){
			throw new MockIllegalArgumentException("Illegal mode \"" + mode
					+ "\" must be one of "
					+ "\"r\", \"rw\", \"rws\","
					+ " or \"rwd\"");
		}
		
		canRead = mode.contains("r");
		assert canRead; // should always be readable
		canWrite = mode.contains("w");
		
		if (name == null) {
			throw new MockNullPointerException();
		}
				
		path = (file != null ? file.getAbsolutePath() : null);
		
		//does the file exist?
		boolean exist = VirtualFileSystem.getInstance().exists(path);
		if(!exist){
			if(!canWrite){
				throw new FileNotFoundException("File does not exist, and RandomAccessFile is not open in write mode");
			} else {
				//let's create it
				boolean created = VirtualFileSystem.getInstance().createFile(path);
				if(!created){
					throw new FileNotFoundException("Failed to create file");
				}
			}
		} else {
			//the file does exist, no need to do anything here
		}
		
		/*
		 * it is important to instantiate it here, because getChannel is final
		 */
		channel = new EvoFileChannel(position,path,canRead,canWrite); 		
	}

	
	// ------- mocked native methods ---------- 
	
	@Override
	public int read() throws IOException{
		if(!MockFramework.isEnabled()){
			return super.read();
		}

		if(closed){
			throw new MockIOException();
		}
		
		//no need to check canRead, as should be always true
		assert canRead;
		
		return NativeMockedIO.read(path, position); 
	}

	@Override
	public void write(int b) throws IOException{
		if(!MockFramework.isEnabled()){
			super.write(b);
			return;
		}

		writeBytes(new byte[]{(byte)b},0,1);
	}

	private void writeBytes(byte b[], int off, int len) throws IOException{
		
		if(closed || !canWrite){
			throw new IOException();
		}

		NativeMockedIO.writeBytes(path, position, b, off, len);
	}
	
	@Override
	public long getFilePointer() throws IOException{
		if(!MockFramework.isEnabled()){
			return super.getFilePointer();
		}

		return position.get();
	}

	@Override
	public void seek(long pos) throws IOException{
		if(!MockFramework.isEnabled()){
			super.seek(pos);
			return;
		}

		if(pos < 0){
			throw new MockIOException("Negative position: "+pos);
		}
		if(pos > Integer.MAX_VALUE){
			throw new MockIOException("Virtual file system does not handle files larger than  "+Integer.MAX_VALUE+" bytes");
		}
		position.set((int)pos);
	}

	@Override
	public long length() throws IOException{
		if(!MockFramework.isEnabled()){
			return super.length();
		}

		if(closed){
			throw new MockIOException();
		}
		return NativeMockedIO.size(path);
	}

	@Override
	public void setLength(long newLength) throws IOException{
		if(!MockFramework.isEnabled()){
			super.setLength(newLength);
			return;
		}

		if(closed || !canWrite){
			throw new IOException();
		}
		
		NativeMockedIO.setLength(path, position, newLength);	
	}

	
	
	// ---------   override methods ----------------
	
	private  int readBytes(byte b[], int off, int len) throws IOException{
		int counter = 0;
		for(int i=0; i<len; i++){
			int v = read();
			if(v == -1){  
				//end of stream
				return -1;
			}
			
			b[off+i] = (byte) v;
			counter++;
		}
		
		return counter; 
	}
	
	@Override
	public int read(byte b[], int off, int len) throws IOException {
		if(!MockFramework.isEnabled()){
			return super.read(b, off, len);
		}

		return readBytes(b, off, len); 
	}

	@Override
	public int read(byte b[]) throws IOException {
		if(!MockFramework.isEnabled()){
			return super.read(b);
		}

		return readBytes(b, 0, b.length); 
	}

	@Override
	public int skipBytes(int n) throws IOException {
		if(!MockFramework.isEnabled()){
			return super.skipBytes(n);
		}

		return super.skipBytes(n);
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
			return ;
		}

		writeBytes(b, off, len);
	}

	@Override
	public void close() throws IOException {
		if(!MockFramework.isEnabled()){
			super.close();
			return;
		}

		synchronized (closeLock) {
		
			super.close();
			
			if (closed) {
				return;
			}
			closed = true;
		}
		
		if (channel != null) {
			channel.close();
		}
		
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);
	}

	@Override
	public void release() throws Exception {		
			super.close();
	}
}
