package org.evosuite.runtime.vnet;


import org.evosuite.runtime.mock.java.net.MockInetAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class is used to create socket connections as test data
 * in the test cases.
 * 
 * @author arcuri
 *
 */
public class NetworkHandling {

	/**
	 * Unless otherwise specified, we simulate incoming connections all
	 * from same remote host
	 */
	private static final String DEFAULT_REMOTE_ADDRESS = "127.0.0.42"; 
			
	/**
	 * Open new connection toward {@code sutServer} and buffer the content of {@code data}
	 * to be later sent once {@code sutServer} is opened
	 * 
	 * @param sutServer  the host/port of the SUT 
	 * @param data  if {@code null}, just simulate opening of connection
	 * @return {@code false} if {@code sutServer} is {@code null}
	 */
	public static boolean sendDataOnTcp(EvoSuiteAddress sutServer, byte[] data){
		if(sutServer==null){
			return false;
		}
		
		NativeTcp connection = VirtualNetwork.getInstance().registerIncomingTcpConnection(
				DEFAULT_REMOTE_ADDRESS, VirtualNetwork.getInstance().getNewRemoteEphemeralPort(),  
				sutServer.getHost(), sutServer.getPort());
		
		/*
		 * At this point in time the SUT has not opened a connection yet (if it did,
		 * it would had thrown an IOException).
		 * But we can already put the message on the buffer
		 */
		
		if(data != null){
			for(byte b : data){
				connection.writeToSUT(b);
			}
		}
		//TODO close connection? or should rather be in another helper function? 
		
		return true;
	}

    /**
     * Send UDP to SUT from an ephemeral port on a default remote host
     * @param sutAddress
     * @param data
     * @return
     */
    public static boolean sendUdpPacket(EvoSuiteAddress sutAddress, byte[] data){
        return sendUdpPacket(sutAddress, new EvoSuiteAddress(DEFAULT_REMOTE_ADDRESS, VirtualNetwork.getInstance().getNewRemoteEphemeralPort()),data);
    }

    /**
     * Create a send a new UDP packet to the SUT. The packets are buffered till the SUT opens a socket
     * to read them.
     *
     * @param sutAddress
     * @param data
     * @return
     */
    public static boolean sendUdpPacket(EvoSuiteAddress sutAddress, EvoSuiteAddress remoteAddress, byte[] data){
        if(sutAddress == null){
            return false;
        }

        //data can be null
        if(data == null){
            data = new byte[0];
        }

        InetAddress address = null;

        try {
            address = MockInetAddress.getByName(remoteAddress.getHost());
        } catch (UnknownHostException e) {
            return false;
        }

        VirtualNetwork.getInstance().sendPacketToSUT(data,
                address,remoteAddress.getPort(),
                sutAddress.getHost(), sutAddress.getPort());

        return true;
    }

	/**
	 * Convert {@code message} to a byte array and send it with
	 * {@link NetworkHandling#sendDataOnTcp}
	 * @param sutServer
	 * @param message
	 * @return
	 */
	public static boolean sendMessageOnTcp(EvoSuiteAddress sutServer, String message){
		return sendDataOnTcp(sutServer,message.getBytes());
	}
}
