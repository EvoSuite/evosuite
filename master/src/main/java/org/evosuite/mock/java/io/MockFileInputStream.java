package org.evosuite.mock.java.io;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.evosuite.runtime.LeakingResource;
import org.evosuite.runtime.VirtualFileSystem;
import org.evosuite.runtime.vfs.VFile;

public class MockFileInputStream extends FileInputStream implements LeakingResource{

	
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
		this(name != null ? new MockFile(name) : null);
	}

	public MockFileInputStream(File file) throws FileNotFoundException {
		super(VirtualFileSystem.getInstance().getRealTmpFile()); // just to make compiler happy
		
		VirtualFileSystem.getInstance().addLeakingResource(this);
		
		path = (file != null ? file.getAbsolutePath() : null);
		if (path == null) {
			throw new NullPointerException();
		}

		VFile vf = MockNative.getFileForReading(path);
		if(vf==null){
			throw new FileNotFoundException();
		}
	}
	
	//we do not really handle this constructor
	public MockFileInputStream(FileDescriptor fdObj) {
		super(fdObj);
		path = "";
	}

	// ----  read methods  ----------

	public int read() throws IOException{
		
		throwExceptionIfClosed();
		
		return MockNative.read(path, position); 
	}

	private  int readBytes(byte b[], int off, int len) throws IOException{
		
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
		if(n<0){
			throw new IOException();
		}
		
		throwExceptionIfClosed();
		
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);
		position.addAndGet((int)n);
		return n; 
	}

	@Override
	public int available() throws IOException{
		
		throwExceptionIfClosed();
		
		VFile vf = MockNative.getFileForReading(path);
		if(vf==null){
			throw new IOException();
		}
		
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);
		
		int size = vf.getDataSize();
		int available = size - position.get();
		
		return available; 
	}

	@Override
	public int read(byte b[]) throws IOException {
		return readBytes(b, 0, b.length);
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		return readBytes(b, off, len);
	}

	
	//-------- other methods ------------

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

	/*  //Cannot be overriden
    public final FileDescriptor getFD() throws IOException {
        if (fd != null) return fd;
        throw new IOException();
    }
	 */

	@Override
	public FileChannel getChannel() {
		synchronized (this) {
			if (channel == null) {
				channel = new EvoFileChannel(position,path,true,false); 
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
