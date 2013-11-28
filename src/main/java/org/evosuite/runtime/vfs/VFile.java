package org.evosuite.runtime.vfs;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a virtual file
 * 
 * @author arcuri
 *
 */
public class VFile extends FSObject{

	/**
	 * the actual data contained in file as a list of bytes
	 */
	private final List<Byte> data;
	
	public VFile(String path, VFolder parent) {
		super(path, parent);
		
		//TODO might need a better type of data structure supporting multi-threading
		data = new ArrayList<Byte>(1024);
	}
	
	public void eraseData(){
		data.clear();
	}
	
	public synchronized int getDataSize(){
		return data.size();
	}
	
	public synchronized int read(int position) throws IllegalArgumentException{
		if(position<0){
			throw new IllegalArgumentException("Position in the file cannot be negative");
		}
		
		if(position >= data.size()){
			return -1; //this represent the end of the stream
		}
		
		return data.get(position);
	}
	
	public synchronized boolean writeBytes(byte b[], int off, int len, boolean append) {
		if(deleted || !isWritePermission()){
			return false;
		}
		
		if(!append){
			eraseData();
		}
		
		for(int i=off; i<b.length & (i-off)<len; i++){
			data.add(b[i]);
		}

		setLastModified(java.lang.System.currentTimeMillis());
		
		return true;
	}
	
	@Override
	public synchronized boolean delete(){
		eraseData();
		return super.delete();
	}
}
