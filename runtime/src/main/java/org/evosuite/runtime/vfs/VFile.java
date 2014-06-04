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


	public synchronized void setLength(int newLength){

		/*
		 * Note: this implementation is not particularly efficient...
		 * but setLength is rarely called
		 */

		while(newLength > data.size()){
			data.add((byte)0);
		}

		if(newLength==0){
			data.clear();
			return;
		}
		
		while(data.size() > newLength){
			data.remove(data.size()-1);
		}
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

	public synchronized int writeBytes(byte b[], int off, int len){
		return writeBytes(data.size(),b,off,len);
	}

	
	public synchronized int writeBytes(int position, byte b[], int off, int len) throws IllegalArgumentException{

		if(position<0){
			throw new IllegalArgumentException("Position in the file cannot be negative");
		}

		if(deleted || !isWritePermission()){
			return 0;
		}

		if(position >= data.size()){
			setLength(position);
		}

		int written = 0;
		for(int i=off; i<b.length & (i-off)<len; i++){
			if(position < data.size()){
				data.set(position,(b[i]));
			} else {
				data.add(b[i]);
			}
			position++;
			written++;
		}

		setLastModified(getCurrentTimeMillis());

		return written;
	}


	@Override
	public synchronized boolean delete(){
		eraseData();
		return super.delete();
	}
}
