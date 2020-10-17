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

import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.OverrideMock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImplFactory;
import java.net.SocketOptions;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;


public class MockSocket extends Socket implements OverrideMock {
	
	private Object closeLock = new Object();

	private boolean created = false;
	private boolean bound = false;
	private boolean connected = false;
	private boolean closed = false;
	private boolean shutIn = false;
	private boolean shutOut = false;

	MockSocketImpl impl;


	//-------- constructors  ---------------------------

    /**
     * TODO rollback here would be very complicated, as would need to replicate every single
     * constructor code by reflection, as they have side-effects we cannot avoid
     */

	public MockSocket() {
		super();
        if(!MockFramework.isEnabled()) {
            return;
        }
		setImpl();
	}

	public MockSocket(Proxy proxy) {
		// Create a copy of Proxy as a security measure
		if (proxy == null) {
			throw new IllegalArgumentException("Invalid Proxy");
		}
		
		Proxy p =  proxy; 
		//Note: not needed as only used in EvoSuiteSocket constructor
		//  == Proxy.NO_PROXY ? Proxy.NO_PROXY : sun.net.ApplicationProxy.create(proxy);
		
		if (p.type() == Proxy.Type.SOCKS) {
			
			InetSocketAddress epoint = (InetSocketAddress) p.address();
			if (epoint.getAddress() != null) {
				checkAddress (epoint.getAddress(), "Socket");
			}

			impl = new EvoSuiteSocket(p); 
			impl.setSocket(this);
		} else {
			if (p == Proxy.NO_PROXY) {
				impl = new EvoSuiteSocket();
				impl.setSocket(this);
			} else
				throw new IllegalArgumentException("Invalid Proxy");
		}
	}


	protected MockSocket(MockSocketImpl impl) throws SocketException {
		super(impl);
        if(!MockFramework.isEnabled()) {
            return;
        }
        this.impl = impl;
		if (impl != null) {
			this.impl.setSocket(this);
		}
	}


	public MockSocket(String host, int port)throws UnknownHostException, IOException {
		this(host != null ? new MockInetSocketAddress(host, port) :
			new MockInetSocketAddress(MockInetAddress.getByName(null), port),
				null, true);
	}


	public MockSocket(InetAddress address, int port) throws IOException {
		this(address != null ? new MockInetSocketAddress(address, port) : null,
				null, true);
	}


	public MockSocket(String host, int port, InetAddress localAddr,
			int localPort) throws IOException {
		this(host != null ? new MockInetSocketAddress(host, port) :
			new MockInetSocketAddress(MockInetAddress.getByName(null), port),
			new MockInetSocketAddress(localAddr, localPort), true);
	}


	public MockSocket(InetAddress address, int port, InetAddress localAddr,
			int localPort) throws IOException {
		this(address != null ? new MockInetSocketAddress(address, port) : null,
				new MockInetSocketAddress(localAddr, localPort), true);
	}



	public MockSocket(String host, int port, boolean stream) throws IOException {
		this(host != null ? new MockInetSocketAddress(host, port) :
			new MockInetSocketAddress(MockInetAddress.getByName(null), port),
				null, stream);
	}



	public MockSocket(InetAddress host, int port, boolean stream) throws IOException {
		this(host != null ? new MockInetSocketAddress(host, port) : null,
				new MockInetSocketAddress(0), stream);
	}

	private MockSocket(SocketAddress address, SocketAddress localAddr,
			boolean stream) throws IOException {
		setImpl();

		// backward compatibility
		if (address == null)
			throw new NullPointerException();

		try {
			createImpl(stream);
			if (localAddr != null)
				bind(localAddr);
			if (address != null)
				connect(address);
		} catch (IOException e) {
			close();
			throw e;
		}
	}


	//---------------------------------------------------------------

	protected void createImpl(boolean stream) throws SocketException {
		if (impl == null)
			setImpl();
		try {
			impl.create(stream);
			created = true;
		} catch (IOException e) {
			throw new SocketException(e.getMessage());
		}
	}

	
	/*
	    in superclass it is package level, so it is not overriden
	 */
	protected void setImpl() {
		impl = new EvoSuiteSocket();
		impl.setSocket(this);
	}



	protected MockSocketImpl getImpl() throws SocketException {
		if (!created)
			createImpl(true);
		return impl;
	}

	/*
	 *  note: super postAccept is final.
	 *  but it is called only in ServerSocket
	 */
	protected void _postAccept() {
		connected = true;
		created = true;
		bound = true;
	}

	protected void setCreated() {
		created = true;
	}

	protected void setBound() {
		bound = true;
	}

	protected void setConnected() {
		connected = true;
	}
	
	//--------   public methods  ----------------
	
	@Override
	public void connect(SocketAddress endpoint) throws IOException {
		connect(endpoint, 0);
	}


	@Override
	public void connect(SocketAddress endpoint, int timeout) throws IOException {
		if (endpoint == null)
			throw new IllegalArgumentException("connect: The address can't be null");

		if (timeout < 0)
			throw new IllegalArgumentException("connect: timeout can't be negative");

		if (isClosed())
			throw new SocketException("Socket is closed");

		if (isConnected())
			throw new SocketException("already connected");

		if (!(endpoint instanceof InetSocketAddress))
			throw new IllegalArgumentException("Unsupported address type");

		InetSocketAddress epoint = (InetSocketAddress) endpoint;
		InetAddress addr = epoint.getAddress();
		checkAddress(addr, "connect");

		if (!created)
			createImpl(true);

        impl.connect(epoint, timeout);

        connected = true;
		/*
		 * If the socket was not bound before the connect, it is now because
		 * the kernel will have picked an ephemeral port & a local address
		 */
		bound = true;
	}


	@Override
	public void bind(SocketAddress bindpoint) throws IOException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		if (isBound())
			throw new SocketException("Already bound");

		if (bindpoint != null && (!(bindpoint instanceof InetSocketAddress)))
			throw new IllegalArgumentException("Unsupported address type");
		InetSocketAddress epoint = (InetSocketAddress) bindpoint;
		if (epoint != null && epoint.isUnresolved())
			throw new SocketException("Unresolved address");
		if (epoint == null) {
			epoint = new MockInetSocketAddress(0);
		}
		InetAddress addr = epoint.getAddress();
		int port = epoint.getPort();
		checkAddress (addr, "bind");
		getImpl().bind (addr, port);
		bound = true;
	}

	private void checkAddress (InetAddress addr, String op) {
		if (addr == null) {
			return;
		}

		if (!(addr instanceof Inet4Address || addr instanceof Inet6Address)) {
			throw new IllegalArgumentException(op + ": invalid address type");
		}
	}



	@Override
	public InetAddress getInetAddress() {
		if (!isConnected())
			return null;
		try {
			return getImpl().getInetAddress();
		} catch (SocketException e) {
		}
		return null;
	}


	@Override
	public InetAddress getLocalAddress() {

		if(!MockFramework.isEnabled()) {
			return super.getLocalAddress();
		}
		
		if (!isBound()) {
			return MockInetAddress.anyLocalAddress();
		}
		
		InetAddress in = null;
		try {
			in = (InetAddress) getImpl().getOption(SocketOptions.SO_BINDADDR);
			if (in.isAnyLocalAddress()) {
				in = MockInetAddress.anyLocalAddress();
			}
		} catch (SecurityException e) {
			in = MockInetAddress.getLoopbackAddress();
		} catch (Exception e) {			
			in = MockInetAddress.anyLocalAddress();
		}
		return in;
	}

	@Override
	public int getPort() {
		if (!isConnected())
			return 0;
		try {
			return getImpl().getPort();
		} catch (SocketException e) {
			// Shouldn't happen as we're connected
		}
		return -1;
	}


	@Override
	public int getLocalPort() {
		if (!isBound())
			return -1;
		try {
			return getImpl().getLocalPort();
		} catch(SocketException e) {
			// shouldn't happen as we're bound
		}
		return -1;
	}


	@Override
	public SocketAddress getRemoteSocketAddress() {
		if (!isConnected())
			return null;
		return new MockInetSocketAddress(getInetAddress(), getPort());
	}


	@Override
	public SocketAddress getLocalSocketAddress() {
		if (!isBound())
			return null;
		return new MockInetSocketAddress(getLocalAddress(), getLocalPort());
	}


	@Override
	public SocketChannel getChannel() {
		return null; //TODO check it
	}


	@Override
	public InputStream getInputStream() throws IOException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		if (!isConnected())
			throw new SocketException("Socket is not connected");
		if (isInputShutdown())
			throw new SocketException("Socket input is shutdown");
		return impl.getInputStream();
	}


	@Override
	public OutputStream getOutputStream() throws IOException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		if (!isConnected())
			throw new SocketException("Socket is not connected");
		if (isOutputShutdown())
			throw new SocketException("Socket output is shutdown");
		return impl.getOutputStream();
	}

	@Override
	public void setTcpNoDelay(boolean on) throws SocketException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		getImpl().setOption(SocketOptions.TCP_NODELAY, on);
	}


	@Override
	public boolean getTcpNoDelay() throws SocketException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		return (Boolean) getImpl().getOption(SocketOptions.TCP_NODELAY);
	}


	@Override
	public void setSoLinger(boolean on, int linger) throws SocketException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		if (!on) {
			getImpl().setOption(SocketOptions.SO_LINGER, on);
		} else {
			if (linger < 0) {
				throw new IllegalArgumentException("invalid value for SO_LINGER");
			}
			if (linger > 65535)
				linger = 65535;
			getImpl().setOption(SocketOptions.SO_LINGER, linger);
		}
	}


	@Override
	public int getSoLinger() throws SocketException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		Object o = getImpl().getOption(SocketOptions.SO_LINGER);
		if (o instanceof Integer) {
			return (Integer) o;
		} else {
			return -1;
		}
	}


	@Override
	public void sendUrgentData (int data) throws IOException  {
		if (!getImpl().supportsUrgentData ()) {
			throw new SocketException ("Urgent data not supported");
		}
		getImpl().sendUrgentData (data);
	}


	@Override
	public void setOOBInline(boolean on) throws SocketException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		getImpl().setOption(SocketOptions.SO_OOBINLINE, on);
	}


	@Override
	public boolean getOOBInline() throws SocketException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		return (Boolean) getImpl().getOption(SocketOptions.SO_OOBINLINE);
	}


	@Override
	public synchronized void setSoTimeout(int timeout) throws SocketException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		if (timeout < 0)
			throw new IllegalArgumentException("timeout can't be negative");

		getImpl().setOption(SocketOptions.SO_TIMEOUT, timeout);
	}


	@Override
	public synchronized int getSoTimeout() throws SocketException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		Object o = getImpl().getOption(SocketOptions.SO_TIMEOUT);
		if (o instanceof Integer) {
			return (Integer) o;
		} else {
			return 0;
		}
	}

	@Override
	public synchronized void setSendBufferSize(int size) throws SocketException{
		if (!(size > 0)) {
			throw new IllegalArgumentException("negative send size");
		}
		if (isClosed())
			throw new SocketException("Socket is closed");
		getImpl().setOption(SocketOptions.SO_SNDBUF, size);
	}

	@Override
	public synchronized int getSendBufferSize() throws SocketException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		int result = 0;
		Object o = getImpl().getOption(SocketOptions.SO_SNDBUF);
		if (o instanceof Integer) {
			result = (Integer) o;
		}
		return result;
	}


	@Override
	public synchronized void setReceiveBufferSize(int size) throws SocketException{
		if (size <= 0) {
			throw new IllegalArgumentException("invalid receive size");
		}
		if (isClosed())
			throw new SocketException("Socket is closed");
		getImpl().setOption(SocketOptions.SO_RCVBUF, size);
	}


	@Override
	public synchronized int getReceiveBufferSize() throws SocketException{
		if (isClosed())
			throw new SocketException("Socket is closed");
		int result = 0;
		Object o = getImpl().getOption(SocketOptions.SO_RCVBUF);
		if (o instanceof Integer) {
			result = (Integer) o;
		}
		return result;
	}


	@Override
	public void setKeepAlive(boolean on) throws SocketException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		getImpl().setOption(SocketOptions.SO_KEEPALIVE, on);
	}


	@Override
	public boolean getKeepAlive() throws SocketException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		return (Boolean) getImpl().getOption(SocketOptions.SO_KEEPALIVE);
	}


	@Override
	public void setTrafficClass(int tc) throws SocketException {
		if (tc < 0 || tc > 255)
			throw new IllegalArgumentException("tc is not in range 0 -- 255");

		if (isClosed())
			throw new SocketException("Socket is closed");
		getImpl().setOption(SocketOptions.IP_TOS, tc);
	}

	@Override
	public int getTrafficClass() throws SocketException {
		return (Integer) (getImpl().getOption(SocketOptions.IP_TOS));
	}

	@Override
	public void setReuseAddress(boolean on) throws SocketException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		getImpl().setOption(SocketOptions.SO_REUSEADDR, on);
	}

	@Override
	public boolean getReuseAddress() throws SocketException {
		if (isClosed())
			throw new SocketException("Socket is closed");
		return (Boolean) (getImpl().getOption(SocketOptions.SO_REUSEADDR));
	}

	
	@Override
	public synchronized void close() throws IOException {
		synchronized(closeLock) {
			if (isClosed())
				return;
			if (created)
				impl.close();
			closed = true;
		}
	}

	@Override
	public void shutdownInput() throws IOException
	{
		if (isClosed())
			throw new SocketException("Socket is closed");
		if (!isConnected())
			throw new SocketException("Socket is not connected");
		if (isInputShutdown())
			throw new SocketException("Socket input is already shutdown");
		getImpl().shutdownInput();
		shutIn = true;
	}

	@Override
	public void shutdownOutput() throws IOException
	{
		if (isClosed())
			throw new SocketException("Socket is closed");
		if (!isConnected())
			throw new SocketException("Socket is not connected");
		if (isOutputShutdown())
			throw new SocketException("Socket output is already shutdown");
		getImpl().shutdownOutput();
		shutOut = true;
	}

	@Override
	public String toString() {
		try {
			if (isConnected())
				return "Socket[addr=" + getImpl().getInetAddress() +
						",port=" + getImpl().getPort() +
						",localport=" + getImpl().getLocalPort() + "]";
		} catch (SocketException e) {
		}
		return "Socket[unconnected]";
	}

	// -------- boolean methods --------
	
	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean isBound() {
		return bound;
	}

	@Override
	public boolean isClosed() {
		synchronized(closeLock) {
			return closed;
		}
	}

	@Override
	public boolean isInputShutdown() {
		return shutIn;
	}

	@Override
	public boolean isOutputShutdown() {
		return shutOut;
	}

	//---------------------------------------
	
	//private static SocketImplFactory factory = null;

	public static synchronized void setSocketImplFactory(SocketImplFactory fac) throws IOException{

		/*
		 * Having a factory would be very tricky, as returned instances are not of type MockSocketImpl.
		 * Even if where to change "impl" into SocketImpl, then we wouldn't be able to call all its package
		 * level methods used in this class. We could use reflection though.
		 * Anyway, as this method is never used in SF110, we just throw a legal exception 
		 */
		throw new IOException("Setting of factory is not supported in virtual network");


		/*
		if (factory != null) {
			throw new SocketException("factory already defined");
		}
		SecurityManager security = System.getSecurityManager();
		if (security != null) {
			security.checkSetFactory();
		}
		factory = fac;
		 */
	}

	@Override
	public void setPerformancePreferences(int connectionTime,
			int latency,
			int bandwidth) {
		super.setPerformancePreferences(connectionTime, latency, bandwidth);
	}

}
