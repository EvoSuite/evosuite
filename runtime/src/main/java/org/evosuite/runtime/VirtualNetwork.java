package org.evosuite.runtime;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Singleton class used to simulate a virtual network.
 * This is useful to test classes that use UDP/TCP connections
 * 
 * @author arcuri
 *
 */
public class VirtualNetwork {

	private enum ConnectionType {UDP,TCP};
	
	/**
	 * Singleton instance
	 */
	private static final VirtualNetwork instance = new VirtualNetwork();
	
	/**
	 * Set of listening ports locally opened by the SUTs.
	 * Eg, when the SUTs work as a server, we keep track
	 * of which local ports/interfaces they listen to.
	 */
	private final Set<EndPointInfo> localListeningPorts;
	
	/**
	 * Buffer of incoming connections.
	 * 
	 * <p>
	 * Key -> local address/port
	 * <p>
	 * Value -> queue of foreign addresses/ports waiting to connect to the given local address (key)
	 */
	private final Map<EndPointInfo,Queue<EndPointInfo>> incomingConnections;
	
	private VirtualNetwork(){
        localListeningPorts = new CopyOnWriteArraySet<>();
        incomingConnections = new ConcurrentHashMap<>();
	}
	
	public static VirtualNetwork getInstance(){
		return instance;
	}
	
	//------------------------------------------

	public void reset(){
		localListeningPorts.clear();
		incomingConnections.clear();
	}
	
	/**
	 * Simulate an incoming connection. The connection is put on a buffer till
	 * the SUT open a listening port
	 * 
	 * @param originAddr
	 * @param originPort
	 * @param destAddr
	 * @param destPort
	 */
	public synchronized void registerIncomingTcpConnection(
			String originAddr, int originPort,
			String destAddr, int destPort){
		
		EndPointInfo origin = new EndPointInfo(originAddr,originPort,ConnectionType.TCP);
		EndPointInfo dest = new EndPointInfo(destAddr,destPort,ConnectionType.TCP);
		
		Queue<EndPointInfo> queue = incomingConnections.get(dest);
		if(queue == null){
			queue = new ConcurrentLinkedQueue<>();
			incomingConnections.put(dest, queue);
		}
		
		queue.add(origin);
	}
	
	//TODO proper return type
	public synchronized Object pullTcpConnection(String localAddress, int localPort){
		EndPointInfo local = new EndPointInfo(localAddress,localPort,ConnectionType.TCP);
		Queue<EndPointInfo> queue = incomingConnections.get(local);
		if(queue == null || queue.isEmpty()){
			return null;
		}
		
		return ""; //FIXME
	}
	
	
	/**
	 * 
	 * @param addr
	 * @return {@code false} if it was not possible to open the listening port
	 */
	public synchronized boolean  openTcpServer(String addr, int port){
		EndPointInfo info = new EndPointInfo(addr,port,ConnectionType.TCP);
		
		if(localListeningPorts.contains(info)){
			//there is already an existing opened port
			return false;
		}
		
		if(! isValidLocalServer(info)){
			return false;
		}
		
		localListeningPorts.add(info);
		
		return true;
	}
	
	
	//------------------------------------------

	private boolean isValidLocalServer(EndPointInfo info){
		return true; //TODO
	}
	
	//------------------------------------------
	
	/**
	 * Convenience class used to store connection info 
	 */
	private static class EndPointInfo{
		public final String host;
		public final int port;
		public final ConnectionType type;
		
		public EndPointInfo(String host, int port, ConnectionType type) {
			super();
			this.host = host;
			this.port = port;
			this.type = type;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((host == null) ? 0 : host.hashCode());
			result = prime * result + port;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EndPointInfo other = (EndPointInfo) obj;
			if (host == null) {
				if (other.host != null)
					return false;
			} else if (!host.equals(other.host))
				return false;
			if (port != other.port)
				return false;
			if (type != other.type)
				return false;
			return true;
		}
	}
}
