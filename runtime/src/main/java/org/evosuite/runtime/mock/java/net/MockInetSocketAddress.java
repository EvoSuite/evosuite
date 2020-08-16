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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockInetSocketAddress extends InetSocketAddress implements OverrideMock{

	private static final Logger logger = LoggerFactory.getLogger(MockInetSocketAddress.class);

	private static final long serialVersionUID = 5076001401234631237L;


	public MockInetSocketAddress(int port) {
		this( MockFramework.isEnabled() ?
                    MockInetAddress.anyLocalAddress() :
                    NetReflectionUtil.anyLocalAddress()
                        , port);
	}

	public MockInetSocketAddress(InetAddress addr, int port) {
		super(addr == null ?
                (MockFramework.isEnabled() ?
                        MockInetAddress.anyLocalAddress() :
                        NetReflectionUtil.anyLocalAddress())
                : addr, port);
	}

	public MockInetSocketAddress(String hostname, int port) {
		super(MockFramework.isEnabled() ?
                        getResolvedAddressed(hostname).getHostAddress() :
                    hostname
                , port);
		/*
		 * TODO we are not mocking this constructor properly.
		 * We should use reflection to modify the state of 
		 * parent's holder variable.
		 * But it would be bit complicated, and not so important,
		 * as case we do not handle should be anyway rare, ie
		 * fail to resolve hostname (see DNS)
		 * 
		 * 
		checkHost(hostname);
		InetAddress addr = null;
		String host = null;
		try {
			addr = InetAddress.getByName(hostname);
		} catch(UnknownHostException e) {
			host = hostname;
		}
		holder = new InetSocketAddressHolder(host, addr, checkPort(port));
		*/
	}

    private static InetAddress getResolvedAddressed(String hostname){
        checkHost(hostname);
        try {
            return MockInetAddress.getByName(hostname);
        } catch(UnknownHostException e) {
            logger.warn("EvoSuite limitation: unsupported case of hostname resolution for "+hostname);
            return null;
        }
    }

    // private constructor for creating unresolved instances
	/*
	private MockInetSocketAddress(int port, String hostname) {
		holder = new InetSocketAddressHolder(hostname, null, port);
	}
	 */
	
	//--------------------------------------------------------------
	
	public static InetSocketAddress createUnresolved(String host, int port) {
		//no need to create a mock instance? likely not
		return InetSocketAddress.createUnresolved(host, port);
		//return new MockInetSocketAddress(checkPort(port), checkHost(host));
	}

    private static String checkHost(String hostname) {
        if (hostname == null)
            throw new IllegalArgumentException("hostname can't be null");
        return hostname;
    }


	/*  
	 * Those are all final
	 * 
	public final int getPort() {
		return holder.getPort();
	}

	public final InetAddress getAddress() {
		return holder.getAddress();
	}

	public final String getHostName() {
		return holder.getHostName();
	}

	public final String getHostString() {
		return holder.getHostString();
	}

	public final boolean isUnresolved() {
		return holder.isUnresolved();
	}

	@Override
	public String toString() {
		return holder.toString();
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == null || !(obj instanceof InetSocketAddress))
			return false;
		return holder.equals(((InetSocketAddress) obj).holder);
	}

	@Override
	public final int hashCode() {
		return holder.hashCode();
	}
	*/
}

