/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
        if(port == 0){
            throw new IllegalArgumentException("Cannot specify an unbound port 0 in a test");
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
