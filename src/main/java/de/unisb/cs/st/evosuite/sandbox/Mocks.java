/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.sandbox;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mockit;
import de.unisb.cs.st.evosuite.Properties;

/**
 * This class is used for the mocks creation and destruction.
 * The implementation of the mocks and stubs is done with jmockit tool
 * trough the Annotation API. Check http://code.google.com/p/jmockit/
 * for more info.
 * 
 * @author Andrey Tarasevich
 *
 */
public class Mocks {
	
	/** Folder where IO should happen, if mocks are enabled */
	private final String sandboxPath = Properties.SANDBOX_FOLDER;
	
	/** If mocks should be created */
	private final boolean mocks = Properties.MOCKS; 
	
	/** If mocks already created */
	private boolean mocksEnabled = false;
	
	/**
	 * Initialize mocks in case the MOCKS property is set to true
	 */
	public void setUpMocks(){
		if(mocks){
			createIODir();
			setUpFileOutputStreamMock();
			setUpSystemMock();
			setUpFileMock();
			mocksEnabled = true;
		}
	}
	
	/**
	 * Deinitialize all mocks if any
	 */
	public void tearDownMocks(){
		if(mocksEnabled){
			Mockit.tearDownMocks();
			deleteIODir(sandboxPath);
			mocksEnabled = false;
		}
	}
	
	/**
	 * Create mocks for the class java.io.FileOutputStream
	 */
	private void setUpFileOutputStreamMock(){
		new MockUp<FileOutputStream>()
		{
			FileOutputStream it;
			@SuppressWarnings("unused")
			@Mock
			// Mock constructor - public FileOutputStream(File file, boolean append);
			// Current mock is just redirects the original output folder to sandbox 
			// folder. Private methods and fields are invoked and set through the 
			// Reflections and jmockit Deencapsulation.
			void $init(File file, boolean append)
			{
				String name = (file != null ? file.getPath() : null);
		        if (name == null) {
		            throw new NullPointerException();
		        } 
		        name = sandboxPath + name;

		        try {
		        	Deencapsulation.setField(it, "closeLock", new Object());
		        	
		        	Constructor<FileDescriptor> c = FileDescriptor.class.getConstructor();
		        	Object fd = c.newInstance(null);		 
					Method fdMethod = fd.getClass().getDeclaredMethod("incrementAndGetUseCount", null);
					
					fdMethod.setAccessible(true);
					fdMethod.invoke(fd, null);
					fdMethod.setAccessible(false);
					
					Deencapsulation.setField(it,"fd", fd);
					Deencapsulation.setField(it, "append", append);
					
					if(append)
						Deencapsulation.invoke(it, "openAppend", name);
					else
						Deencapsulation.invoke(it, "open", name);
						
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	/**
	 * Create mocks for the class java.lang.System
	 */
	private void setUpSystemMock(){
		new MockUp<System>()
		{
			@SuppressWarnings("unused")
			@Mock
			// Mock method public Properties getProperties();
			// The mocked method returns empty property instance.
			java.util.Properties getProperties() {
				return new java.util.Properties();
			}
		};
	}
	
	/**
	 * Create mocks for the class java.io.File
	 */
	private void setUpFileMock(){
		new MockUp<File>()
		{
			File it;
			boolean filePathChanged = false;
			@SuppressWarnings("unused")
			@Mock
			// Mock method public boolean mkdir();
			// Whenever the mkdir() is called, the path of the File instance 
			// is changed to sandboxPath + originalPath. Private methods and fields 
			// are invoked and set through the jmockit Deencapsulation.
			boolean mkdir(){
				Object fileSystem = Deencapsulation.getField(it, "fs");
				
				// Check if original path was already changed, if not - redirect it
				if(!filePathChanged){
					String originalPath = Deencapsulation.getField(it, "path");
					String changedPath = Deencapsulation.invoke(fileSystem, "normalize", sandboxPath + originalPath);
					filePathChanged = true;
					Deencapsulation.setField(it, "path", changedPath);
				}
				boolean dirCreated = false;
				dirCreated = (Boolean)Deencapsulation.invoke(fileSystem, "createDirectory", it);		
				return dirCreated;
			}
		};
	}
	/**
	 * Create directory where all IO should happen
	 */
	private void createIODir(){
		File dir = new File(sandboxPath);
		if(!dir.exists())
			dir.mkdir();
	}
	
	/**
	 * Remove files inside sandbox directory and remove directory itself
	 */
	private void deleteIODir(String path){
		File dir = new File(path);
		if(dir.exists()){
			String[] children = dir.list();
			for(String s : children){
				File f = new File(dir,s);
				if(f.isDirectory())
					deleteIODir(f.getAbsolutePath());
				f.delete();
			}			
		}
		dir.delete();
	}
}
