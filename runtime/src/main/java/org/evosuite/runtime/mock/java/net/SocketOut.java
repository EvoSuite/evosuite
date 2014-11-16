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
