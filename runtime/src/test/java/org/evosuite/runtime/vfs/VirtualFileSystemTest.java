/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.vfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.evosuite.runtime.Runtime;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.java.io.MockFile;
import org.evosuite.runtime.mock.java.io.MockFileInputStream;
import org.evosuite.runtime.mock.java.io.MockFileOutputStream;
import org.evosuite.runtime.vfs.VirtualFileSystem;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VirtualFileSystemTest {

	@Before
	public void init(){
		MockFramework.enable();
		VirtualFileSystem.getInstance().resetSingleton();
		VirtualFileSystem.getInstance().init();
	}
	
	@After
	public void tearDown(){
		VirtualFileSystem.getInstance().resetSingleton();
	}
	
	
	@Test
	public void testTokenizeOnWindows(){
		String[] paths = new String[]{
				"C:\\foo\\single",
				"C:\\\\foo\\\\double",
				"C:\\foo\\\\mixed",
				"D:\\foo\\onD",
				"D:\\foo\\trail\\",
				"D:\\foo\\doubleTrail\\\\",
				"D:\\\\\\\\foo\\eight"
		};
		for(String path : paths){
			String[] tokens = VirtualFileSystem.tokenize(path, '\\');
			Assert.assertEquals(Arrays.toString(tokens),3, tokens.length);
			for(String token : tokens){
				Assert.assertTrue(token,!token.contains("\\"));
			}
		}
	}
	
	@Test
	public void testNoAccessByDefault(){
		Assert.assertEquals(0, VirtualFileSystem.getInstance().getAccessedFiles().size());
	}
	
	@Test 
	public void testRename() throws IOException{
		File bla = new MockFile("bla");
		File doh = new MockFile("doh");
		Assert.assertFalse(bla.exists());
		Assert.assertFalse(doh.exists());
		
		boolean created = bla.createNewFile();
		Assert.assertTrue(created);
		Assert.assertTrue(bla.exists());
		Assert.assertFalse(doh.exists());
		
		boolean renamed = bla.renameTo(doh);
		Assert.assertTrue(renamed);
		Assert.assertFalse(bla.exists());
		Assert.assertTrue(doh.exists());
		
		File inAnotherFolder = new MockFile("foo/hei/hello.tmp");
		Assert.assertFalse(inAnotherFolder.exists());
		renamed = doh.renameTo(inAnotherFolder);
		Assert.assertFalse(renamed);
		Assert.assertFalse(inAnotherFolder.exists());
		Assert.assertTrue(doh.exists());
		
		File de = new MockFile("deeee");
		File blup = new MockFile("blup");
		Assert.assertFalse(de.exists());
		Assert.assertFalse(blup.exists());
		renamed = de.renameTo(blup);
		Assert.assertFalse(renamed);
		Assert.assertFalse(de.exists());
		Assert.assertFalse(blup.exists());
	}
	
	@Test
	public void testReadAfterWriteToFile() throws IOException{
		
		File file = MockFile.createTempFile("foo", ".tmp");
		Assert.assertTrue(file.exists());
		
		byte[] data = new byte[]{42,66};
		MockFileOutputStream out = new MockFileOutputStream(file);
		out.write(data);
		out.close();
		
		MockFileInputStream in = new MockFileInputStream(file);
		byte[] buffer = new byte[4];
		int count = in.read(buffer);
		in.close();
		Assert.assertEquals("End of stream should had been reached",data.length, count);
		Assert.assertEquals(data[0],buffer[0]);
		Assert.assertEquals(data[1],buffer[1]);
		Assert.assertEquals(0,buffer[2]);
		Assert.assertEquals(0,buffer[3]);
	}
	
	@Test
	public void testReadingNonExistingFile() throws IOException{
		String fileName = "this_file_should_not_exist";
		File realFile = new File(fileName);
		Assert.assertFalse(realFile.exists());
		
		try{
			MockFileInputStream in = new MockFileInputStream(realFile);
			Assert.fail(); //real file does not exist
		} catch(FileNotFoundException e){			
		}
		
		File mockFile = new MockFile(fileName);
		Assert.assertFalse(mockFile.exists());
		
		try{
			MockFileInputStream in = new MockFileInputStream(mockFile);
			Assert.fail(); // also the mock file does not exist (yet)
		} catch(FileNotFoundException e){			
		}
		
		boolean created = mockFile.createNewFile();
		Assert.assertTrue(created);
		Assert.assertTrue(mockFile.exists());
		Assert.assertFalse(realFile.exists()); //real file shouldn's have been created
		
		//following should work even if real file does not exist
		MockFileInputStream in = new MockFileInputStream(mockFile);
	}
	
	@Test
	public void testWriteToFile() throws IOException{
		
		String fileName = "foo_written_with_FOS";
		File realFile = new File(fileName);
		realFile.deleteOnExit(); // be sure to get it deleted in case we accidently create it
		Assert.assertFalse(realFile.exists());
		
		File file = new MockFile(fileName);
		Assert.assertFalse(file.exists());
		
		byte[] data = new byte[]{42};
		MockFileOutputStream out = new MockFileOutputStream(file);
		out.write(data);
		
		//writing to such file should create it
		Assert.assertTrue(file.exists());
		
		out.close();
		try{
			out.write(data);
			Assert.fail();
		} catch(Exception e){
			//this is expected, as the stream is closed
		}
		
		//be sure that no real file was created
		Assert.assertFalse(realFile.exists());
	}
	
	@Test
	public void testTmpFileCreation() throws IOException{
		
		File file = MockFile.createTempFile("foo", ".tmp");
		Assert.assertTrue(file.exists());
		String path = file.getAbsolutePath();
		java.lang.System.out.println(path);
		Assert.assertTrue(path,path.contains("foo") & path.contains(".tmp"));
	}
	
	@Test
	public void testWorkingDirectoryExists(){
		MockFile workingDir = new MockFile(java.lang.System.getProperty("user.dir"));
		Assert.assertTrue(workingDir.exists());
	}
	
	@Test
	public void testCreateDeleteFileDirectly() throws IOException{
		
		MockFile file = new MockFile("foo");
		Assert.assertFalse(file.exists());
		boolean created = file.createNewFile();
		Assert.assertTrue(created);
		Assert.assertTrue(file.exists());
		boolean deleted = file.delete();
		Assert.assertTrue(deleted);
		Assert.assertFalse(file.exists());
	}


	@Test
	public void testCreateDeleteFolderDirectly() throws IOException{
		
		MockFile folder = new MockFile("foo"+File.separator+"hello");
		Assert.assertFalse(folder.exists());
		boolean created = folder.mkdir(); // parent doesn't exist, so should fail
		Assert.assertFalse(created);		
		Assert.assertFalse(folder.exists());
		
		created = folder.mkdirs();
		Assert.assertTrue(created);		
		Assert.assertTrue(folder.exists());
		
		MockFile file = new MockFile(folder.getAbsoluteFile()+File.separator+"evo");
		created = file.createNewFile();
		Assert.assertTrue(created);		
		Assert.assertTrue(file.exists());
		
		//deleting non-empty folder should fail
		boolean deleted = folder.delete();
		Assert.assertFalse(deleted);
		Assert.assertTrue(folder.exists());
		
		deleted = file.delete();
		Assert.assertTrue(deleted);
		Assert.assertFalse(file.exists());
		
		//now we can delete the folder
		deleted = folder.delete();
		Assert.assertTrue(deleted);
		Assert.assertFalse(folder.exists());		
	}

}
