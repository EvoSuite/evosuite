package org.evosuite.runtime.mock.java.net;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

public class EvoURLStreamHandler extends MockURLStreamHandler{

	private final String protocol;

	public EvoURLStreamHandler(String protocol) throws IllegalArgumentException{
		super();
		
		if(protocol==null || protocol.trim().isEmpty()){
			throw new IllegalArgumentException("Null protocol");
		}
		
		this.protocol = protocol.trim().toLowerCase();
	}

	public static boolean isValidProtocol(String protocol){
		if(protocol==null){
			return false;
		}
		
		protocol = protocol.trim().toLowerCase();
		
		//these depend on what in the "sun.net.www.protocol" package
		List<String> list = Arrays.asList("file","ftp","gopher","http","https","jar","mailto","netdoc");
		
		return list.contains(protocol); 
	}
	
	@Override
	protected URLConnection openConnection(URL u) throws IOException {
	
		if(! u.getProtocol().trim().equalsIgnoreCase(this.protocol)){
			//should never happen
			throw new IOException("Error, protocol mismatch: "+u.getProtocol()+" != "+this.protocol);
		}
		
		//TODO
		
		/*
		 * "http/https" need to be treated specially, look at
		 * source code of:
		 * http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/7-b147/sun/net/www/protocol/http/HttpURLConnection.java
		 * 
		 * also "jar", but it is very, very rare (so skip it?)
		 * 
		 * "file" protocol needs to use VFS (if it is active)
		 */
		
		return null;
	}
	
	
}
