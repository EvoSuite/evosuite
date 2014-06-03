package org.evosuite.mock.java.io;

import java.util.Scanner;

import org.evosuite.Properties;
import org.evosuite.runtime.Runtime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CharByteReadWriteTest {

	private static final boolean VFS = Properties.VIRTUAL_FS;

	@Before
	public void init(){		
		Properties.VIRTUAL_FS = true;
		Runtime.getInstance().resetRuntime();		
	}
	
	@After
	public void restoreProperties(){
		Properties.VIRTUAL_FS = VFS;
	}

	@Test
	public void testReadWriteByte() throws Throwable{
		
		String file = "FileOutputStream_file.tmp";
		String expected = "testReadWriteByte";
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
		
		String file = "FileWriter_file.tmp";
		String expected = "testReadWriteChar";
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

	@Test
	public void testPrintWriter() throws Throwable{
		
		String file = "PrintWriter_file.tmp";
		String expected = "testPrintWriter";

		MockPrintWriter out = new MockPrintWriter(file);
		out.println(expected);
		out.close();

		Scanner in = new Scanner(new MockFileInputStream(file));
		String result = in.nextLine();
		in.close();
		
		Assert.assertEquals(expected, result);
	}

	@Test
	public void testPrintStream() throws Throwable{
		
		String file = "PrintStream_file.tmp";
		String expected = "testPrintStream";

		MockPrintStream out = new MockPrintStream(file);
		out.println(expected);
		out.close();

		Scanner in = new Scanner(new MockFileInputStream(file));
		String result = in.nextLine();
		in.close();
		
		Assert.assertEquals(expected, result);
	}


}
