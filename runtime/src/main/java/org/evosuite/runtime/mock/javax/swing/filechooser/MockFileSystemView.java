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
package org.evosuite.runtime.mock.javax.swing.filechooser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.mock.java.io.MockFile;
import org.evosuite.runtime.vfs.FSObject;
import org.evosuite.runtime.vfs.VFolder;
import org.evosuite.runtime.vfs.VirtualFileSystem;


public abstract class MockFileSystemView extends FileSystemView  implements OverrideMock{

	static FileSystemView windowsFileSystemView = null;
	static FileSystemView unixFileSystemView = null;
	static FileSystemView genericFileSystemView = null;

	private boolean useSystemExtensionHiding =
			UIManager.getDefaults().getBoolean("FileChooser.useSystemExtensionHiding");


	public static FileSystemView getFileSystemView() {
		if(File.separatorChar == '\\') {
			if(windowsFileSystemView == null) {
				windowsFileSystemView = new MockWindowsFileSystemView();
			}
			return windowsFileSystemView;
		}

		if(File.separatorChar == '/') {
			if(unixFileSystemView == null) {
				unixFileSystemView = new MockUnixFileSystemView();
			}
			return unixFileSystemView;
		}


		if(genericFileSystemView == null) {
			genericFileSystemView = new MockGenericFileSystemView();
		}
		return genericFileSystemView;
	}


	public MockFileSystemView() {
		final WeakReference<MockFileSystemView> weakReference = new WeakReference<MockFileSystemView>(this);

		UIManager.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				MockFileSystemView fileSystemView = weakReference.get();

				if (fileSystemView == null) {
					// FileSystemView was destroyed
					UIManager.removePropertyChangeListener(this);
				} else {
					if (evt.getPropertyName().equals("lookAndFeel")) {
						fileSystemView.useSystemExtensionHiding =
								UIManager.getDefaults().getBoolean("FileChooser.useSystemExtensionHiding");
					}
				}
			}
		});
	}

	@Override
	public boolean isRoot(File f) {
		return super.isRoot(f);
	}

	@Override
	public Boolean isTraversable(File f) {
		return super.isTraversable(f);
	}

	@Override
	public String getSystemDisplayName(File f) {
		if (f == null) {
			return null;
		}

		String name = f.getName();

		/*
		if (!name.equals("..") && !name.equals(".") &&
				(useSystemExtensionHiding || !isFileSystem(f) || isFileSystemRoot(f)) &&
				(f instanceof ShellFolder || f.exists())) {

			try {
				name = getShellFolder(f).getDisplayName();
			} catch (FileNotFoundException e) {
				return null;
			}
		}
		 */
		if (name == null || name.length() == 0) {
			name = f.getPath(); // e.g. "/"
		}
		//}

		return name;
	}

	@Override
	public String getSystemTypeDescription(File f) {
		return super.getSystemTypeDescription(f);
	}

	@Override
	public Icon getSystemIcon(File f) {
		if (f == null) {
			return null;
		}

		/*
		ShellFolder sf;

		try {
			sf = getShellFolder(f);
		} catch (FileNotFoundException e) {
			return null;
		}

		Image img = sf.getIcon(false);

		if (img != null) {
			return new ImageIcon(img, sf.getFolderType());
		} else {
		 */

		return UIManager.getIcon(f.isDirectory() ? "FileView.directoryIcon" : "FileView.fileIcon");
		//}
	}

	@Override
	public boolean isParent(File folder, File file) {
		if (folder == null || file == null) {
			return false;
		} /* else if (folder instanceof ShellFolder) {
			File parent = file.getParentFile();
			if (parent != null && parent.equals(folder)) {
				return true;
			}
			File[] children = getFiles(folder, false);
			for (File child : children) {
				if (file.equals(child)) {
					return true;
				}
			}
			return false;
		} */ else {
			return folder.equals(file.getParentFile());
		}
	}

	@Override
	public File getChild(File parent, String fileName) {
		/*
		if (parent instanceof ShellFolder) {
			File[] children = getFiles(parent, false);
			for (File child : children) {
				if (child.getName().equals(fileName)) {
					return child;
				}
			}
		}
		*/
		return createFileObject(parent, fileName);
	}

	@Override
	public boolean isFileSystem(File f) {
		/*
		if (f instanceof ShellFolder) {
			ShellFolder sf = (ShellFolder)f;
			// Shortcuts to directories are treated as not being file system objects,
			// so that they are never returned by JFileChooser.
			return sf.isFileSystem() && !(sf.isLink() && sf.isDirectory());
		} else {
			return true;
		}
		*/
		return true;
	}

	@Override
	public boolean isHiddenFile(File f) {
		return super.isHiddenFile(f);
	}

	@Override
	public boolean isFileSystemRoot(File dir) {

		FSObject fso = VirtualFileSystem.getInstance().findFSObject(dir.getAbsolutePath());
		if(fso==null || !(fso instanceof VFolder)){
			return false;
		}

		VFolder folder = (VFolder) fso;
		return folder.isRoot();
	}

	@Override
	public boolean isDrive(File dir) {
		return super.isDrive(dir);
	}

	@Override
	public boolean isFloppyDrive(File dir) {
		return super.isFloppyDrive(dir);
	}

	@Override
	public boolean isComputerNode(File dir) {
		
		//return ShellFolder.isComputerNode(dir);
		/*
		 * TODO: not really sure how to mock this one, and if
		 * we actually need it
		 * 
		 */
		return false;
	}

	@Override
	public File[] getRoots() {
		// Don't cache this array, because filesystem might change
		//File[] roots = (File[])ShellFolder.get("roots");
		File[] roots = MockFile.listRoots();

		for (int i = 0; i < roots.length; i++) {
			if (isFileSystemRoot(roots[i])) {
				roots[i] = createFileSystemRoot(roots[i]);
			}
		}
		return roots;
	}


	// Providing default implementations for the remaining methods
	// because most OS file systems will likely be able to use this
	// code. If a given OS can't, override these methods in its
	// implementation.

	@Override
	public File getHomeDirectory() {
		return super.getHomeDirectory();
	}

	public File getDefaultDirectory() {
		//File f = (File)ShellFolder.get("fileChooserDefaultFolder");
		File f = getHomeDirectory();
		
		if (isFileSystemRoot(f)) {
			f = createFileSystemRoot(f);
		}
		return f;
	}

	@Override
	public File createFileObject(File dir, String filename) {
		if(dir == null) {
			return new MockFile(filename);
		} else {
			return new MockFile(dir, filename);
		}
	}

	@Override
	public File createFileObject(String path) {
		File f = new MockFile(path);
		if (isFileSystemRoot(f)) {
			f = createFileSystemRoot(f);
		}
		return f;
	}

	@Override
	public File[] getFiles(File dir, boolean useFileHiding) {
		List<File> files = new ArrayList<File>();

		if(dir==null){
			return new File[0];
		}
		
		//File[] names = ((ShellFolder) dir).listFiles(!useFileHiding);
		File[] names = dir.listFiles();
		
		if (names == null) {
			return new File[0];
		}

		for (File f : names) {
			if (Thread.currentThread().isInterrupted()) {
				break;
			}

			//if (!(f instanceof ShellFolder)) {
				if (isFileSystemRoot(f)) {
					f = createFileSystemRoot(f);
				}
				/*
				try {
					f = ShellFolder.getShellFolder(f);
				} catch (FileNotFoundException e) {
					// Not a valid file (wouldn't show in native file chooser)
					// Example: C:\pagefile.sys
					continue;
				} catch (InternalError e) {
					// Not a valid file (wouldn't show in native file chooser)
					// Example C:\Winnt\Profiles\joe\history\History.IE5
					continue;
				}
				*/
			//}
			if (!useFileHiding || !isHiddenFile(f)) {
				files.add(f);
			}
		}

		return files.toArray(new File[files.size()]);
	}


	@Override
	public File getParentDirectory(File dir) {
		if (dir == null || !dir.exists()) {
			return null;
		}

		return dir.getParentFile();
		
		/*
		ShellFolder sf;

		try {
			sf = getShellFolder(dir);
		} catch (FileNotFoundException e) {
			return null;
		}

		File psf = sf.getParentFile();

		if (psf == null) {
			return null;
		}

		if (isFileSystem(psf)) {
			File f = psf;
			if (!f.exists()) {
				// This could be a node under "Network Neighborhood".
				File ppsf = psf.getParentFile();
				if (ppsf == null || !isFileSystem(ppsf)) {
					// We're mostly after the exists() override for windows below.
					f = createFileSystemRoot(f);
				}
			}
			return f;
		} else {
			return psf;
		}
		*/
	}


	@Override
	protected File createFileSystemRoot(File f) {
		
		if(f instanceof MockFileSystemRoot){
			return f;
		}
		
		return new MockFileSystemRoot(f);
	}


	static class MockFileSystemRoot extends MockFile {

		private static final long serialVersionUID = -7059909648025194367L;

		public MockFileSystemRoot(File f) {
			super(f,"");
		}

		public MockFileSystemRoot(String s) {
			super(s);
		}

		public boolean isDirectory() {
			return true;
		}

		public String getName() {
			return getPath();
		}
	}
}


class MockUnixFileSystemView extends MockFileSystemView {

	private static final String newFolderString =
			UIManager.getString("FileChooser.other.newFolder");
	private static final String newFolderNextString  =
			UIManager.getString("FileChooser.other.newFolder.subsequent");

	public File createNewFolder(File containingDir) throws IOException {
		if(containingDir == null) {
			throw new IOException("Containing directory is null:");
		}
		
		File newFolder;
		// Unix - using OpenWindows' default folder name. Can't find one for Motif/CDE.
		newFolder = createFileObject(containingDir, newFolderString);
		int i = 1;
		while (newFolder.exists() && i < 100) {
			newFolder = createFileObject(containingDir, MessageFormat.format(
					newFolderNextString, new Integer(i)));
			i++;
		}

		if(newFolder.exists()) {
			throw new IOException("Directory already exists:" + newFolder.getAbsolutePath());
		} else {
			newFolder.mkdirs();
		}

		return newFolder;
	}

	public boolean isFileSystemRoot(File dir) {
		return dir != null && dir.getAbsolutePath().equals("/");
	}

	public boolean isDrive(File dir) {
		return isFloppyDrive(dir);
	}

	public boolean isFloppyDrive(File dir) {
		// Could be looking at the path for Solaris, but wouldn't be reliable.
		// For example:
		// return (dir != null && dir.getAbsolutePath().toLowerCase().startsWith("/floppy"));
		return false;
	}

	public boolean isComputerNode(File dir) {
		if (dir != null) {
			String parent = dir.getParent();
			if (parent != null && parent.equals("/net")) {
				return true;
			}
		}
		return false;
	}
}



class MockWindowsFileSystemView extends MockFileSystemView {

	private static final String newFolderString =
			UIManager.getString("FileChooser.win32.newFolder");
	private static final String newFolderNextString  =
			UIManager.getString("FileChooser.win32.newFolder.subsequent");

	public Boolean isTraversable(File f) {
		return Boolean.valueOf(isFileSystemRoot(f) || isComputerNode(f) || f.isDirectory());
	}

	public File getChild(File parent, String fileName) {
		if (fileName.startsWith("\\")
				&& !fileName.startsWith("\\\\")
				&& isFileSystem(parent)) {

			//Path is relative to the root of parent's drive
			String path = parent.getAbsolutePath();
			if (path.length() >= 2
					&& path.charAt(1) == ':'
					&& Character.isLetter(path.charAt(0))) {

				return createFileObject(path.substring(0, 2) + fileName);
			}
		}
		return super.getChild(parent, fileName);
	}

	public String getSystemTypeDescription(File f) {
		return super.getSystemTypeDescription(f);
		/*
		if (f == null) {
			return null;
		}

		try {
			return getShellFolder(f).getFolderType();
		} catch (FileNotFoundException e) {
			return null;
		}
		*/
	}

	/**
	 * @return the Desktop folder.
	 */
	public File getHomeDirectory() {
		return getRoots()[0];
	}

	/**
	 * Creates a new folder with a default folder name.
	 */
	public File createNewFolder(File containingDir) throws IOException {
		if(containingDir == null) {
			throw new IOException("Containing directory is null:");
		}
		// Using NT's default folder name
		File newFolder = createFileObject(containingDir, newFolderString);
		int i = 2;
		while (newFolder.exists() && i < 100) {
			newFolder = createFileObject(containingDir, MessageFormat.format(
					newFolderNextString, new Integer(i)));
			i++;
		}

		if(newFolder.exists()) {
			throw new IOException("Directory already exists:" + newFolder.getAbsolutePath());
		} else {
			newFolder.mkdirs();
		}

		return newFolder;
	}

	public boolean isDrive(File dir) {
		return isFileSystemRoot(dir);
	}

	public boolean isFloppyDrive(final File dir) {
		/*
		String path = AccessController.doPrivileged(new PrivilegedAction<String>() {
			public String run() {
				return dir.getAbsolutePath();
			}
		});
		 */
		String path = dir.getAbsolutePath();
		return path != null && (path.equals("A:\\") || path.equals("B:\\"));
	}

	/**
	 * Returns a File object constructed from the given path string.
	 */
	public File createFileObject(String path) {
		// Check for missing backslash after drive letter such as "C:" or "C:filename"
		if (path.length() >= 2 && path.charAt(1) == ':' && Character.isLetter(path.charAt(0))) {
			if (path.length() == 2) {
				path += "\\";
			} else if (path.charAt(2) != '\\') {
				path = path.substring(0, 2) + "\\" + path.substring(2);
			}
		}
		return super.createFileObject(path);
	}

	protected File createFileSystemRoot(File f) {
		// Problem: Removable drives on Windows return false on f.exists()
		// Workaround: Override exists() to always return true.
		return new MockFileSystemRoot(f) {			
			private static final long serialVersionUID = 1L;
			public boolean exists() {
				return true;
			}
		};
	}

}

/**
 * Fallthrough FileSystemView in case we can't determine the OS.
 */
class MockGenericFileSystemView extends MockFileSystemView {

	private static final String newFolderString =
			UIManager.getString("FileChooser.other.newFolder");

	/**
	 * Creates a new folder with a default folder name.
	 */
	public File createNewFolder(File containingDir) throws IOException {
		if(containingDir == null) {
			throw new IOException("Containing directory is null:");
		}
		// Using NT's default folder name
		File newFolder = createFileObject(containingDir, newFolderString);

		if(newFolder.exists()) {
			throw new IOException("Directory already exists:" + newFolder.getAbsolutePath());
		} else {
			newFolder.mkdirs();
		}

		return newFolder;
	}

}

