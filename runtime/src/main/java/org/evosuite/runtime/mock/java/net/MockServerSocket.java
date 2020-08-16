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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImplFactory;
import java.net.SocketOptions;
import java.nio.channels.ServerSocketChannel;

/**
 *
 * Created by arcuri on 6/29/14.
 */
public class MockServerSocket extends ServerSocket implements OverrideMock {

	private boolean created = false;
	private boolean bound = false;
	private boolean closed = false;
	private Object closeLock = new Object();
	private boolean oldImpl = false;

	private MockSocketImpl impl;


	//--------- constructors -------------

	/**
	 *  Non-visible (package-level) constructor
	 *
	MockServerSocket(MockSocketImpl impl) {
		this.impl = impl;
		impl.setServerSocket(this);
	}
	 */

	public MockServerSocket() throws IOException {
		super();
		/*
		 * the super constructor is only calling setImpl, which is implemented with:
		 
		  	if (factory != null) {
            		impl = factory.createSocketImpl();
            		checkOldImpl();
        		} else {
            		// No need to do a checkOldImpl() here, we know it's an up to date
            		// SocketImpl!
            		impl = new SocksSocketImpl();
        		}
        		if (impl != null)
            		impl.setServerSocket(this);
            		
         * ie, the only side effect is to set the variable "impl"            		
		 */
		setImpl();
	}

	public MockServerSocket(int port) throws IOException {
		this(port, 50, null);
	}


	public MockServerSocket(int port, int backlog) throws IOException {
		this(port, backlog, null);
	}

	public MockServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
		this();

        if (port < 0 || port > 0xFFFF)
			throw new IllegalArgumentException("Port value out of range: " + port);

        if (backlog < 1)
			backlog = 50;

        try {
            if(MockFramework.isEnabled()) {
                bind(new MockInetSocketAddress(bindAddr, port), backlog);
            } else {
                Method setImplMethod = ServerSocket.class.getDeclaredMethod("setImpl");
                setImplMethod.setAccessible(true);
                setImplMethod.invoke(this);
                super.bind(new InetSocketAddress(bindAddr, port), backlog);
            }
		} catch(SecurityException e) {
            close();
            throw e;
        } catch(IOException e) {
			close();
			throw e;
		} catch (NoSuchMethodException | IllegalAccessException e ) {
            //should never happen
            throw new RuntimeException("ERROR in EvoSuite: "+e,e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

	//-------------------------------------------------

	public static synchronized void setSocketFactory(SocketImplFactory fac) throws IOException {
		//for explanation, see MockSocket
		throw new IOException("Setting of factory is not supported in virtual network");
	}

	private void setImpl() {		
		impl = new EvoSuiteSocket();		
		impl.setServerSocket(this);
	}
	
	private MockSocketImpl getImpl() throws SocketException {
		if (!created)
			createImpl();
		return impl;
	}

    protected void setBound() {
        bound = true;
    }

    protected void setCreated() {
        created = true;
    }

	
	private void createImpl() throws SocketException {
		if (impl == null)
			setImpl();
		try {
			impl.create(true);
			created = true;
		} catch (IOException e) {
			throw new SocketException(e.getMessage());
		}
	}

    @Override
	public void bind(SocketAddress endpoint) throws IOException {
		if(!MockFramework.isEnabled()){
            super.bind(endpoint);
            return;
        }
        bind(endpoint, 50);
	}

    @Override
	public void bind(SocketAddress endpoint, int backlog) throws IOException {
        if(!MockFramework.isEnabled()){
            super.bind(endpoint,backlog);
            return;
        }

        if (isClosed())
			throw new SocketException("Socket is closed");
		if (!oldImpl && isBound())
			throw new SocketException("Already bound");
		if (endpoint == null)
			endpoint = new MockInetSocketAddress(0);
		if (!(endpoint instanceof InetSocketAddress))
			throw new IllegalArgumentException("Unsupported address type");
		
		InetSocketAddress epoint = (InetSocketAddress) endpoint;
		if (epoint.isUnresolved())
			throw new SocketException("Unresolved address");
		if (backlog < 1)
			backlog = 50;
		
		try {			
			getImpl().bind(epoint.getAddress(), epoint.getPort());
			getImpl().listen(backlog);
			bound = true;
		}  catch(IOException e) {
			bound = false;
			throw e;
		}
	}

	@Override
	public InetAddress getInetAddress() {
        if(!MockFramework.isEnabled()){
            return super.getInetAddress();
        }
        if (!isBound())
			return null;
		try {
			InetAddress in = getImpl().getInetAddress();			
			return in;
		}  catch (SocketException e) {
			// nothing
			// If we're bound, the impl has been created
			// so we shouldn't get here
		}
		return null;
	}

	@Override
	public int getLocalPort() {
        if(!MockFramework.isEnabled()){
            return super.getLocalPort();
        }
        if (!isBound())
			return -1;
		try {
			return getImpl().getLocalPort();
		} catch (SocketException e) {
			// nothing
			// If we're bound, the impl has been created
			// so we shouldn't get here
		}
		return -1;
	}

    @Override
	public SocketAddress getLocalSocketAddress() {
        if(!MockFramework.isEnabled()){
            return super.getLocalSocketAddress();
        }
        if (!isBound())
			return null;
		return new InetSocketAddress(getInetAddress(), getLocalPort());
	}

    @Override
	public Socket accept() throws IOException {
        if(!MockFramework.isEnabled()){
            return super.accept();
        }
        if (isClosed())
			throw new SocketException("Socket is closed");
		if (!isBound())
			throw new SocketException("Socket is not bound yet");
		MockSocket s = new MockSocket((MockSocketImpl) null);
		
		_implAccept(s); 
		
		return s;
	}

	protected void _implAccept(MockSocket s) throws IOException {
		MockSocketImpl si = null;
		try {
			if (s.impl == null)
				s.setImpl();
			else {
				s.impl.reset();
			}
			si = s.impl;
			s.impl = null;
			//si.address = new InetAddress(); 
			//si.fd = new FileDescriptor(); 
			
			si.setServerSocket(this);// TODO not sure if correct
			getImpl().accept(si);
			
		} catch (IOException e) {
			if (si != null)
				si.reset();
			s.impl = si;
			throw e;
		} 
		s.impl = si;
		s._postAccept();
	}

	
	/* Cannot override because final 
	protected final void implAccept(Socket s) throws IOException {
		SocketImpl si = null;
		try {
			if (s.impl == null)
				s.setImpl();
			else {
				s.impl.reset();
			}
			si = s.impl;
			s.impl = null;
			si.address = new InetAddress();
			si.fd = new FileDescriptor();
			getImpl().accept(si);

			SecurityManager security = System.getSecurityManager();
			if (security != null) {
				security.checkAccept(si.getInetAddress().getHostAddress(),
						si.getPort());
			}
		} catch (IOException e) {
			if (si != null)
				si.reset();
			s.impl = si;
			throw e;
		} catch (SecurityException e) {
			if (si != null)
				si.reset();
			s.impl = si;
			throw e;
		}
		s.impl = si;
		s.postAccept();
	}
	*/
	
	@Override
	public void close() throws IOException {
        if(!MockFramework.isEnabled()){
            super.close();
            return;
        }
        synchronized(closeLock) {
			if (isClosed())
				return;
			if (created)
				impl.close();
			closed = true;
		}
	}

	@Override
	public ServerSocketChannel getChannel() {
		return null; //TODO
	}

	@Override
	public boolean isBound() {
        if(!MockFramework.isEnabled()){
            return super.isBound();
        }
		// Before 1.3 ServerSockets were always bound during creation
		return bound || oldImpl;
	}

	@Override
	public boolean isClosed() {
        if(!MockFramework.isEnabled()){
            return super.isClosed();
        }
		synchronized(closeLock) {
			return closed;
		}
	}

	@Override
	public synchronized void setSoTimeout(int timeout) throws SocketException {
        if(!MockFramework.isEnabled()){
            super.setSoTimeout(timeout);
            return;
        }
        if (isClosed())
			throw new SocketException("Socket is closed");
		getImpl().setOption(SocketOptions.SO_TIMEOUT, timeout);
	}

	@Override
	public synchronized int getSoTimeout() throws IOException {
        if(!MockFramework.isEnabled()){
            return super.getSoTimeout();
        }
        if (isClosed())
			throw new SocketException("Socket is closed");
		Object o = getImpl().getOption(SocketOptions.SO_TIMEOUT);
		/* extra type safety */
		if (o instanceof Integer) {
			return (Integer) o;
		} else {
			return 0;
		}
	}

	@Override
	public void setReuseAddress(boolean on) throws SocketException {
        if(!MockFramework.isEnabled()){
            super.setReuseAddress(on);
            return;
        }
        if (isClosed())
			throw new SocketException("Socket is closed");
		getImpl().setOption(SocketOptions.SO_REUSEADDR, on);
	}

	@Override
	public boolean getReuseAddress() throws SocketException {
        if(!MockFramework.isEnabled()){
            return super.getReuseAddress();
        }
        if (isClosed())
			throw new SocketException("Socket is closed");
		return (Boolean) (getImpl().getOption(SocketOptions.SO_REUSEADDR));
	}

	@Override
	public String toString() {
        if(!MockFramework.isEnabled()){
            return super.toString();
        }
        if (!isBound())
			return "ServerSocket[unbound]";
		
		InetAddress in = impl.getInetAddress();

		return "ServerSocket[addr=" + in + ",localport=" + impl.getLocalPort()  + "]";
	}


	@Override
	public synchronized void setReceiveBufferSize (int size) throws SocketException {
        super.setReceiveBufferSize(size);
	}

	@Override
	public synchronized int getReceiveBufferSize() throws SocketException{
		return super.getReceiveBufferSize();
	}

	@Override
	public void setPerformancePreferences(int connectionTime,
			int latency,
			int bandwidth){
		super.setPerformancePreferences(connectionTime, latency, bandwidth);
	}

}
