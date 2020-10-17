/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
		
		if(protocol==null || protocol.trim().isEmpty()) {
			throw new IllegalArgumentException("Null protocol");
		}
		
		this.protocol = protocol.trim().toLowerCase();
	}

	public static boolean isValidProtocol(String protocol) {
		if(protocol==null) {
			return false;
		}
		
		protocol = protocol.trim().toLowerCase();
		
		//these depend on what in the "sun.net.www.protocol" package
		List<String> list = Arrays.asList("file","ftp","gopher","http","https","jar","mailto","netdoc");
		
		return list.contains(protocol); 
	}
	
	@Override
	protected URLConnection openConnection(URL u) throws IOException {

		if(!u.getProtocol().trim().equalsIgnoreCase(this.protocol)) {
			//should never happen
			throw new IOException("Error, protocol mismatch: "+u.getProtocol()+" != "+this.protocol);
		}

        if(protocol.equals("http") || protocol.equals("https")) {
            return new EvoHttpURLConnection(u);
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
