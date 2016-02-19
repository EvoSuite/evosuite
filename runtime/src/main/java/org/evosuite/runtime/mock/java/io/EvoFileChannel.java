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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.evosuite.runtime.mock.java.lang.MockIllegalArgumentException;
import org.evosuite.runtime.vfs.VirtualFileSystem;


/**
 * This is not a mock of FileChannel, as FileChannel is an abstract class.
 * This class is never instantiated directly from the SUTs, but rather from mock classes (eg MockFileInputStream).
 * 
 * 
 * @author arcuri
 *
 */
public class EvoFileChannel extends FileChannel{  //FIXME mock FileChannel
	
	
	/**
	 * The read/write position in the channel
	 */
	private final AtomicInteger position;

	/**
	 * The absolute path of the file this channel is for
	 */
	private final String path;

	/**
	 * Can this channel be used for read operations?
	 */
	private final boolean isOpenForRead;

	/**
	 * Can this channel be used for write operations?
	 */
	private final boolean isOpenForWrite;

	/**
	 * Is this channel closed? Most functions throw an exception if the channel is closed.
	 * Once a channel is closed, it cannot be reopened
	 */
	private volatile boolean closed;


	private final Object readWriteMonitor = new Object();

	/**
	 * Main constructor
	 * 
	 * @param sharedPosition   the position in the channel, which should be shared with the stream this channel was generated from 
	 							(i.e., same instance reference) 
	 * @param path				full qualifying path the of the target file
	 * @param isOpenForRead
	 * @param isOpenForWrite
	 */
	protected EvoFileChannel(AtomicInteger sharedPosition, String path,
			boolean isOpenForRead, boolean isOpenForWrite) {
		super();
		this.position = sharedPosition;
		this.path = path;
		this.isOpenForRead = isOpenForRead;
		this.isOpenForWrite = isOpenForWrite;

		closed = false;
	}

	// -----  read --------

	@Override
	public int read(ByteBuffer dst) throws IOException {
		return read(new ByteBuffer[]{dst},0,1,position);
	}

	@Override
	public int read(ByteBuffer dst, long pos) throws IOException {

		if(pos < 0){
			throw new MockIllegalArgumentException("Negative position: "+pos);
		}

		AtomicInteger tmp = new AtomicInteger((int)pos);

		return read(new ByteBuffer[]{dst},0,1,tmp);
	}

	@Override
	public long read(ByteBuffer[] dsts, int offset, int length)
			throws IOException {
		return read(dsts,offset,length,position);
	}

	private int read(ByteBuffer[] dsts, int offset, int length, AtomicInteger posToUpdate)
			throws IOException {
		if(!isOpenForRead){
			throw new NonReadableChannelException();
		}

		throwExceptionIfClosed();

		int counter = 0;

		synchronized(readWriteMonitor){
			for(int j=offset; j<length; j++){
				ByteBuffer dst = dsts[j];
				int r = dst.remaining();
				for(int i=0; i<r; i++){
					int b = NativeMockedIO.read(path, posToUpdate);
					if(b < 0){ //end of stream
						return -1;
					}

					if(closed){
						throw new AsynchronousCloseException();
					}

					if(Thread.currentThread().isInterrupted()){
						close();
						throw new ClosedByInterruptException();
					}

					dst.put((byte)b);
					counter++;
				}
			}
		}

		return counter;		
	}


	// -------- write ----------

	@Override
	public int write(ByteBuffer src) throws IOException {		
		return write(new ByteBuffer[]{src},0,1,position);
	}


	@Override
	public int write(ByteBuffer src, long pos) throws IOException {

		if(pos < 0){
			throw new MockIllegalArgumentException("Negative position: "+pos);
		}

		AtomicInteger tmp = new AtomicInteger((int)pos);

		return write(new ByteBuffer[]{src},0,1,tmp);
	}


	@Override
	public long write(ByteBuffer[] srcs, int offset, int length)
			throws IOException {		
		return write(srcs,offset,length,position);
	}


	private int write(ByteBuffer[] srcs, int offset, int length, AtomicInteger posToUpdate)
			throws IOException {
		if(!isOpenForWrite){
			throw new NonWritableChannelException();
		}

		if( (offset < 0) || (offset > srcs.length) ||  (length < 0) || (length > srcs.length-offset) ){
			throw new IndexOutOfBoundsException();
		}

		throwExceptionIfClosed();

		int counter = 0;

		byte[] buffer = new byte[1];

		synchronized(readWriteMonitor){
			for(int j=offset; j<length; j++){
				ByteBuffer src = srcs[j];
				int r = src.remaining();
				for(int i=0; i<r; i++){
					byte b = src.get();
					buffer[0] = b;
					NativeMockedIO.writeBytes(path, posToUpdate, buffer, 0, 1);
					counter++;

					if(closed){
						throw new AsynchronousCloseException();
					}

					if(Thread.currentThread().isInterrupted()){
						close();
						throw new ClosedByInterruptException();
					}
				}
			}
		}

		return counter;		
	}

	@Override
	public FileChannel truncate(long size) throws IOException {
		throwExceptionIfClosed();

		if(size < 0){
			throw new MockIllegalArgumentException();
		}

		if(!isOpenForWrite){
			throw new NonWritableChannelException();
		}

		long currentSize = size();
		if(size < currentSize){
			NativeMockedIO.setLength(path, position, size);	
		}

		return this;
	}


	//------ others --------


	@Override
	public long position() throws IOException {		
		throwExceptionIfClosed();
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);
		return position.get();
	}

	@Override
	public FileChannel position(long newPosition) throws IOException {

		if(newPosition < 0){
			throw new MockIllegalArgumentException();
		}

		throwExceptionIfClosed();
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);
		
		position.set((int)newPosition);

		return this;
	}

	@Override
	public long size() throws IOException {
		throwExceptionIfClosed();		
		return NativeMockedIO.size(path);
	}



	@Override
	public void force(boolean metaData) throws IOException {
		throwExceptionIfClosed();
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);
		//nothing to do
	}

	@Override
	public long transferTo(long position, long count, WritableByteChannel target)
			throws IOException {
		// TODO 		
		throw new MockIOException("transferTo is not supported yet");
	}

	@Override
	public long transferFrom(ReadableByteChannel src, long position, long count)
			throws IOException {
		// TODO 
		throw new MockIOException("transferFrom is not supported yet");
	}


	@Override
	public MappedByteBuffer map(MapMode mode, long position, long size)
			throws IOException {
		// TODO 
		throw new MockIOException("MappedByteBuffer mocks are not supported yet");
	}

	@Override
	public FileLock lock(long position, long size, boolean shared)
			throws IOException {
		// TODO 
		throw new MockIOException("FileLock mocks are not supported yet");
	}

	@Override
	public FileLock tryLock(long position, long size, boolean shared)
			throws IOException {
		// TODO 
		throw new MockIOException("FileLock mocks are not supported yet");
	}

	@Override
	protected void implCloseChannel() throws IOException {
		closed = true;		
	}

	private void throwExceptionIfClosed() throws ClosedChannelException{
		if(closed){
			throw new ClosedChannelException();
		}
	}
}
