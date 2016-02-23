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
package org.evosuite.runtime.mock.java.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;
import org.evosuite.runtime.mock.java.lang.MockIllegalArgumentException;
import org.evosuite.runtime.mock.java.net.MockURL;
import org.evosuite.runtime.vfs.FSObject;
import org.evosuite.runtime.vfs.VFile;
import org.evosuite.runtime.vfs.VFolder;
import org.evosuite.runtime.vfs.VirtualFileSystem;

/**
 * This class is used in the mocking framework to replace File instances.
 * 
 * <p>
 * All files are created in memory, and no access to disk is ever done
 *   
 * @author arcuri
 *
 */
public class MockFile extends File implements OverrideMock {

	private static final long serialVersionUID = -8217763202925800733L;

	/*
	 *  Constructors, with same inputs as in File. Note: it is not possible to inherit JavaDocs for constructors.
	 */

	public MockFile(String pathname) {
		super(pathname);
	}

	public MockFile(String parent, String child) {
		this(combine(parent,child));
	}

	public MockFile(File parent, String child) {
		this(parent == null ? (String) null : parent.getAbsolutePath()
				, child);
	}

	public MockFile(URI uri) {
		super(uri);
	}

	private static String combine(String parent, String child){
		if (child == null) {
			throw new NullPointerException();
		}
		if(parent == null){
			return child;
		}
		if(parent.equals("")){
			return VirtualFileSystem.getDefaultParent()+child;
		}
		return makeAbsolute(parent) + "/" + child;
	}

	private static String makeAbsolute(String path){

		String base = VirtualFileSystem.getWorkingDirPath();
		if(base.startsWith("/")){
			//Mac/Linux
			if(path.startsWith("/")){
				return path;
			} else {
				return base + "/" + path;
			}
		} else {
			//Windows
			//TODO: tmp, nasty hack, but anyway this class ll need refactoring when fully handling Java 8
			String root = base.substring(0, 3); //eg, C:\
			if(path.startsWith(root)){
				return path;
			} else{
				return base + "/" + path;
			}
		}
	}

	/*
	 * TODO: Java 7
	 * 
	 * there is only one method in File that depends on Java 7:
	 * 
	 * public Path toPath()
	 * 
	 * 
	 * but if we include it here, we will break compatibility with Java 6.
	 * Once we drop such backward compatibility, we will need to override
	 * such method
	 */
	
	/*
	 * --------- static methods ------------------
	 * 
	 * recall: it is not possible to override static methods.
	 * In the SUT, all calls to those static methods of File, eg File.foo(),
	 * will need to be replaced with EvoFile.foo() 
	 */

	public static File[] listRoots() {
		if(! MockFramework.isEnabled()){
			return File.listRoots();
		}

		//FIXME: this is not going to work if tests are executed on different machine
		File[] roots = File.listRoots();
		MockFile[] mocks = new MockFile[roots.length];
		for(int i=0; i<roots.length; i++){
			mocks[i] = new MockFile(roots[i].getAbsolutePath());
		}
		return mocks; 
	}

	public static File createTempFile(String prefix, String suffix, File directory)
			throws IOException{
		if(! MockFramework.isEnabled()){
			return File.createTempFile(prefix, suffix, directory);
		}

		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded("");
		
		String path = VirtualFileSystem.getInstance().createTempFile(prefix, suffix, directory);
		if(path==null){
			throw new MockIOException();
		}
		return new MockFile(path); 
	}

	public static File createTempFile(String prefix, String suffix)
			throws IOException {
		return createTempFile(prefix, suffix, null);
	}


	// -------- modified methods ----------------

	@Override
	public String getAbsolutePath() {
		if(! MockFramework.isEnabled()){
			return super.getAbsolutePath();
		}
		String absolute = makeAbsolute(getPath());
		File tmp = new File(absolute); //be sure to force actual resolution
		return tmp.getAbsolutePath();
	}

	@Override
	public int compareTo(File pathname) {
		if(! MockFramework.isEnabled()){
			return super.compareTo(pathname);
		}

		return new File(getAbsolutePath()).compareTo(pathname); 
	}

	@Override
	public File getParentFile() {
		if(! MockFramework.isEnabled()){
			return super.getParentFile();
		}
		
		String p = this.getParent();
		if (p == null) return null;
		return new MockFile(p);
	}

	@Override
	public File getAbsoluteFile() {
		if(! MockFramework.isEnabled()){
			return super.getAbsoluteFile();
		}

		String absPath = getAbsolutePath();
		return new MockFile(absPath);
	}

	@Override
	public File getCanonicalFile() throws IOException {
		if(! MockFramework.isEnabled()){
			return super.getCanonicalFile();
		}
		
		String canonPath = getCanonicalPath();
		
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(getAbsolutePath());
		
		return new MockFile(canonPath);
	}

	@Override
	public boolean canRead() {
		if(! MockFramework.isEnabled()){
			return super.canRead();
		}
		
		FSObject file = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(file==null){
			return false;
		}
		return file.isReadPermission();
	}

	@Override
	public boolean setReadOnly() {
		if(! MockFramework.isEnabled()){
			return super.setReadOnly();
		}
		
		FSObject file = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(file==null){
			return false;
		}
		
		file.setReadPermission(true);
		file.setExecutePermission(false);
		file.setWritePermission(false);
		
		return true; 
	}

	@Override
	public boolean setReadable(boolean readable, boolean ownerOnly) {
		if(! MockFramework.isEnabled()){
			return super.setReadable(readable, ownerOnly);
		}
		
		FSObject file = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(file==null){
			return false;
		}
		
		file.setReadPermission(readable);
		return true;
	}

	@Override
	public boolean canWrite() {
		if(! MockFramework.isEnabled()){
			return super.canWrite();
		}
		
		FSObject file = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(file==null){
			return false;
		}
		
		return file.isWritePermission();
	}

	@Override
	public boolean setWritable(boolean writable, boolean ownerOnly) {
		if(! MockFramework.isEnabled()){
			return super.setWritable(writable, ownerOnly);
		}
		
		FSObject file = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(file==null){
			return false;
		}
		
		file.setWritePermission(writable);
		return true;
	}

	@Override
	public boolean setExecutable(boolean executable, boolean ownerOnly) {
		if(! MockFramework.isEnabled()){
			return super.setExecutable(executable, ownerOnly);
		}
		
		FSObject file = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(file==null){
			return false;
		}
		
		file.setExecutePermission(executable);
		return true;
	}

	@Override
	public boolean canExecute() {
		if(! MockFramework.isEnabled()){
			return super.canExecute();
		}
		
		FSObject file = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(file==null){
			return false;
		}
		
		return file.isExecutePermission();
	}

	@Override
	public boolean exists() {
		if(! MockFramework.isEnabled()){
			return super.exists();
		}
		
		return VirtualFileSystem.getInstance().exists(getAbsolutePath());
	}

	@Override
	public boolean isDirectory() {
		if(! MockFramework.isEnabled()){
			return super.isDirectory();
		}
		
		FSObject file = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(file==null){
			return false;
		}
		
		return file.isFolder();
	}

	@Override
	public boolean isFile() {
		if(! MockFramework.isEnabled()){
			return super.isFile();
		}
		return !isDirectory();
	}

	@Override
	public boolean isHidden() {
		if(! MockFramework.isEnabled()){
			return super.isHidden();
		}
		
		if(getName().startsWith(".")){
			//this is not necessarily true in Windows
			return true;
		} else {
			return false; 
		}
	}

	@Override
	public boolean setLastModified(long time) {
		if(! MockFramework.isEnabled()){
			return super.setLastModified(time);
		}
		
		if (time < 0){
        		throw new MockIllegalArgumentException("Negative time");
        }
        
		FSObject target = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(target==null){
			return false;
		}

		return target.setLastModified(time);
	}

	@Override
	public long lastModified() {
		if(! MockFramework.isEnabled()){
			return super.lastModified();
		}
		
		FSObject target = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(target==null){
			return 0;
		}

		return target.getLastModified(); 
	}

	@Override
	public long length() {
		if(! MockFramework.isEnabled()){
			return super.length();
		}
		
		FSObject target = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(target==null){
			return 0;
		}

		if(target.isFolder() || target.isDeleted()){
			return 0;
		}
		
		VFile file = (VFile) target;
		
		return file.getDataSize(); 
	}

	//following 3 methods are never used in SF110
	
	@Override
	public long getTotalSpace() {
		if(! MockFramework.isEnabled()){
			return super.getTotalSpace();
		}
		return 0; //TODO
	}

	@Override
	public long getFreeSpace() {
		if(! MockFramework.isEnabled()){
			return super.getFreeSpace();
		}
		return 0; //TODO
	}

	@Override
	public long getUsableSpace() {
		if(! MockFramework.isEnabled()){
			return super.getUsableSpace();
		}
		return 0; //TODO
	}

	@Override
	public boolean createNewFile() throws IOException {
		if(! MockFramework.isEnabled()){
			return super.createNewFile();
		}
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(getAbsolutePath()); 
		return VirtualFileSystem.getInstance().createFile(getAbsolutePath());
	}

	@Override
	public boolean delete() {
		if(! MockFramework.isEnabled()){
			return super.delete();
		}
		return VirtualFileSystem.getInstance().deleteFSObject(getAbsolutePath()); 
	}

	@Override
	public boolean renameTo(File dest) {
		if(! MockFramework.isEnabled()){
			return super.renameTo(dest);
		}
		boolean renamed = VirtualFileSystem.getInstance().rename(
				this.getAbsolutePath(), 
				dest.getAbsolutePath());
		
		return renamed;
	}

	@Override
	public boolean mkdir() {
		if(! MockFramework.isEnabled()){
			return super.mkdir();
		}
		String parent = this.getParent();
		if(parent==null || !VirtualFileSystem.getInstance().exists(parent)){
			return false;
		}
		return VirtualFileSystem.getInstance().createFolder(getAbsolutePath());
	}

	@Override
	public void deleteOnExit() {
		if(! MockFramework.isEnabled()){
			super.deleteOnExit();
		}
		/*
		 * do nothing, as anyway no actual file is created
		 */
	}

	@Override
	public String[] list() {
		if(! MockFramework.isEnabled()){
			return super.list();
		}
		if(!isDirectory() || !exists()){
			return null; 
		} else {
			VFolder dir = (VFolder) VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
			return dir.getChildrenNames();
		}
	}

	@Override
	public File[] listFiles() {
		if(! MockFramework.isEnabled()){
			return super.listFiles();
		}
		String[] ss = list();
		if (ss == null) return null;
		int n = ss.length;
		MockFile[] fs = new MockFile[n];
		for (int i = 0; i < n; i++) {
			fs[i] = new MockFile(this,ss[i]);
		}
		return fs;
	}

	@Override
	public File[] listFiles(FileFilter filter) {
		if(! MockFramework.isEnabled()){
			return super.listFiles(filter);
		}
		String ss[] = list();
		if (ss == null) return null;
		ArrayList<File> files = new ArrayList<File>();
		for (String s : ss) {
			File f = new MockFile(this,s);
			if ((filter == null) || filter.accept(f))
				files.add(f);
		}
		return files.toArray(new File[files.size()]);
	}


    @Override
    public String getCanonicalPath() throws IOException {
        if(! MockFramework.isEnabled()){
            return super.getCanonicalPath();
        }
        VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(getAbsolutePath());
        return super.getCanonicalPath();
    }

    @Override
    public URL toURL() throws MalformedURLException {
        if(! MockFramework.isEnabled() || !RuntimeSettings.useVNET){
            return super.toURL();
        }
        URL url = super.toURL();
        return MockURL.URL(url.toString());
    }


    // -------- unmodified methods --------------

	@Override
	public String getName(){
		return super.getName();
	}

	@Override
	public String getParent() {
		return super.getParent();
	}

	@Override
	public String getPath() {
		return super.getPath();
	}

	@Override
	public boolean isAbsolute() {
		return super.isAbsolute();
	}

	@Override
	public URI toURI() {
		return super.toURI(); //no need of VNET here
	}

	@Override
	public String[] list(FilenameFilter filter) {
		//no need to mock it, as it uses the mocked list()
		return super.list(filter); 
	}

	@Override
	public boolean mkdirs() {
		//no need to mock it, as all methods it calls are mocked
		return super.mkdirs();  
	}

	@Override
	public boolean setWritable(boolean writable) {
		return super.setWritable(writable); // it calls mocked method
	}

	@Override
	public boolean setReadable(boolean readable) {
		return super.setReadable(readable); //it calls mocked method
	}

	@Override
	public boolean setExecutable(boolean executable) {
		return super.setExecutable(executable); // it calls mocked method
	}
	
	
	// ------- Object methods -----------
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public String toString() {
        return super.toString();
    }
}

