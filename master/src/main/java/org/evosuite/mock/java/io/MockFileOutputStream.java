package org.evosuite.mock.java.io;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.evosuite.runtime.LeakingResource;
import org.evosuite.runtime.VirtualFileSystem;
import org.evosuite.runtime.vfs.FSObject;
import org.evosuite.runtime.vfs.VFile;

public class MockFileOutputStream extends FileOutputStream implements LeakingResource{
	
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
		this(name != null ? new MockFile(name) : null, false);
	}


	public MockFileOutputStream(String name, boolean append) throws FileNotFoundException {
		this(name != null ? new MockFile(name) : null, append);
	}


	public MockFileOutputStream(File file) throws FileNotFoundException {
		this(file, false);
	}


	public MockFileOutputStream(File file, boolean append) throws FileNotFoundException{
		
		super(VirtualFileSystem.getInstance().getRealTmpFile(),true); //just to make the compiler happy
		
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

		MockNative.writeBytes(path, position, b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		write(new byte[]{(byte)b},0,1);
	}

	@Override
	public void write(byte b[]) throws IOException {
		writeBytes(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		writeBytes(b, off, len);
	}

	
	//-----  other methods ------
	
	@Override
	public void close() throws IOException {
		
		super.close();
		
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
		synchronized (this) {
			if (channel == null) {
				channel = new EvoFileChannel(position,path,false,true);  
			}
			return channel;
		}
	}
	
	private void throwExceptionIfClosed() throws IOException{
		if(closed){
			throw new IOException();
		}
	}
	
	@Override
	public void release() throws Exception {		
			super.close();
	}
}
