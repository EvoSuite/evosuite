package org.evosuite.mock.java.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * This is not a mock of FileChannel, as FileChannel is an abstract class.
 * This class is never instantiated directly from the SUTs, but rather from mock classes (eg MockFileInputStream).
 * 
 * <p>
 * FIXME need to support blocking operations
 * 
 * @author arcuri
 *
 */
public class EvoFileChannel extends FileChannel{

	private final AtomicInteger position;
	private final String path;
	private final boolean isOpenForRead;
	private final boolean isOpenForWrite;


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
	}

	// -----  read --------
	
	@Override
	public int read(ByteBuffer dst) throws IOException {
		return read(new ByteBuffer[]{dst},0,1,position);
	}

	@Override
	public int read(ByteBuffer dst, long pos) throws IOException {

		if(pos < 0){
			throw new IllegalArgumentException("Negative position: "+pos);
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

		//TODO close

		int counter = 0;

		for(int j=offset; j<length; j++){
			ByteBuffer dst = dsts[j];
			int r = dst.remaining();
			for(int i=0; i<r; i++){
				int b = MockNative.read(path, posToUpdate);
				if(b < 0){ //end of stream
					return -1;
				}

				dst.put((byte)b);
				counter++;
			}
		}
		
		return counter;		
	}


	// -------- write ----------

	@Override
	public int write(ByteBuffer src) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long write(ByteBuffer[] srcs, int offset, int length)
			throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int write(ByteBuffer src, long position) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	//------ others --------
	
	
	@Override
	public long position() throws IOException {		
		return position.get();
	}

	@Override
	public FileChannel position(long newPosition) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long size() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FileChannel truncate(long size) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void force(boolean metaData) throws IOException {
		//nothing to do
	}

	@Override
	public long transferTo(long position, long count, WritableByteChannel target)
			throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long transferFrom(ReadableByteChannel src, long position, long count)
			throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public MappedByteBuffer map(MapMode mode, long position, long size)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileLock lock(long position, long size, boolean shared)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileLock tryLock(long position, long size, boolean shared)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void implCloseChannel() throws IOException {
		// TODO Auto-generated method stub		
	}

}
