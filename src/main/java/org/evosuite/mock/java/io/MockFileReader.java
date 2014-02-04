package org.evosuite.mock.java.io;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.evosuite.runtime.VirtualFileSystem;

public class MockFileReader extends FileReader{

	/**
	 * As all the constructors of FileReader instantiate
	 * a FileInputStream, none of them can be used to create
	 * a usable instance of MockFileReader.
	 * So, we need to create an instance of MockFileInputStream,
	 * a redirect all calls of MockFileReader to this stream 
	 */
	private InputStreamReader stream;
		
	/*
	 * -- constructors ----------------------
	 * 
	 * FileReader defines only constructors, no methods
	 */
	
    public MockFileReader(String fileName) throws FileNotFoundException {
    		this(fileName != null ? new MockFile(fileName) : null);
    }

    public MockFileReader(File file) throws FileNotFoundException {
		super(VirtualFileSystem.getInstance().getRealTmpFile()); // just to make compiler happy
		
		MockFileInputStream mock = new MockFileInputStream(file);
		
		stream = new InputStreamReader(mock);
		
		VirtualFileSystem.getInstance().addLeakingResource(mock);
    }

    //we do not really handle this constructor
    public MockFileReader(FileDescriptor fd) {
        super(fd);
    }

    //-- methods from InputStreamReader -------------

    @Override
    public String getEncoding() {
        return super.getEncoding();
    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int read(char cbuf[], int offset, int length) throws IOException {
        return stream.read(cbuf, offset, length);
    }

    @Override
    public boolean ready() throws IOException {
        return stream.ready();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
        
}
