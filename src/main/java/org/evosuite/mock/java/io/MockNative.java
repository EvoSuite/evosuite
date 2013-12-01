package org.evosuite.mock.java.io;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.evosuite.runtime.VirtualFileSystem;
import org.evosuite.runtime.vfs.FSObject;
import org.evosuite.runtime.vfs.VFile;

/**
 * This class is used to mock native methods regarding I/O, and 
 * it also provide support functions
 * 
 * @author arcuri
 *
 */
public class MockNative {

	
	public static VFile getFileForReading(String path){
		FSObject target = VirtualFileSystem.getInstance().findFSObject(path);
		if(target==null || target.isDeleted() || target.isFolder() || !target.isReadPermission()){
			return null;
		}
		return (VFile) target;
	}
	
	public static int read(String path, AtomicInteger position) throws IOException{
		VFile vf = MockNative.getFileForReading(path);
		if(vf==null){
			throw new IOException();
		}
		
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);
		
		int b = vf.read(position.getAndIncrement());
				
		return b; 
	}
	
}
