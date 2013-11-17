package org.evosuite.mock.java.io;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.evosuite.runtime.VirtualFileSystem;
import org.evosuite.runtime.vfs.FSObject;
import org.evosuite.runtime.vfs.VFile;

public class MockFileInputStream extends FileInputStream{

	
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
		super(file); // just to make compiler happy
		
		path = (file != null ? file.getAbsolutePath() : null);
		if (path == null) {
			throw new NullPointerException();
		}

		VFile vf = getFileForReading();
		if(vf==null){
			throw new FileNotFoundException();
		}
	}
	
	private VFile getFileForReading(){
		FSObject target = VirtualFileSystem.getInstance().findFSObject(path);
		if(target==null || target.isDeleted() || target.isFolder() || !target.isReadPermission()){
			return null;
		}
		return (VFile) target;
	}
	
	//we do not really handle this constructor
	public MockFileInputStream(FileDescriptor fdObj) {
		super(fdObj);
		path = "";
	}

	// ----  read methods  ----------

	public int read() throws IOException{
		VFile vf = getFileForReading();
		if(vf==null || closed){
			throw new IOException();
		}
		
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);
		
		int b = vf.read(position.getAndIncrement());
				
		return b; 
	}

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
	public  long skip(long n) throws IOException{
		if(n<0){
			throw new IOException();
		}
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);
		position.addAndGet((int)n);
		return n; 
	}

	@Override
	public int available() throws IOException{
		
		VFile vf = getFileForReading();
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
				//TODO
			}
			return channel;
		}
	}
}
