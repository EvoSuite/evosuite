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
import java.io.OutputStream;

import org.evosuite.runtime.vnet.NativeTcp;

/**
 * Class used to create an OutputStream for a virtual socket connection
 * 
 * @author arcuri
 *
 */

public class SocketOut extends OutputStream{

	/**
	 * The TCP connection this stream is representing
	 */
	private final NativeTcp tcp;

	/**
	 * Is this stream for the local socket of the 2-way connection?
	 */
	private final boolean isLocal;

	private volatile boolean closed;


	public SocketOut(NativeTcp tcp, boolean isLocal) {
		super();
		this.tcp = tcp;
		this.isLocal = isLocal;
		closed = false;
	}

	@Override
	public void write(int b) throws IOException{
		checkClosed();
		if(isLocal){
			tcp.writeToRemote((byte)b);
		} else {
			tcp.writeToSUT((byte)b);
		}
	}

	@Override
	public void write(byte b[]) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		super.write(b, off, len);
	}


	@Override
	public void flush() throws IOException {
		checkClosed();
		//nothing to do
	}

	@Override
	public void close() throws IOException {
		closed = true;
	}


	private void checkClosed() throws IOException{
		if(closed){
			throw new IOException("Closed stream");
		}
	}
}
