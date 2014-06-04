package org.evosuite.mock.java.io;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.evosuite.runtime.VirtualFileSystem;

public class MockFileWriter extends FileWriter{

	/*
	 * This class is specular to MockFileReader
	 */
	
	private OutputStreamWriter stream;
	
	/*
	 *  -- constructors --------
	 */
	
    public MockFileWriter(String fileName) throws IOException {
    		this(fileName != null ? new MockFile(fileName) : null);
    }

    public MockFileWriter(String fileName, boolean append) throws IOException {
    		this(fileName != null ? new MockFile(fileName) : null, append);
    	}

    public MockFileWriter(File file) throws IOException {
        this(file,false);
    }

    public MockFileWriter(File file, boolean append) throws IOException {
        super(VirtualFileSystem.getInstance().getRealTmpFile());
        
        MockFileOutputStream mock = new MockFileOutputStream(file,append);
        
        stream = new OutputStreamWriter(mock);
        
        VirtualFileSystem.getInstance().addLeakingResource(mock);
    }

    // we do not handle this constructor
    public MockFileWriter(FileDescriptor fd) {
        super(fd);
    }

    
    
    // ---- methods from  OutputStreamWriter -----------
    
    @Override
    public String getEncoding() {
        return stream.getEncoding();
    }

    /*
     * cannot override a package-level method...
     * 
     * but this is not a problem:
     * 1) only called by PrintStream
     * 2) anyway, the goal would be to mock all objects in
     *    a package 
     * 
    void flushBuffer() throws IOException {
        stream.flushBuffer();
    }
	*/

    @Override
    public void write(int c) throws IOException {
        stream.write(c);
    }

    @Override
    public void write(char cbuf[], int off, int len) throws IOException {
        stream.write(cbuf, off, len);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        stream.write(str, off, len);
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

}
