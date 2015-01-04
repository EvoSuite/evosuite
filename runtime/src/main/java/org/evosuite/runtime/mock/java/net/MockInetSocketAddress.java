package org.evosuite.runtime.mock.java.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.evosuite.runtime.mock.OverrideMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class MockInetSocketAddress extends InetSocketAddress implements OverrideMock{

	private static final Logger logger = LoggerFactory.getLogger(MockInetSocketAddress.class);

	private static final long serialVersionUID = 5076001401234631237L;


	private static String checkHost(String hostname) {
		if (hostname == null)
			throw new IllegalArgumentException("hostname can't be null");
		return hostname;
	}


	public MockInetSocketAddress(int port) {
		this(MockInetAddress.anyLocalAddress(), port);
	}

	public MockInetSocketAddress(InetAddress addr, int port) {
		super(addr == null ? MockInetAddress.anyLocalAddress() : addr, port); //TODO need to handle port 0?
	}

	private static InetAddress getResolvedAddressed(String hostname){
		checkHost(hostname);
		try {
			return MockInetAddress.getByName(hostname);
		} catch(UnknownHostException e) {
			logger.warn("EvoSuite limitation: unsupported case of hostname resolution for "+hostname);
			return null;
		}
	}
	
	public MockInetSocketAddress(String hostname, int port) {
		this(getResolvedAddressed(hostname), port); 
		/*
		 * TODO we are not mocking this constructor properly.
		 * We should use reflection to modify the state of 
		 * parent's holder variable.
		 * But it would be bit complicated, and not so important,
		 * as case we do not handle should be anyway rare, ie
		 * fail to resolve hostname (see DNS)
		 * 
		 * 
		checkHost(hostname);
		InetAddress addr = null;
		String host = null;
		try {
			addr = InetAddress.getByName(hostname);
		} catch(UnknownHostException e) {
			host = hostname;
		}
		holder = new InetSocketAddressHolder(host, addr, checkPort(port));
		*/
	}

	// private constructor for creating unresolved instances
	/*
	private MockInetSocketAddress(int port, String hostname) {
		holder = new InetSocketAddressHolder(hostname, null, port);
	}
	 */
	
	//--------------------------------------------------------------
	
	public static InetSocketAddress createUnresolved(String host, int port) {
		//no need to create a mock instance? likely not
		return InetSocketAddress.createUnresolved(host, port);
		//return new MockInetSocketAddress(checkPort(port), checkHost(host));
	}

	
	/*  
	 * Those are all final
	 * 
	public final int getPort() {
		return holder.getPort();
	}

	public final InetAddress getAddress() {
		return holder.getAddress();
	}

	public final String getHostName() {
		return holder.getHostName();
	}

	public final String getHostString() {
		return holder.getHostString();
	}

	public final boolean isUnresolved() {
		return holder.isUnresolved();
	}

	@Override
	public String toString() {
		return holder.toString();
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj == null || !(obj instanceof InetSocketAddress))
			return false;
		return holder.equals(((InetSocketAddress) obj).holder);
	}

	@Override
	public final int hashCode() {
		return holder.hashCode();
	}
	*/
}

