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
package org.evosuite.runtime.mock.java.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.UnknownHostException;

import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;


public abstract class MockURLStreamHandler extends URLStreamHandler implements OverrideMock{

	protected abstract  URLConnection openConnection(URL u) throws IOException;

	@Override
	protected synchronized InetAddress getHostAddress(URL u) {
        if(! MockFramework.isEnabled()) {
            return super.getHostAddress(u);
        }

        if (URLUtil.getHostAddress(u) != null)
			return URLUtil.getHostAddress(u);

		String host = u.getHost();
		if (host == null || host.equals("")) {
			return null;
		} else {
			try {
				URLUtil.setHostAddress(u, MockInetAddress.getByName(host));
			} catch (UnknownHostException ex) {
				return null;
			} catch (SecurityException se) {
				return null;
			}
		}
		return URLUtil.getHostAddress(u);
	}

	/*
	 * Following methods do not need to be mocked
	 */
	
	@Override
	protected URLConnection openConnection(URL u, Proxy p) throws IOException {
		throw new UnsupportedOperationException("Method not implemented.");
	}

	@Override
	protected void parseURL(URL u, String spec, int start, int limit) {
		super.parseURL(u, spec, start, limit);		
	}

	@Override
	protected int getDefaultPort() {
		return super.getDefaultPort();
	}

	@Override
	protected boolean equals(URL u1, URL u2) {
		return super.equals(u1, u2);
	}

	@Override
	protected int hashCode(URL u) {
		return super.hashCode(u);
	}

	@Override
	protected boolean sameFile(URL u1, URL u2) {
		return super.sameFile(u1, u2);
	}


	@Override
	protected boolean hostsEqual(URL u1, URL u2) {
		return super.hostsEqual(u1, u2);
	}

	@Override
	protected String toExternalForm(URL u) {
		return super.toExternalForm(u);
	}

	@Override
	protected void setURL(URL u, String protocol, String host, int port,
			String authority, String userInfo, String path,
			String query, String ref) {
		
		super.setURL(u, protocol, host, port, authority, userInfo, path, query, ref);
	}

	@Deprecated
	protected void setURL(URL u, String protocol, String host, int port,
			String file, String ref) {
		super.setURL(u, protocol, host, port, file, ref);
	}

}
