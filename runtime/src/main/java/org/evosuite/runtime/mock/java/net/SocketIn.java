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
import java.io.InputStream;

import org.evosuite.runtime.vnet.NativeTcp;

/**
 * Class used to create an InputStream for a virtual socket connection
 * 
 * @author arcuri
 *
 */
public class SocketIn extends InputStream{

	/**
	 * The TCP connection this stream is representing
	 */
	private final NativeTcp tcp;

	/**
	 * Is this stream for the local socket of the 2-way connection?
	 */
	private final boolean isLocal;

	private volatile boolean closed;


	public SocketIn(NativeTcp tcp, boolean isLocal) throws IllegalArgumentException{
		super();
		
		if(tcp == null){
			throw new IllegalArgumentException("Input connection cannot be null");
		}
		
		this.tcp = tcp;
		this.isLocal = isLocal;
		closed = false;
	}

	@Override
	public int read() throws IOException {
		
		checkClosed();
		
		if(isLocal){
			return tcp.readInSUTfromRemote();
		} else {
			return tcp.readInTestFromSUT();
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return super.read(b,off,len);
	}

	@Override
	public long skip(long n) throws IOException {
		checkClosed();
		return super.skip(n);
	}

	@Override
	public int available() throws IOException {

		checkClosed();

		if(isLocal){
			return tcp.getAmountOfDataInLocalBuffer();
		} else {
			return tcp.getAmountOfDataInRemoteBuffer();
		}
	}

	@Override
	public void close() throws IOException {
		closed = true;
	}


	@Override
	public synchronized void mark(int readlimit) {}

	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	private void checkClosed() throws IOException{
		if(closed){
			throw new IOException("Closed stream");
		}
	}
}
