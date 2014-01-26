package org.evosuite.mock.java.io;

import org.evosuite.Properties;
import org.evosuite.runtime.Runtime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class CharByteReadWriteTest {

	private static final boolean VFS = Properties.VIRTUAL_FS;

	@After
	public void restoreProperties(){
		Properties.VIRTUAL_FS = VFS;
	}

	@Test
	public void testReadWriteByte() throws Throwable{
		
		Properties.VIRTUAL_FS = true;
		Runtime.getInstance().resetRuntime();
		
		String file = "file.tmp";
		String expected = "Hello World!";
		byte[] data = expected.getBytes();
		
		MockFileOutputStream out = new MockFileOutputStream(file);
		out.write(data, 0, data.length);
		out.flush();
		out.close();
		
		byte[] buffer = new byte[1024];
		MockFileInputStream in = new MockFileInputStream(file);
		int read = in.read(buffer);
		in.close();
		String result = new String(buffer,0,read);
		
		Assert.assertEquals(expected, result);
	}

	@Test
	public void testReadWriteChar() throws Throwable{
		
		Properties.VIRTUAL_FS = true;
		Runtime.getInstance().resetRuntime();
		
		String file = "file.tmp";
		String expected = "Hello World!";
		char[] data = expected.toCharArray();
		
		MockFileWriter out = new MockFileWriter(file);
		out.write(data, 0, data.length);
		out.flush();
		out.close();
		
		char[] buffer = new char[1024];
		MockFileReader in = new MockFileReader(file);
		int read = in.read(buffer);
		in.close();
		String result = new String(buffer,0,read);
		
		Assert.assertEquals(expected, result);
	}

}
