package org.evosuite.runtime.vfs;

import java.io.File;

/**
 * Parent class for both files and folders
 * 
 * @author arcuri
 *
 */
public abstract class FSObject {

	private volatile boolean readPermission;
	
	private volatile boolean writePermission;
	
	private volatile boolean executePermission;
	
	/**
	 * Normalized path uniquely identifying this file on the VFS
	 */
	protected final String path;
	
	protected final VFolder parent;
	
	/**
	 * Even if file is removed from file system, some threads could still have
	 * references to it. So, long/expensive operations could be stopped here if
	 * file is deleted
	 */
	protected volatile boolean deleted;
	
	public FSObject(String path,VFolder parent){		
		readPermission = true;
		writePermission = true;
		executePermission = true;
		this.path = normalizePath(path);	
		this.parent = parent;
		this.deleted = false;
	}

	public boolean delete(){
		parent.removeChild(getName());
		deleted = true;
		return deleted;
	}
	
	public boolean isFolder(){
		return this instanceof VFolder;
	}
	
	public String getName(){
		if(path==null){
			return null;
		}
		return new File(path).getName(); 
	}
	
	public String normalizePath(String rawPath){
		if(rawPath==null){
			return null;
		}
		return new File(rawPath).getAbsolutePath(); 
	}

	public boolean isReadPermission() {
		return readPermission;
	}


	public void setReadPermission(boolean readPermission) {
		this.readPermission = readPermission;
	}


	public boolean isWritePermission() {
		return writePermission;
	}


	public void setWritePermission(boolean writePermission) {
		this.writePermission = writePermission;
	}


	public boolean isExecutePermission() {
		return executePermission;
	}


	public void setExecutePermission(boolean executePermission) {
		this.executePermission = executePermission;
	}

	public String getPath() {
		return path == null ? "" : path;
	}

	/**
	 * Once a file/folder is deleted, it shouldn't be accessible any more from the VFS.
	 * But in case some thread holds a reference to this instance, we need to mark 
	 * that it is supposed to be deleted
	 * @return
	 */
	public boolean isDeleted() {
		return deleted;
	}
	
	@Override
	public String toString(){
		return getPath();
	}
}
