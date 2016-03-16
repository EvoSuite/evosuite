/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.runtime.vnet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import org.evosuite.runtime.mock.java.net.MockInetAddress;
import org.evosuite.runtime.mock.java.net.MockURL;


/**
 * Singleton class used to simulate a virtual network.
 * This is useful to test classes that use UDP/TCP connections
 * 
 * @author arcuri
 *
 */
public class VirtualNetwork {

    /**
     * Specify a network protocol
     */
	public enum ConnectionType {UDP,TCP};


	/**
	 * Singleton instance
	 */
	private static final VirtualNetwork instance = new VirtualNetwork();

	/**
	 * When we simulate a remote incoming connection, we still need a remote port.
	 * Note: in theory we could have the same port if we simulate several different
	 * remote hosts. But, for unit testing purposes, it is likely an unnecessary
	 * overhead/complication
	 */
	private static final int START_OF_REMOTE_EPHEMERAL_PORTS = 40000; 

	/**
	 * Set of listening ports locally opened by the SUTs.
	 * Eg, when the SUTs work as a server, we keep track
	 * of which local ports/interfaces they listen to.
	 */
	private final Set<EndPointInfo> localListeningPorts;

	/**
	 * Set of addresses/ports the SUT tried to contact.
	 */
	private final Set<EndPointInfo> remoteContactedPorts;

    /**
     * key -> address of a remote server
     * <p>
     * value -> a queue of instances of remote servers for the given address.
     * Note: we need a queue as a server listening on a port could handle several
     * connections (eg with thread-pool), and each one needs its own object instance.
     */
	private final Map<EndPointInfo, Queue<RemoteTcpServer>> remoteCurrentServers;

	/**
	 * Buffer of incoming connections.
	 * 
	 * <p>
	 * Key -> local address/port
	 * <p>
	 * Value -> queue of foreign addresses/ports waiting to connect to the given local address (key)
	 */
	private final Map<EndPointInfo,Queue<NativeTcp>> incomingConnections;

	/**
	 * Keep track of all TCP connections opened during the tests.
	 * This is for example useful to check what data the SUT sent.
	 */
	private final Set<NativeTcp> openedTcpConnections;

	/**
	 * Current remote port number that can be opened
	 */
	private final AtomicInteger remotePortIndex;

    /**
     *  Keeping track of all sent UDP messages is likely not a viable option.
     *  So, for each remote host, we can keep track of how many UDP were sent to it.
     *  This can be used to create concise assertions.
     *  Note: for unit testing purposes, it does not really matter if the remote host is listening,
     *  as UDP is stateless.
     */
    private final Map<EndPointInfo , AtomicInteger>  sentUdpPackets;

    /**
     * key -> local address/port for SUT
     * <p>
     * value -> queue of incoming UDP packets
     */
    private final Map<EndPointInfo , Queue<DatagramPacket>> udpPacketsToSUT;

	/**
	 * Define what interfaces are available:
	 * eg, a loopback one and a wifi
	 */
	private final List<NetworkInterfaceState> networkInterfaces;


    /**
     * Key -> resolved URL (ie based on DNS) of the remote file
     * Value -> the remote file we ll allow the tests to read from
     *
     * <p>
     *    This data structure represents remote files that are on a different host, and that could be accessed
     * for example by http/s using an URL object.
     *
     * <p>
     * For simplicity, we focus on text files (eg webpages), as those are the most common example.
     *
     * <p>
     * Note: ideally we should have a full mock of remote servers. For example, accessing a http URL
     * should be equivalent to open a TCP socket and send a GET command manually. However, as we
     * do unit testing, this level of realism seems unnecessary (and anyway far too complicated to
     * implement at the moment).
     */
    private final Map<String , RemoteFile> remoteFiles;

    /**
     * Keep track of what remote URL the SUT tried to access/read from
     */
    private final Set<String> remoteAccessedFiles;

	private DNS dns;

	/**
	 * private, singleton constructor
	 */
	private VirtualNetwork(){
		localListeningPorts = new CopyOnWriteArraySet<>();
		incomingConnections = new ConcurrentHashMap<>();
		openedTcpConnections = new CopyOnWriteArraySet<>();
		remotePortIndex = new AtomicInteger(START_OF_REMOTE_EPHEMERAL_PORTS);
		remoteContactedPorts = new CopyOnWriteArraySet<>();
		remoteCurrentServers = new ConcurrentHashMap<>();
		networkInterfaces = new CopyOnWriteArrayList<>();
        remoteFiles = new ConcurrentHashMap<>();
        remoteAccessedFiles = new CopyOnWriteArraySet<>();
        sentUdpPackets = new ConcurrentHashMap<>();
        udpPacketsToSUT = new ConcurrentHashMap<>();

		dns = new DNS();
	}

	public static VirtualNetwork getInstance(){
		return instance;
	}

	//------------------------------------------


    public void init(){
        reset(); //just to be sure

        initNetworkInterfaces();
        MockURL.initStaticState();
    }

	public void reset(){
		dns = new DNS();

		incomingConnections.clear();
		remotePortIndex.set(START_OF_REMOTE_EPHEMERAL_PORTS);
		remoteCurrentServers.clear();
		networkInterfaces.clear();
        remoteFiles.clear();
        udpPacketsToSUT.clear();
        sentUdpPackets.clear();
        localListeningPorts.clear();
		openedTcpConnections.clear();
		remoteContactedPorts.clear();
        remoteAccessedFiles.clear();
	}

    // -------  observers ----------------------

    public Set<String> getViewOfRemoteAccessedFiles(){
        return Collections.unmodifiableSet(remoteAccessedFiles);
    }

    public Set<NativeTcp> getViewOfOpenedTcpConnections(){
        return  Collections.unmodifiableSet(openedTcpConnections);
    }

    public Set<EndPointInfo> getViewOfLocalListeningPorts(){
        return Collections.unmodifiableSet(localListeningPorts);
    }

    public Set<EndPointInfo> getViewOfRemoteContactedPorts() {return Collections.unmodifiableSet(remoteContactedPorts);}

    public Map<EndPointInfo, Integer> getCopyOfSentUDP(){
        //as AtomicInteger is modifiable, we cannot return a view. we need a copy
        Map<EndPointInfo, Integer> map = new LinkedHashMap<>();
        for(EndPointInfo info : sentUdpPackets.keySet()){
            map.put(info,sentUdpPackets.get(info).get());
        }
        return map;
    }

    /**
     * Get a copy of all available interfaces
     * @return
     */
    public List<NetworkInterfaceState> getAllNetworkInterfaceStates(){
        return new ArrayList<>(networkInterfaces);
    }



    //------------------------------------------


    /**
     * Create a new remote file that can be accessed by the given URL
     * @param url
     * @param content
     * @return {@code false} if URL is malformed, if the protocol is not a remote one (eg "file"), or
     * if the file was already created
     */
    public boolean addRemoteTextFile(String url, String content){

        URL mockURL;
        try {
            /*
                be sure to use the mocked URL, in case we have DNS resolution
             */
            mockURL = MockURL.URL(url);
        } catch (MalformedURLException e) {
            return  false;
        }
        if(mockURL.getProtocol().toLowerCase().equals("file")){
            return false; // those are handled in VFS
        }

        String key = url.toString();
        if(remoteFiles.containsKey(key)){
            return false;
        }

        RemoteFile rf = new RemoteFile(key,content);
        remoteFiles.put(key,rf);

        return true;
    }

    /**
     * Represent the fact that a UDP was sent to a remote host
     *
     * @param packet
     */
    public void sentPacketBySUT(DatagramPacket packet){
        InetAddress addr = packet.getAddress();
        int port = packet.getPort();
        EndPointInfo info = new EndPointInfo(addr.getHostAddress(),port,ConnectionType.UDP);

        remoteContactedPorts.add(info);
        synchronized(sentUdpPackets){
            AtomicInteger counter = sentUdpPackets.get(info);
            if(counter == null){
                counter = new AtomicInteger(0);
                sentUdpPackets.put(info,counter);
            }
            counter.incrementAndGet();
        }
    }

    /**
     *
     * @param sutAddress
     * @param sutPort
     * @return {@code null} if there is no buffered incoming packet for the given SUT address
     */
    public DatagramPacket pullUdpPacket(String sutAddress, int sutPort){
        EndPointInfo sut = new EndPointInfo(sutAddress,sutPort,ConnectionType.UDP);
        Queue<DatagramPacket> queue = udpPacketsToSUT.get(sut);
        if(queue == null || queue.isEmpty()){
            return null;
        }

        DatagramPacket p = queue.poll();
        return p;
    }

    public void sendPacketToSUT(byte[] data, InetAddress remoteAddress, int remotePort,  String sutAddress, int sutPort){
        DatagramPacket packet = new DatagramPacket(data.clone(),data.length,remoteAddress, remotePort);
        EndPointInfo sut = new EndPointInfo(sutAddress,sutPort,ConnectionType.UDP);

        synchronized(udpPacketsToSUT){
            Queue<DatagramPacket> queue = udpPacketsToSUT.get(sut);
            if(queue == null){
                queue = new ConcurrentLinkedQueue<>();
                udpPacketsToSUT.put(sut,queue);
            }
            queue.add(packet);
        }
    }

    /**
     * If it is present on the VNET, return a remote file handler to read such file pointed by the URL.
     *
     * @param url
     * @return {@code null} if there is no such file
     */
    public RemoteFile getFile(URL url){
        String s = url.toString();
        if(!remoteAccessedFiles.contains(s)){
            remoteAccessedFiles.add(s);
        }
        return remoteFiles.get(s);
    }

	/**
	 * Create new port to open on remote host
	 * 
	 * @return a integer representing a port number on remote host
	 */
	public int getNewRemoteEphemeralPort(){
		return remotePortIndex.getAndIncrement();
	}

    /**
     * Create new port on local host
     *
     * @return a integer representing a port number on local host
     */
    public int getNewLocalEphemeralPort(){
        return remotePortIndex.getAndIncrement(); //Note: could use a new variable, but doesn't really matter
    }

	/**
	 * 
	 * @param name
	 * @return {@code null} if the interface does not exist
	 */
	public NetworkInterfaceState getNetworkInterfaceState(String name){
		for(NetworkInterfaceState ni : networkInterfaces){
			if(ni.getNetworkInterface().getName().equals(name)){
				return ni;
			}
		}
		return null; 
	}

	/**
	 * Use mocked DNS to resolve host
	 * @param host
	 * @return 
	 */
	public String dnsResolve(String host){
		return dns.resolve(host);
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
	public synchronized NativeTcp registerIncomingTcpConnection(
			String originAddr, int originPort,
			String destAddr, int destPort){

		EndPointInfo origin = new EndPointInfo(originAddr,originPort,ConnectionType.TCP);
		EndPointInfo dest = new EndPointInfo(destAddr,destPort,ConnectionType.TCP);

		Queue<NativeTcp> queue = incomingConnections.get(dest);
		if(queue == null){
			queue = new ConcurrentLinkedQueue<>();
			incomingConnections.put(dest, queue);
		}

		NativeTcp connection = new NativeTcp(dest,origin);		
		queue.add(connection);
		return connection;
	}

	/**
	 * Return a TCP connection for the given local address if there is any inbound remote connection to it
	 * 
	 * @param localAddress
	 * @param localPort
	 * @return  {@code null} if the test case has not set up it an incoming TCP connection
	 */
	public synchronized NativeTcp pullTcpConnection(String localAddress, int localPort){

        EndPointInfo local = new EndPointInfo(localAddress,localPort,ConnectionType.TCP);
		Queue<NativeTcp> queue = incomingConnections.get(local);
		if(queue == null || queue.isEmpty()){
			return null;
		}

		NativeTcp connection = queue.poll();
		openedTcpConnections.add(connection);

		return connection; 
	}


	/**
	 * 
	 * @param addr
	 * @return {@code false} if it was not possible to open the listening port
	 */
    public synchronized boolean  openTcpServer(String addr, int port) throws IllegalArgumentException{
        return openServer(addr,port,ConnectionType.TCP);
    }

    /**
     *
     * @param addr
     * @return {@code false} if it was not possible to open the listening port
     */
    public synchronized boolean  openUdpServer(String addr, int port) throws IllegalArgumentException{
        return openServer(addr,port,ConnectionType.UDP);
    }


    private boolean openServer(String addr, int port, ConnectionType type) throws IllegalArgumentException{

        if(port == 0){
            throw new IllegalArgumentException("Cannot try to bind to wildcard port 0");
        }

        EndPointInfo info = new EndPointInfo(addr,port,type);

        if(localListeningPorts.contains(info)){
            /*
                there is already an existing opened port.
                Note: it is possible to have a UDP and TCP on same port
             */
            return false;
        }

        if(! isValidLocalServer(info)){
            return false;
        }

        localListeningPorts.add(info);

        return true;
    }



	/**
	 *  Register a remote server that can reply to SUT's connection requests
	 */
	public synchronized void addRemoteTcpServer(RemoteTcpServer server){

		Queue<RemoteTcpServer> queue = remoteCurrentServers.get(server.getAddress());
		if(queue==null){
			queue = new ConcurrentLinkedQueue<>();
			remoteCurrentServers.put(server.getAddress(), queue);
		}

		queue.add(server);
	}


    /**
     * Create a mocked TCP connection from the SUT to a remote host
     *
     * @param localOrigin
     * @param remoteTarget
     * @return
     * @throws IllegalArgumentException
     * @throws IOException
     */
	public synchronized NativeTcp connectToRemoteAddress(EndPointInfo localOrigin, EndPointInfo remoteTarget)
			throws IllegalArgumentException, IOException{

		if(localOrigin==null || remoteTarget==null){
			throw new IllegalArgumentException("Null input");
		}

        if(!localOrigin.getType().equals(ConnectionType.TCP) || !remoteTarget.getType().equals(ConnectionType.TCP)){
            throw new IllegalArgumentException("Non-TCP connections");
        }

		if(!isValidLocalServer(localOrigin)){
			throw new IllegalArgumentException("Invalid local address: "+localOrigin);
		}

		remoteContactedPorts.add(remoteTarget);

		Queue<RemoteTcpServer> queue = remoteCurrentServers.get(remoteTarget);
		if(queue==null || queue.isEmpty()){
			throw new IOException("Remote address/port is not opened: "+remoteTarget);
		}

		RemoteTcpServer server = queue.poll();
		NativeTcp connection = server.connect(localOrigin);
		return connection;
	}

	//------------------------------------------


	private boolean isValidLocalServer(EndPointInfo info){
		return true; //TODO
	}

	private void initNetworkInterfaces() {

		try{
			NetworkInterfaceState loopback = new NetworkInterfaceState(
					"Evo_lo0", 1, null, 16384, true, MockInetAddress.getByName("127.0.0.1"));
			networkInterfaces.add(loopback);

			NetworkInterfaceState wifi = new NetworkInterfaceState(
					"Evo_en0", 5, new byte[]{0, 42, 0, 42, 0, 42}, 
					1500, false, MockInetAddress.getByName("192.168.1.42"));
			networkInterfaces.add(wifi);
		} catch(Exception e){
			//this should never happen
			throw new RuntimeException("EvoSuite error: "+e.getMessage());
		}
	}

	//------------------------------------------
}
