package org.evosuite.runtime.mock.java.net;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

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
		return true; //TODO
	}
	
	@Override
	protected URLConnection openConnection(URL u) throws IOException {
	
		if(! u.getProtocol().trim().equalsIgnoreCase(this.protocol)){
			//should never happen
			throw new IOException("Error, protocol mismatch: "+u.getProtocol()+" != "+this.protocol);
		}
		
		//TODO
		
		return null;
	}
	
	
}
