package org.evosuite.runtime.mock.java.net;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
//import java.net.InetSocketAddress.InetSocketAddressHolder;

public class MockInetSocketAddress extends InetSocketAddress{

	/*
	// Private implementation class pointed to by all public methods.
	private static class InetSocketAddressHolder {
		// The hostname of the Socket Address
		private String hostname;
		// The IP address of the Socket Address
		private InetAddress addr;
		// The port number of the Socket Address
		private int port;

		private InetSocketAddressHolder(String hostname, InetAddress addr, int port) {
			this.hostname = hostname;
			this.addr = addr;
			this.port = port;
		}

		private int getPort() {
			return port;
		}

		private InetAddress getAddress() {
			return addr;
		}

		private String getHostName() {
			if (hostname != null)
				return hostname;
			if (addr != null)
				return addr.getHostName();
			return null;
		}

		private String getHostString() {
			if (hostname != null)
				return hostname;
			if (addr != null) {
				if (addr.holder().getHostName() != null)
					return addr.holder().getHostName();
				else
					return addr.getHostAddress();
			}
			return null;
		}

		private boolean isUnresolved() {
			return addr == null;
		}

		@Override
		public String toString() {
			if (isUnresolved()) {
				return hostname + ":" + port;
			} else {
				return addr.toString() + ":" + port;
			}
		}

		@Override
		public final boolean equals(Object obj) {
			if (obj == null || !(obj instanceof InetSocketAddressHolder))
				return false;
			InetSocketAddressHolder that = (InetSocketAddressHolder)obj;
			boolean sameIP;
			if (addr != null)
				sameIP = addr.equals(that.addr);
			else if (hostname != null)
				sameIP = (that.addr == null) &&
				hostname.equalsIgnoreCase(that.hostname);
			else
				sameIP = (that.addr == null) && (that.hostname == null);
			return sameIP && (port == that.port);
		}

		@Override
		public final int hashCode() {
			if (addr != null)
				return addr.hashCode() + port;
			if (hostname != null)
				return hostname.toLowerCase().hashCode() + port;
			return port;
		}
	}
	
	private final transient InetSocketAddressHolder holder;
	*/

	private static final long serialVersionUID = 5076001401234631237L;

	private static int checkPort(int port) {
		if (port < 0 || port > 0xFFFF)
			throw new IllegalArgumentException("port out of range:" + port);
		return port;
	}

	private static String checkHost(String hostname) {
		if (hostname == null)
			throw new IllegalArgumentException("hostname can't be null");
		return hostname;
	}


	public MockInetSocketAddress(int port) {
		this((InetAddress)null, port); //FIXME
		//this(InetAddress.anyLocalAddress(), port);
	}

	public MockInetSocketAddress(InetAddress addr, int port) {
		super(addr == null ? (InetAddress)null : addr,port); //FIXME InetAddress.anyLocalAddress()
		/*
		holder = new InetSocketAddressHolder(
				null,
				addr == null ? InetAddress.anyLocalAddress() : addr,
						checkPort(port));
		*/				
	}

	public MockInetSocketAddress(String hostname, int port) {
		this((InetAddress)null, port); //FIXME most likely ll need reflection to set host
		/*
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

