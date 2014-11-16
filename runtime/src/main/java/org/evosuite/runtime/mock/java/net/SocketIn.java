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
	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
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
