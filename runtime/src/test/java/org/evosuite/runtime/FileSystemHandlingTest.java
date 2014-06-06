package org.evosuite.runtime;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.evosuite.Properties;
import org.evosuite.mock.java.io.MockFile;
import org.evosuite.mock.java.io.MockFileInputStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class FileSystemHandlingTest {

	private static final boolean VFS = Properties.VIRTUAL_FS;

	@After
	public void restoreProperties(){
		Properties.VIRTUAL_FS = VFS;
	}

	@Test
	public void createNewFileByAddingData() throws IOException{

		Properties.VIRTUAL_FS = true;
		Runtime.getInstance().resetRuntime();

		byte[] data = new byte[]{42,66};

		EvoSuiteFile file = new EvoSuiteFile("foo");
		MockFile mf = new MockFile(file.getPath());
		Assert.assertFalse(mf.exists());

		FileSystemHandling.appendDataToFile(file, data);

		Assert.assertTrue(mf.exists());

		MockFileInputStream in = new MockFileInputStream(file.getPath());
		byte[] buffer = new byte[4];
		int count = in.read(buffer);
		in.close();
		Assert.assertEquals(data.length, count);
		Assert.assertEquals(data[0],buffer[0]);
		Assert.assertEquals(data[1],buffer[1]);
		Assert.assertEquals(0,buffer[2]);
		Assert.assertEquals(0,buffer[3]);
	}

	@Test
	public void createNewFileByAddingLine() throws IOException{

		Properties.VIRTUAL_FS = true;
		Runtime.getInstance().resetRuntime();

		String data = "A new line to be added";

		EvoSuiteFile file = new EvoSuiteFile("foo");
		MockFile mf = new MockFile(file.getPath());
		Assert.assertFalse(mf.exists());

		FileSystemHandling.appendStringToFile(file, data);

		Assert.assertTrue(mf.exists());

		//try read bytes directly
		MockFileInputStream in = new MockFileInputStream(file.getPath());
		byte[] buffer = new byte[1024];
		in.read(buffer);
		in.close();
		String byteString = new String(buffer);
		Assert.assertTrue("Read: "+byteString, byteString.startsWith(data));

		//try with InputStreamReader
		InputStreamReader reader = new InputStreamReader(new MockFileInputStream(file.getPath()));
		char[] cbuf = new char[1024];
		reader.read(cbuf);
		reader.close();
		String charString = new String(cbuf);
		Assert.assertTrue("Read: "+charString, charString.startsWith(data));

		//try BufferedReader
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new MockFileInputStream(file.getPath())));
		cbuf = new char[1024];
		bufferedReader.read(cbuf);
		bufferedReader.close();
		charString = new String(cbuf);
		Assert.assertTrue("Read: "+charString, charString.startsWith(data));


		//try with Scanner
		Scanner fromFile = new Scanner(new MockFileInputStream(file.getPath()));
		String fileContent = fromFile.nextLine();
		fromFile.close();

		Assert.assertEquals(data,fileContent);		
	}

}
