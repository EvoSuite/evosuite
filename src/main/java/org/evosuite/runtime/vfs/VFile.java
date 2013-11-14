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

	private final List<Byte> data;
	
	public VFile(String path, VFolder parent) {
		super(path, parent);
		
		//TODO might need a better type of data structure supporting multi-threading
		data = new ArrayList<Byte>();
	}
	
	public void append(byte[] v){
		for(byte b : v){
			data.add(b);
		}
	}
}
