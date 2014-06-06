package org.evosuite.mock.java.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.evosuite.runtime.LeakingResource;
import org.evosuite.runtime.VirtualFileSystem;
import org.evosuite.runtime.vfs.VFile;

public class MockRandomAccessFile extends RandomAccessFile implements LeakingResource{

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
		this(name != null ? new File(name) : null, mode);
	}


	public MockRandomAccessFile(File file, String mode) throws FileNotFoundException {
		
		super(VirtualFileSystem.getInstance().getRealTmpFile(),"rw"); //just to make the compiler happy

		VirtualFileSystem.getInstance().addLeakingResource(this);
		
		String name = (file != null ? file.getPath() : null);
		
		if (mode==null || (!mode.equals("r") && !mode.equals("rw") && !mode.equals("rws") && !mode.equals("rwd")) ){
			throw new IllegalArgumentException("Illegal mode \"" + mode
					+ "\" must be one of "
					+ "\"r\", \"rw\", \"rws\","
					+ " or \"rwd\"");
		}
		
		canRead = mode.contains("r");
		assert canRead; // should always be readable
		canWrite = mode.contains("w");
		
		if (name == null) {
			throw new NullPointerException();
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
		if(closed){
			throw new IOException();
		}
		
		//no need to check canRead, as should be always true
		assert canRead;
		
		return MockNative.read(path, position); 
	}

	@Override
	public void write(int b) throws IOException{
		writeBytes(new byte[]{(byte)b},0,1);
	}

	private void writeBytes(byte b[], int off, int len) throws IOException{
		
		if(closed || !canWrite){
			throw new IOException();
		}

		MockNative.writeBytes(path, position, b, off, len);
	}
	
	@Override
	public long getFilePointer() throws IOException{
		return position.get();
	}

	@Override
	public void seek(long pos) throws IOException{
		if(pos < 0){
			throw new IOException("Negative position: "+pos);
		}
		if(pos > Integer.MAX_VALUE){
			throw new IOException("Virtual file system does not handle files larger than  "+Integer.MAX_VALUE+" bytes");
		}
		position.set((int)pos);
	}

	@Override
	public long length() throws IOException{
		if(closed){
			throw new IOException();
		}
		return MockNative.size(path);
	}

	@Override
	public void setLength(long newLength) throws IOException{
		
		if(closed || !canWrite){
			throw new IOException();
		}
		
		MockNative.setLength(path, position, newLength);	
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
		return readBytes(b, off, len); 
	}

	@Override
	public int read(byte b[]) throws IOException {
		return readBytes(b, 0, b.length); 
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return super.skipBytes(n);
	}

	@Override
	public void write(byte b[]) throws IOException {
		writeBytes(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		writeBytes(b, off, len);
	}

	@Override
	public void close() throws IOException {
		
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
