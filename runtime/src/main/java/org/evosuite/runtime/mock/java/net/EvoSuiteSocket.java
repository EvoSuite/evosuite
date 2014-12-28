package org.evosuite.runtime.mock.java.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketOptions;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.evosuite.runtime.vnet.EndPointInfo;
import org.evosuite.runtime.vnet.NativeTcp;
import org.evosuite.runtime.vnet.VirtualNetwork;
import org.evosuite.runtime.vnet.VirtualNetwork.ConnectionType;

import sun.net.ResourceManager;

/*
 * An actual implementation is 
 * 
 * SocksSocketImpl -> PlainSocketImpl -> AbstractPlainSocketImpl -> SocketImpl
 */

public class EvoSuiteSocket extends MockSocketImpl{

	private final Map<Integer,Object> options;

	private NativeTcp openedConnection;

	private boolean isClosed;

	/**
	 *  whether this Socket is a stream (TCP) socket or not (UDP)
	 */
	protected boolean stream;


	public EvoSuiteSocket(){
		options = new ConcurrentHashMap<>();
		initOptions();
		isClosed = false;
	}

	public EvoSuiteSocket(Proxy proxy){
		this();
		SocketAddress a = proxy.address();
		if (a instanceof InetSocketAddress) {
			InetSocketAddress ad = (InetSocketAddress) a;
			// Use getHostString() to avoid reverse lookups

			//server = ad.getHostString();  /FIXME: check how it is used in SocksSocketImpl
			//serverPort = ad.getPort();
		}
	}

	private void initOptions(){
		// see AbstractPlainSocketImpl
		options.put(SocketOptions.SO_TIMEOUT, 0);
		options.put(SocketOptions.SO_RCVBUF, 131072);
		options.put(SocketOptions.SO_SNDBUF, 131072);
		options.put(SocketOptions.SO_LINGER, -1);
		options.put(SocketOptions.SO_KEEPALIVE, false);
		options.put(SocketOptions.SO_OOBINLINE, false);
		options.put(SocketOptions.SO_REUSEADDR, false);
		options.put(SocketOptions.TCP_NODELAY, false);
	}

	@Override
	public void setOption(int optID, Object value) throws SocketException {
		//TODO validation?
		// see AbstractPlainSocketImpl
		options.put(optID, value);
	}

	/**
	 * Return the current value of SO_TIMEOUT
	 */
	public int getTimeout() {
		return (int) options.get(SO_TIMEOUT); 
	}

	@Override
	public Object getOption(int optID) throws SocketException {
		return options.get(optID);
	}

	/**
	 * Creates a socket with a boolean that specifies whether this
	 * is a stream socket (true) or an unconnected UDP socket (false).
	 */
	@Override
	protected synchronized void create(boolean stream) throws IOException {
		this.stream = stream;
		if (!stream) {
				socketCreate(false);
		} else {
			socketCreate(true);
		}
		if (socket != null)
			socket.setCreated();
		if (serverSocket != null)
			serverSocket.setCreated();

	}

	protected void socketCreate(boolean isStream){
		//TODO
	}
	
	@Override
	protected void connect(String host, int port) throws UnknownHostException, IOException {
		//from AbstractPlainSocketImpl
		boolean connected = false;
		try {
			InetAddress address = InetAddress.getByName(host);
			this.port = port;
			this.address = address;

			connectToAddress(address, port, getTimeout()); 
			connected = true;
		} finally {
			if (!connected) {
				try {
					close();
				} catch (IOException ioe) {
					/* Do nothing. If connect threw an exception then
                       it will be passed up the call stack */
				}
			}
		}

	}

	@Override
	protected void connect(InetAddress address, int port) throws IOException {
		//from AbstractPlainSocketImpl
		this.port = port;
		this.address = address;

		try {
			connectToAddress(address, port, getTimeout()); 
			return;
		} catch (IOException e) {
			// everything failed
			close();
			throw e;
		}
	}

	@Override
	protected void connect(SocketAddress address, int timeout)
			throws IOException {
		//from AbstractPlainSocketImpl, and TODO overridden in SocksSocketImpl
		boolean connected = false;
		try {
			if (address == null || !(address instanceof InetSocketAddress))
				throw new IllegalArgumentException("unsupported address type");
			InetSocketAddress addr = (InetSocketAddress) address;
			if (addr.isUnresolved())
				throw new UnknownHostException(addr.getHostName());
			this.port = addr.getPort();
			this.address = addr.getAddress();

			connectToAddress(this.address, port, timeout);
			connected = true;
		} finally {
			if (!connected) {
				try {
					close();
				} catch (IOException ioe) {
					/* Do nothing. If connect threw an exception then
                       it will be passed up the call stack */
				}
			}
		}

	}

	@Override
	protected void bind(InetAddress host, int port) throws IOException {		
		//TODO: need to check special cases like multicast and 0.0.0.0
		boolean opened = VirtualNetwork.getInstance().openTcpServer(host.getHostAddress(), port);
		if(!opened){
			throw new IOException("Failed to opened TCP port");
		}
		super.localport = port;
		setOption(SocketOptions.SO_BINDADDR, host);		
	}

	@Override
	protected void listen(int backlog) throws IOException {
		// TODO 		
	}

	@Override
	protected void accept(SocketImpl s) throws IOException {

		if(! (s instanceof EvoSuiteSocket)){
			throw new IOException("Can only handle mocked sockets");
		}

		EvoSuiteSocket mock = (EvoSuiteSocket) s;

		/*
		 * If the test case has set up an incoming connection, then
		 * simulate an immediate connection.
		 * If not, there is no point in blocking the SUT for
		 * a connection that will never arrive: just throw an exception
		 */

		InetAddress localHost = (InetAddress) options.get(SocketOptions.SO_BINDADDR); 
		NativeTcp tcp = VirtualNetwork.getInstance().pullTcpConnection(localHost.getHostAddress(),localport);

		if(tcp == null){
			throw new IOException("Simulated exception on waiting server");
		} else {
			mock.openedConnection = tcp;
			mock.setOption(SocketOptions.SO_BINDADDR, localHost);		
			mock.setLocalPort(localport);
			mock.setRemoteAddress(InetAddress.getByName(tcp.getRemoteEndPoint().getHost()));
			mock.setRemotePort(tcp.getRemoteEndPoint().getPort());
		}
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		checkIfClosed();
		return new SocketIn(openedConnection,true);
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {		
		checkIfClosed();
		return new SocketOut(openedConnection,true);
	}

	@Override
	protected int available() throws IOException {
		checkIfClosed();
		if(openedConnection==null){
			return 0;
		}
		return openedConnection.getAmountOfDataInLocalBuffer();
	}

	@Override
	protected void close() throws IOException {
		isClosed = true;
	}

	@Override
	protected void sendUrgentData(int data) throws IOException {
		// TODO this is off by default, but can be activated with the option SO_OOBINLINE
		checkIfClosed();
	}

	//---------------------------------------------------

	protected void connectToAddress(InetAddress address, int port, int timeout) throws IOException {
		if (address.isAnyLocalAddress()) {
			doConnect(InetAddress.getLocalHost(), port, timeout);
		} else {
			doConnect(address, port, timeout);
		}
	}

	protected synchronized void doConnect(InetAddress address, int port, int timeout) throws IOException {
		EndPointInfo remoteTarget = new EndPointInfo(address.getHostAddress(), port, ConnectionType.TCP);
		
		InetSocketAddress isa = new InetSocketAddress(0);// FIXME
		EndPointInfo localOrigin = new EndPointInfo(isa.getAddress().getHostAddress(), isa.getPort(), ConnectionType.TCP);
		this.openedConnection = VirtualNetwork.getInstance().connectToRemoteAddress(localOrigin, remoteTarget);
		
		this.setOption(SocketOptions.SO_BINDADDR, isa.getAddress());
		this.setLocalPort(isa.getPort());
		this.setRemoteAddress(address);
		this.setRemotePort(port);
	}

	protected void checkIfClosed() throws IOException{
		if(isClosed){
			throw new IOException("Connection is closed");
		}
	}
}
