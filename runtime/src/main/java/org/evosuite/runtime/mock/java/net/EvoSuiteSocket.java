package org.evosuite.runtime.mock.java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketOptions;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.evosuite.runtime.vnet.NativeTcp;
import org.evosuite.runtime.vnet.VirtualNetwork;

/*
 * An actual implementation is 
 * 
 * SocksSocketImpl -> PlainSocketImpl -> AbstractPlainSocketImpl -> SocketImpl
 */

public class EvoSuiteSocket extends MockSocketImpl{

	private final Map<Integer,Object> options;
	
	private NativeTcp openedConnection;
	
	private boolean isClosed;
	
	public EvoSuiteSocket(){
		options = new ConcurrentHashMap<>();
		initOptions();
		isClosed = false;
	}
	
	private void initOptions(){
		//options.put(SocketOptions, );
		//TODO
	}
	
	@Override
	public void setOption(int optID, Object value) throws SocketException {
		options.put(optID, value);
	}

	@Override
	public Object getOption(int optID) throws SocketException {
		return options.get(optID);
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
			throw new IOException("Can only hanlded mocked sockets");
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

	private void checkIfClosed() throws IOException{
		if(isClosed){
			throw new IOException("Connection is closed");
		}
	}
}
