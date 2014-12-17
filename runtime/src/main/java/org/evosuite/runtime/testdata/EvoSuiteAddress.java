package org.evosuite.runtime.testdata;

import java.io.Serializable;

/**
 * A object wrapper for host/port addresses accessed by the SUTs 
 * 
 * @author arcuri
 *
 */
public abstract class EvoSuiteAddress implements Serializable {

	private static final long serialVersionUID = 1734299467948600797L;

	private final String host;
	private final int port;
	
	
	public EvoSuiteAddress(String host, int port) throws IllegalArgumentException{
		super();
		/*
		 * actually, we could do more input validation. 
		 * but as those values are not really part of the search, it should be fine
		 */
		if(host==null){
			throw new IllegalArgumentException("Host should not be null");
		}
		if(port < 0){
			throw new IllegalArgumentException("Port cannot be negative");
		}
		this.host = host;
		this.port = port;
	}


	public String getHost() {
		return host;
	}


	public int getPort() {
		return port;
	}
	
	
}
