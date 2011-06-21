/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.sandbox;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mockit;
import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.utils.Utils;

/**
 * This class is used for the mocks creation and destruction. The implementation
 * of the mocks and stubs is done with jmockit tool through the Annotation API.
 * Check http://code.google.com/p/jmockit/ for more info.
 * 
 * @author Andrey Tarasevich
 * 
 */
class Mocks {

	/** Folder where IO should happen, if mocks are enabled */
	private final String sandboxPath;

	/** If mocks already created */
	private boolean mocksEnabled = false;

	/**
	 * Set that contains the names of the files, which were attempted to be read
	 */
	private final Set<String> filesAccessed = new HashSet<String>();

	/**
	 * Initialization of required fields.
	 */
	public Mocks() {
		// Using File class in order to get absolute path of the sandbox
		// folder.
		File f = new File(Properties.SANDBOX_FOLDER);
		sandboxPath = f.getAbsolutePath() + "/";
	}

	/**
	 * Initialize mocks in case the MOCKS property is set to true
	 */
	public void setUpMocks() {
		Utils.createDir(sandboxPath);
		setUpFileOutputStreamMock();
		setUpSystemMock();
		setUpFileMock();
		setUpFileInputStreamMock();
		mocksEnabled = true;
	}

	/**
	 * Deinitialize all mocks if any
	 */
	public void tearDownMocks() {
		if (mocksEnabled) {
			Mockit.tearDownMocks();
			Utils.deleteDir(sandboxPath);
			mocksEnabled = false;
			filesAccessed.clear();
		}
	}

	/**
	 * Create mocks for the class java.io.FileOutputStream
	 */
	private void setUpFileOutputStreamMock() {
		new MockUp<FileOutputStream>() {
			FileOutputStream it;

			@SuppressWarnings("unused")
			@Mock
			// Mock constructor - public FileOutputStream(File file, boolean append);
			// Current mock is just redirects the original output folder to sandbox 
			// folder. Private methods and fields are invoked and set through the 
			// jmockit Deencapsulation.
			void $init(File file, boolean append) {
				String name = (file != null ? file.getPath() : null);
				if (name == null) {
					throw new NullPointerException();
				}
				name = sandboxPath + name;

				try {
					Deencapsulation.setField(it, "closeLock", new Object());
					Object fd = Deencapsulation.newInstance(FileDescriptor.class);
					Deencapsulation.invoke(fd, "incrementAndGetUseCount");
					Deencapsulation.setField(it, "fd", fd);
					Deencapsulation.setField(it, "append", append);

					if (append)
						Deencapsulation.invoke(it, "openAppend", name);
					else
						Deencapsulation.invoke(it, "open", name);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
		};
	}

	/**
	 * Create mocks for the class java.lang.System
	 */
	private void setUpSystemMock() {
		new MockUp<System>() {
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
	private void setUpFileMock() {
		new MockUp<File>() {
			File it;
			boolean filePathChanged = false;

			@SuppressWarnings("unused")
			@Mock
			// Mock method public boolean mkdir();
			// Whenever the mkdir() is called, the path of the File instance 
			// is changed to sandboxPath + originalPath. Private methods and fields 
			// are invoked and set through the jmockit Deencapsulation.
			boolean mkdir() {
				Object fileSystem = Deencapsulation.getField(it, "fs");
				String originalPath = Deencapsulation.getField(it, "path");

				// Check if original path was already changed, if not - redirect it
				if (!originalPath.contains(sandboxPath) || !filePathChanged) {
					String changedPath = Deencapsulation.invoke(fileSystem, "normalize",
					                                            sandboxPath
					                                                    + originalPath);
					filePathChanged = true;
					Deencapsulation.setField(it, "path", changedPath);
				}
				boolean dirCreated = false;
				dirCreated = (Boolean) Deencapsulation.invoke(fileSystem,
				                                              "createDirectory", it);
				return dirCreated;
			}

			@SuppressWarnings("unused")
			@Mock
			// Mock method public boolean isDirectory();
			// Mock is done to avoid security manager checks.
			boolean isDirectory() {
				Object fileSystem = Deencapsulation.getField(it, "fs");
				int attr = (Integer) Deencapsulation.invoke(fileSystem,
				                                            "getBooleanAttributes", it);
				int ba_dir = (Integer) Deencapsulation.getField(fileSystem,
				                                                "BA_DIRECTORY");
				return ((attr & ba_dir) != 0);
			}
		};
	}

	/**
	 * Create mocks for the class java.io.FileInputStream
	 */
	private void setUpFileInputStreamMock() {
		new MockUp<FileInputStream>() {
			FileInputStream it;

			@SuppressWarnings("unused")
			@Mock
			// Mock constructor public FileInputStream(File file);
			// Current mock redirects IO call to the sandbox folder.
			// Also remembers the names of the files, which were attempted
			// to be read.
			void $init(File file) {
				String name = (file != null ? file.getPath() : null);
				if (name == null) {
					throw new NullPointerException();
				}
				try {
					Deencapsulation.setField(it, "closeLock", new Object());
					Object fd = Deencapsulation.newInstance(FileDescriptor.class);
					Deencapsulation.invoke(fd, "incrementAndGetUseCount");
					Deencapsulation.setField(it, "fd", fd);

					String pathToOpen = name;

					if (checkStackTrace()) {
						pathToOpen = sandboxPath + pathToOpen;
						filesAccessed.add(pathToOpen);
					}

					Deencapsulation.invoke(it, "open", pathToOpen);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
		};
	}

	/**
	 * Checks StackTrace of the current thread and decides, whether the file
	 * path should be changed
	 * 
	 * @return true, if file path should be changed, false otherwise
	 */
	private boolean checkStackTrace() {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		for (int elementCounter = 0; elementCounter < stackTraceElements.length; elementCounter++) {
			StackTraceElement e = stackTraceElements[elementCounter];
			if (e.getMethodName().equals("setSecurityManager"))
				return false;
		}
		return true;
	}

	public Set<String> getFilesAccessed() {
		return filesAccessed;
	}
}
