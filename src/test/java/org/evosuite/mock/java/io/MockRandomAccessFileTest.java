package org.evosuite.mock.java.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.evosuite.runtime.VirtualFileSystem;
import org.junit.Assert;
import org.junit.Test;

public class MockRandomAccessFileTest {

	@Test
	public void testNoWritePermission(){
		
		VirtualFileSystem.getInstance().resetSingleton();
		VirtualFileSystem.getInstance().init();
		
		String fileName = "foo_random_access.txt";
		
		RandomAccessFile ra = null;
		try {
			ra = new MockRandomAccessFile(fileName,"r");
			Assert.fail();
		} catch (FileNotFoundException e1) {
			//expected as file does not exist
		}
		
		File file = new MockFile(fileName);
		try {
			file.createNewFile();
		} catch (IOException e1) {
			Assert.fail(); //we should be able to create it
		}
		
		try {
			ra = new MockRandomAccessFile(fileName,"r");
		} catch (FileNotFoundException e1) {
			Assert.fail(); //we should be able to open the stream
		}
		
		final int LENGTH = 10;

		try {
			ra.setLength(LENGTH);
			Assert.fail();
		} catch (IOException e) {
			//expected, as we do now have write permissions;
		}
		
		long size = -1;
		
		try {
			ra.close();
			ra = new MockRandomAccessFile(fileName,"rw");
			ra.setLength(LENGTH);
			size = ra.length();
			ra.close();
		} catch (IOException e) {
			Assert.fail();
		}
		
		Assert.assertEquals(LENGTH, size);
	}
}
