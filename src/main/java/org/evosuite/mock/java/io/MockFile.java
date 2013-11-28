package org.evosuite.mock.java.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import org.evosuite.runtime.VirtualFileSystem;
import org.evosuite.runtime.vfs.FSObject;
import org.evosuite.runtime.vfs.VFile;
import org.evosuite.runtime.vfs.VFolder;

/**
 * This class is used in the mocking framework to replace File instances.
 * 
 * <p>
 * All files are created in memory, and no access to disk is ever done
 *   
 * @author arcuri
 *
 */
public class MockFile extends File{

	private static final long serialVersionUID = -8217763202925800733L;

	/*
	 *  Constructors, with same inputs as in File. Note: it is not possible to inherit JavaDocs for constructors.
	 */

	public MockFile(String pathname) {
		super(pathname);
	}

	public MockFile(String parent, String child) {
		super(parent,child);
	}

	public MockFile(File parent, String child) {
		this(parent.getPath(),child);
	}

	public MockFile(URI uri) {
		super(uri);
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
		File[] roots = File.listRoots();
		MockFile[] mocks = new MockFile[roots.length];
		for(int i=0; i<roots.length; i++){
			mocks[i] = new MockFile(roots[i].getAbsolutePath());
		}
		return mocks; 
	}

	public static File createTempFile(String prefix, String suffix, File directory)
			throws IOException{
		
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded("");
		
		String path = VirtualFileSystem.getInstance().createTempFile(prefix, suffix, directory);
		if(path==null){
			throw new IOException();
		}
		return new MockFile(path); 
	}

	public static File createTempFile(String prefix, String suffix)
			throws IOException {
		return createTempFile(prefix, suffix, null);
	}


	// -------- modified methods ----------------

	@Override
	public int compareTo(File pathname) {
		return new File(getAbsolutePath()).compareTo(pathname); 
	}

	@Override
	public File getParentFile() {
		String p = this.getParent();
		if (p == null) return null;
		return new MockFile(p);
	}

	@Override
	public File getAbsoluteFile() {
		String absPath = getAbsolutePath();
		return new MockFile(absPath);
	}

	@Override
	public File getCanonicalFile() throws IOException {
		String canonPath = getCanonicalPath();
		
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(getAbsolutePath());
		
		return new MockFile(canonPath);
	}

	@Override
	public boolean canRead() {
		FSObject file = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(file==null){
			return false;
		}
		return file.isReadPermission();
	}

	@Override
	public boolean setReadOnly() {
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
		FSObject file = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(file==null){
			return false;
		}
		
		file.setReadPermission(readable);
		return true;
	}

	@Override
	public boolean canWrite() {
		FSObject file = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(file==null){
			return false;
		}
		
		return file.isWritePermission();
	}

	@Override
	public boolean setWritable(boolean writable, boolean ownerOnly) {
		FSObject file = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(file==null){
			return false;
		}
		
		file.setWritePermission(writable);
		return true;
	}

	@Override
	public boolean setExecutable(boolean executable, boolean ownerOnly) {
		FSObject file = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(file==null){
			return false;
		}
		
		file.setExecutePermission(executable);
		return true;
	}

	@Override
	public boolean canExecute() {
		FSObject file = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(file==null){
			return false;
		}
		
		return file.isExecutePermission();
	}

	@Override
	public boolean exists() {
		return VirtualFileSystem.getInstance().exists(getAbsolutePath());
	}

	@Override
	public boolean isDirectory() {
		FSObject file = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(file==null){
			return false;
		}
		
		return file.isFolder();
	}

	@Override
	public boolean isFile() {
		return !isDirectory();
	}

	@Override
	public boolean isHidden() {
		if(getName().startsWith(".")){
			//this is not necessarily true in Windows
			return true;
		} else {
			return false; 
		}
	}

	@Override
	public boolean setLastModified(long time) {
        if (time < 0){
        		throw new IllegalArgumentException("Negative time");
        }
        
		FSObject target = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(target==null){
			return false;
		}

		return target.setLastModified(time);
	}

	@Override
	public long lastModified() {
		FSObject target = VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
		if(target==null){
			return 0;
		}

		return target.getLastModified(); 
	}

	@Override
	public long length() {
	
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
		return 0; //TODO
	}

	@Override
	public long getFreeSpace() {
		return 0; //TODO
	}

	@Override
	public long getUsableSpace() {
		return 0; //TODO
	}

	@Override
	public boolean createNewFile() throws IOException {
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(getAbsolutePath()); 
		return VirtualFileSystem.getInstance().createFile(getAbsolutePath());
	}

	@Override
	public boolean delete() {
		return VirtualFileSystem.getInstance().deleteFSObject(getAbsolutePath()); 
	}

	@Override
	public boolean renameTo(File dest) {				
		boolean renamed = VirtualFileSystem.getInstance().rename(
				this.getAbsolutePath(), 
				dest.getAbsolutePath());
		
		return renamed;
	}

	@Override
	public boolean mkdir() {
		String parent = this.getParent();
		if(parent==null || !VirtualFileSystem.getInstance().exists(parent)){
			return false;
		}
		return VirtualFileSystem.getInstance().createFolder(getAbsolutePath());
	}

	@Override
	public void deleteOnExit() {
		/*
		 * do nothing, as anyway no actual file is created
		 */
	}

	@Override
	public String[] list() {
		if(!isDirectory() || !exists()){
			return null; 
		} else {
			VFolder dir = (VFolder) VirtualFileSystem.getInstance().findFSObject(getAbsolutePath());
			return dir.getChildrenNames();
		}
	}

	@Override
	public File[] listFiles() {
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
	public String getAbsolutePath() {
		return super.getAbsolutePath();
	}

	@Override
	public String getCanonicalPath() throws IOException {
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(getAbsolutePath()); 
		return super.getCanonicalPath();
	}

	@Override
	public URL toURL() throws MalformedURLException {
		return super.toURL();
	}

	@Override
	public URI toURI() {
		return super.toURI();
	}

	@Override
	public String[] list(FilenameFilter filter) {
		return super.list(filter);
	}

	@Override
	public boolean mkdirs() {
		return super.mkdirs();
	}

	@Override
	public boolean setWritable(boolean writable) {
		return super.setWritable(writable);
	}

	@Override
	public boolean setReadable(boolean readable) {
		return super.setReadable(readable);
	}

	@Override
	public boolean setExecutable(boolean executable) {
		return super.setExecutable(executable);
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

