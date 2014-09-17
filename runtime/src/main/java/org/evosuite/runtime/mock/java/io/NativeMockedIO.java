package org.evosuite.runtime.mock.java.io;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.evosuite.runtime.vfs.FSObject;
import org.evosuite.runtime.vfs.VFile;
import org.evosuite.runtime.vfs.VirtualFileSystem;

/**
 * This class is used to mock native methods regarding I/O, and 
 * it also provide support functions used by different I/O mocks
 * 
 * <p>
 * All methods declaring IOException might throw it depending on simulation / test data 
 * 
 * @author arcuri
 *
 */
public class NativeMockedIO {

	
	public static VFile getFileForReading(String path){
		FSObject target = VirtualFileSystem.getInstance().findFSObject(path);
		if(target==null || target.isDeleted() || target.isFolder() || !target.isReadPermission()){
			return null;
		}
		return (VFile) target;
	}
	
	public static int read(String path, AtomicInteger position) throws IOException{
		VFile vf = NativeMockedIO.getFileForReading(path);
		if(vf==null){
			throw new MockIOException();
		}
		
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);
		
		int b = vf.read(position.getAndIncrement());
				
		return b; 
	}

	
	public static VFile getFileForWriting(String path){
		FSObject target = VirtualFileSystem.getInstance().findFSObject(path);
		if(target==null || target.isDeleted() || target.isFolder() || !target.isWritePermission()){
			return null;
		}
		return (VFile) target;
	}	
	
	
	public static void writeBytes(String path, AtomicInteger position, byte b[], int off, int len)
			throws IOException{
		
		VFile vf = NativeMockedIO.getFileForWriting(path);
		if(vf==null){
			throw new MockIOException();
		}
				
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);
				
		int written = vf.writeBytes(position.get(),b, off, len);
		if(written==0){
			throw new MockIOException("Error in writing to file");
		}
		position.addAndGet(written);
	}

	
	public static int size(String path) throws IOException{
		VFile vf = NativeMockedIO.getFileForReading(path);
		if(vf==null){
			throw new MockIOException();
		}
		
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);
		
		return vf.getDataSize();
	}
	
	
	public static void setLength(String path, AtomicInteger position, long newLength) throws IOException{
		if(newLength < 0){
			throw new MockIOException("Negative position: "+newLength);
		}
		if(newLength > Integer.MAX_VALUE){
			throw new MockIOException("Virtual file system does not handle files larger than  "+Integer.MAX_VALUE+" bytes");
		}
		
		VFile vf = NativeMockedIO.getFileForWriting(path);
		if(vf==null){
			throw new MockIOException();
		}
		
		VirtualFileSystem.getInstance().throwSimuledIOExceptionIfNeeded(path);
		
		vf.setLength((int)newLength);
		
		if(position.get() > newLength){
			position.set((int)newLength);
		}		
	}
}
