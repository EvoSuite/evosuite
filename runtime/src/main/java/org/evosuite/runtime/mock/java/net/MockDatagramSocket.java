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
import org.evosuite.runtime.mock.java.io.MockIOException;
import org.evosuite.runtime.mock.java.lang.MockError;
import org.evosuite.runtime.mock.java.lang.MockIllegalArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.channels.DatagramChannel;

/**
 * TODO need to implement rollback
 *
 * Created by arcuri on 12/7/14.
 */
public class MockDatagramSocket extends DatagramSocket implements OverrideMock{

    private static final Logger logger = LoggerFactory.getLogger(MockDatagramSocket.class);

    private static final int ST_NOT_CONNECTED = 0;
    private static final int ST_CONNECTED = 1;
    private static final int ST_CONNECTED_NO_IMPL = 2;

    private static final Method CREATE_IMPL;
    private static final Field IMPL;

    static{
        Method m = null;
        try {
            m = DatagramSocket.class.getDeclaredMethod("createImpl");
            m.setAccessible(true);
        } catch (NoSuchMethodException e) {
            //should never happen
            logger.error("Failed reflection on DatagramSocket: "+e.getMessage());
        }

        Field f = null;
        try {
            f = DatagramSocket.class.getDeclaredField("impl");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
            //should never happen
            logger.error("Failed reflection on DatagramSocket: "+e.getMessage());
        }

        CREATE_IMPL = m;
        IMPL = f;
    }


    /*
        following fields are the same as in superclass.
        however, they are not overwritten, as in superclass they
        have private/package level access
     */

    private final Object closeLock = new Object();

    private boolean created = false;
    private boolean bound = false;
    private boolean closed = false;
    private int connectState = ST_NOT_CONNECTED;
    private EvoDatagramSocketImpl impl;
    private InetAddress connectedAddress = null;
    private int connectedPort = -1;

    // there is only one protected constructor that does not do binding
    /*
    protected DatagramSocket(DatagramSocketImpl impl) {
        if (impl == null)
            throw new NullPointerException();
        this.impl = impl;
        checkOldImpl();
    }
    */



    protected MockDatagramSocket(DatagramSocketImpl impl) throws SocketException {
    /*
        note: we need to pass to the super(impl) constructor a non-null reference.
        however, such reference is not used in class (we could by using reflection,
        but just easier to make a new copy)
     */
        super(MockFramework.isEnabled() ?
                new EvoDatagramSocketImpl():
                impl);

        if(!MockFramework.isEnabled()){
            return;
        }

        createImpl();
    }

    public MockDatagramSocket() throws SocketException {
        super(new EvoDatagramSocketImpl());

        if(!MockFramework.isEnabled()){
            try {
                IMPL.set(this, null);
                CREATE_IMPL.invoke(this);
            } catch (InvocationTargetException e) {
                throw new SocketException(""+e.getCause().getMessage());
            } catch (IllegalAccessException e) {
                //should never happen
                logger.error("Failed reflection");
            }
            super.bind(new InetSocketAddress(0));
            return;
        }

        // create a datagram socket.
        createImpl();
        try {
            bind(new MockInetSocketAddress(0));
        } catch (SocketException se) {
            throw se;
        } catch(IOException e) {
            throw new SocketException(e.getMessage());
        }
    }


    public MockDatagramSocket(SocketAddress bindaddr) throws SocketException {
        super(new EvoDatagramSocketImpl());

        if(!MockFramework.isEnabled()){
            try {
                IMPL.set(this, null);
                CREATE_IMPL.invoke(this);
            } catch (InvocationTargetException e) {
                throw new SocketException(""+e.getCause().getMessage());
            } catch (IllegalAccessException e) {
                //should never happen
                logger.error("Failed reflection");
            }
            if(bindaddr!=null) {
                super.bind(bindaddr);
            }
            return;
        }
        // create a datagram socket.
        createImpl();
        if (bindaddr != null) {
            bind(bindaddr);
        }
    }

    public MockDatagramSocket(int port) throws SocketException {
        this(port, null);
    }


    public MockDatagramSocket(int port, InetAddress laddr) throws SocketException {
        this(MockFramework.isEnabled() ?
                        new MockInetSocketAddress(laddr, port) :
                        new InetSocketAddress(laddr, port)
        );
    }


    // -----------------------------------


    /*
        it was package level in superclass
     */
    private void createImpl() throws SocketException {
        if (impl == null) {
                //boolean isMulticast = (this instanceof MulticastSocket) ? true : false;
                //impl = DefaultDatagramSocketImplFactory.createDatagramSocketImpl(isMulticast);
                impl = new EvoDatagramSocketImpl();

        }
        // creates a udp socket
        impl.create();
        created = true;
    }

    private synchronized void connectInternal(InetAddress address, int port) throws SocketException {
        if (port < 0 || port > 0xFFFF) {
            throw new MockIllegalArgumentException("connect: " + port);
        }
        if (address == null) {
            throw new MockIllegalArgumentException("connect: null address");
        }
        checkAddress (address, "connect");

        if (isClosed()) {
            return;
        }

        if (!isBound()) {
            bind(new MockInetSocketAddress(0));
        }

            try {
                getImpl().connect(address, port);

                // socket is now connected by the impl
                connectState = ST_CONNECTED;
            } catch (SocketException se) {
                //NOTE: this should never happen in mock environment
                //
                // connection will be emulated by DatagramSocket
                connectState = ST_CONNECTED_NO_IMPL;
            }

        connectedAddress = address;
        connectedPort = port;
    }


    private EvoDatagramSocketImpl getImpl() throws SocketException {
        if (!created) {
            createImpl();
        }
        return impl;
    }

    @Override
    public synchronized void bind(SocketAddress addr) throws SocketException {
        if(!MockFramework.isEnabled()){
            super.bind(addr);
            return;
        }

        if (isClosed())
            throw new SocketException("Socket is closed");
        if (isBound())
            throw new SocketException("already bound");
        if (addr == null)
            addr = new MockInetSocketAddress(0);
        if (!(addr instanceof InetSocketAddress))
            throw new MockIllegalArgumentException("Unsupported address type!");
        InetSocketAddress epoint = (InetSocketAddress) addr;
        if (epoint.isUnresolved())
            throw new SocketException("Unresolved address");
        InetAddress iaddr = epoint.getAddress();
        int port = epoint.getPort();
        checkAddress(iaddr, "bind");

        try {
            getImpl().bind(port, iaddr);
        } catch (SocketException e) {
            getImpl().close();
            throw e;
        }
        bound = true;
    }

    private void checkAddress (InetAddress addr, String op) {
        if (addr == null) {
            return;
        }
        if (!(addr instanceof Inet4Address || addr instanceof Inet6Address)) {
            throw new MockIllegalArgumentException(op + ": invalid address type");
        }
    }

    @Override
    public void connect(InetAddress address, int port) {
        if(!MockFramework.isEnabled()){
            super.connect(address,port);
            return;
        }
        try {
            connectInternal(address, port);
        } catch (SocketException se) {
            throw new MockError("connect failed", se);
        }
    }

    @Override
    public void connect(SocketAddress addr) throws SocketException {
        if(!MockFramework.isEnabled()){
            super.connect(addr);
            return;
        }
        if (addr == null)
            throw new MockIllegalArgumentException("Address can't be null");
        if (!(addr instanceof InetSocketAddress))
            throw new MockIllegalArgumentException("Unsupported address type");
        InetSocketAddress epoint = (InetSocketAddress) addr;
        if (epoint.isUnresolved())
            throw new SocketException("Unresolved address");
        connectInternal(epoint.getAddress(), epoint.getPort());
    }

    @Override
    public void disconnect() {
        if(!MockFramework.isEnabled()){
            super.disconnect();
            return;
        }
        synchronized (this) {
            if (isClosed())
                return;
            if (connectState == ST_CONNECTED) {
                impl.disconnect();
            }
            connectedAddress = null;
            connectedPort = -1;
            connectState = ST_NOT_CONNECTED;
        }
    }

    @Override
    public boolean isBound() {
        if(!MockFramework.isEnabled()){
            return super.isBound();
        }
        return bound;
    }

    @Override
    public boolean isConnected() {
        if(!MockFramework.isEnabled()){
            return super.isConnected();
        }
        return connectState != ST_NOT_CONNECTED;
    }

    @Override
    public InetAddress getInetAddress() {
        if(!MockFramework.isEnabled()){
            return super.getInetAddress();
        }
        return connectedAddress;
    }

    @Override
    public int getPort() {
        if(!MockFramework.isEnabled()){
            return super.getPort();
        }
        return connectedPort;
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        if(!MockFramework.isEnabled()){
            return super.getRemoteSocketAddress();
        }
        if (!isConnected())
            return null;
        return new MockInetSocketAddress(getInetAddress(), getPort());
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        if(!MockFramework.isEnabled()){
            return super.getLocalSocketAddress();
        }
        if (isClosed())
            return null;
        if (!isBound())
            return null;
        return new MockInetSocketAddress(getLocalAddress(), getLocalPort());
    }

    @Override
    public void send(DatagramPacket p) throws IOException  {
        if(!MockFramework.isEnabled()){
            super.send(p);
            return;
        }
        InetAddress packetAddress = null;
        synchronized (p) {
            if (isClosed())
                throw new SocketException("Socket is closed");
            checkAddress (p.getAddress(), "send");
            if (connectState == ST_NOT_CONNECTED) {
                // check the address is ok wiht the security manager on every send.
            } else {
                // we're connected
                packetAddress = p.getAddress();
                if (packetAddress == null) {
                    p.setAddress(connectedAddress);
                    p.setPort(connectedPort);
                } else if ((!packetAddress.equals(connectedAddress)) ||
                        p.getPort() != connectedPort) {
                    throw new MockIllegalArgumentException("connected address and packet address differ");
                }
            }

            // Check whether the socket is bound
            if (!isBound())
                bind(new MockInetSocketAddress(0));
            // call the  method to send
            getImpl().send(p);
        }
    }

    @Override
    public synchronized void receive(DatagramPacket p) throws IOException {
        if(!MockFramework.isEnabled()){
            super.receive(p);
            return;
        }
        synchronized (p) {
            if (!isBound())
                bind(new MockInetSocketAddress(0));
            if (connectState == ST_CONNECTED_NO_IMPL) {
                /* We have to do the filtering the old fashioned way since
                 the native impl doesn't support connect or the connect
                 via the impl failed.

                    However, in mock there is no need to simulate such behavior
                */
            }
            getImpl().receive(p);
        }
    }

    @Override
    public InetAddress getLocalAddress() {
        if(!MockFramework.isEnabled()){
            return super.getLocalAddress();
        }
        if (isClosed())
            return null;
        InetAddress in = null;
        try {
            in = (InetAddress) getImpl().getOption(SocketOptions.SO_BINDADDR);
            if (in.isAnyLocalAddress()) {
                in = MockInetAddress.anyLocalAddress();
            }
        } catch (Exception e) {
            in = MockInetAddress.anyLocalAddress(); // "0.0.0.0"
        }
        return in;
    }



    @Override
    public void close() {
        if(!MockFramework.isEnabled()){
            super.close();
            return;
        }
        synchronized(closeLock) {
            if (isClosed())
                return;
            impl.close();
            closed = true;
        }
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
    public DatagramChannel getChannel() {
        if(!MockFramework.isEnabled()){
            return super.getChannel();
        }
        return null;
    }


    @Override
    public int getLocalPort() {
        if(!MockFramework.isEnabled()){
            return super.getLocalPort();
        }
        if (isClosed())
            return -1;
        try {
            return getImpl().getLocalPort();
        } catch (Exception e) {
            return 0;
        }
    }


    public static synchronized void setDatagramSocketImplFactory(DatagramSocketImplFactory fac)
            throws IOException
    {
        //setting a custom factory is too risky
        throw new MockIOException("Setting of factory is not supported");
    }


    //-----------------------------------

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
    public synchronized int getSoTimeout() throws SocketException {
        if(!MockFramework.isEnabled()){
            return super.getSoTimeout();
        }
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (getImpl() == null)
            return 0;
        Object o = getImpl().getOption(SocketOptions.SO_TIMEOUT);
        /* extra type safety */
        if (o instanceof Integer) {
            return (Integer) o;
        } else {
            return 0;
        }
    }

    @Override
    public synchronized void setSendBufferSize(int size)
            throws SocketException{
        if(!MockFramework.isEnabled()){
            super.setSendBufferSize(size);
            return;
        }
        if (!(size > 0)) {
            throw new IllegalArgumentException("negative send size");
        }
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_SNDBUF, size);
    }

    @Override
    public synchronized int getSendBufferSize() throws SocketException {
        if(!MockFramework.isEnabled()){
            return super.getSendBufferSize();
        }
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
    public synchronized void setReceiveBufferSize(int size)
            throws SocketException{
        if(!MockFramework.isEnabled()){
            super.setReceiveBufferSize(size);
            return;
        }
        if (size <= 0) {
            throw new IllegalArgumentException("invalid receive size");
        }
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_RCVBUF, size);
    }

    @Override
    public synchronized int getReceiveBufferSize()
            throws SocketException{
        if(!MockFramework.isEnabled()){
            return super.getReceiveBufferSize();
        }
        if (isClosed())
            throw new SocketException("Socket is closed");//TODO
        int result = 0;
        Object o = getImpl().getOption(SocketOptions.SO_RCVBUF);
        if (o instanceof Integer) {
            result = (Integer) o;
        }
        return result;
    }

    @Override
    public synchronized void setReuseAddress(boolean on) throws SocketException {
        if(!MockFramework.isEnabled()){
            super.setReuseAddress(on);
            return;
        }
        if (isClosed())
            throw new SocketException("Socket is closed"); //TODO
        // Integer instead of Boolean for compatibility with older DatagramSocketImpl
            getImpl().setOption(SocketOptions.SO_REUSEADDR, on);
    }

    @Override
    public synchronized boolean getReuseAddress() throws SocketException {
        if(!MockFramework.isEnabled()){
            return super.getReuseAddress();
        }
        if (isClosed())
            throw new SocketException("Socket is closed"); //TODO
        Object o = getImpl().getOption(SocketOptions.SO_REUSEADDR);
        return (Boolean) o;
    }

    @Override
    public synchronized void setBroadcast(boolean on) throws SocketException {
        if(!MockFramework.isEnabled()){
            super.setBroadcast(on);
            return;
        }
        if (isClosed())
            throw new SocketException("Socket is closed"); //TODO
        getImpl().setOption(SocketOptions.SO_BROADCAST, on);
    }

    @Override
    public synchronized boolean getBroadcast() throws SocketException {
        if(!MockFramework.isEnabled()){
            return super.getBroadcast();
        }
        if (isClosed())
            throw new SocketException("Socket is closed"); //TODO
        return (Boolean) (getImpl().getOption(SocketOptions.SO_BROADCAST));
    }

    @Override
    public synchronized void setTrafficClass(int tc) throws SocketException {
        if(!MockFramework.isEnabled()){
            super.setTrafficClass(tc);
            return;
        }
        if (tc < 0 || tc > 255)
            throw new MockIllegalArgumentException("tc is not in range 0 -- 255");

        if (isClosed())
            throw new SocketException("Socket is closed"); //TODO
        getImpl().setOption(SocketOptions.IP_TOS, tc);
    }

    @Override
    public synchronized int getTrafficClass() throws SocketException {
        if(!MockFramework.isEnabled()){
            return super.getTrafficClass();
        }
        if (isClosed())
            throw new SocketException("Socket is closed"); //TODO
        return (Integer) (getImpl().getOption(SocketOptions.IP_TOS));
    }

}
