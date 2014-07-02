package org.evosuite.runtime.mock.java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;

public class EvoSuiteSocket extends MockSocketImpl{

	@Override
	public void setOption(int optID, Object value) throws SocketException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getOption(int optID) throws SocketException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void create(boolean stream) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void connect(String host, int port) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void connect(InetAddress address, int port) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void connect(SocketAddress address, int timeout)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void bind(InetAddress host, int port) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void listen(int backlog) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void accept(SocketImpl s) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int available() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void sendUrgentData(int data) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
