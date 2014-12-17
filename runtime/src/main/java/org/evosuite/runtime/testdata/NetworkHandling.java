package org.evosuite.runtime.testdata;


import org.evosuite.runtime.mock.java.net.MockInetAddress;
import org.evosuite.runtime.vnet.EndPointInfo;
import org.evosuite.runtime.vnet.NativeTcp;
import org.evosuite.runtime.vnet.RemoteTcpServer;
import org.evosuite.runtime.vnet.VirtualNetwork;

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

    /*
        WARN: the methods in this class are accessed by reflection in EnvironmentTestClusterAugmenter.
        Do not change signatures unless you update that class as well.
     */

	/**
	 * Unless otherwise specified, we simulate incoming connections all
	 * from same remote host
	 */
	private static final String DEFAULT_REMOTE_ADDRESS = "127.0.0.42";


    /**
     * Create a one-time listener on remote address/port
     * @param remoteServer
     * @return
     */
    public static boolean openRemoteTcpServer(EvoSuiteRemoteAddress remoteServer){
        if(remoteServer==null){
            return false;
        }

        RemoteTcpServer server = new RemoteTcpServer(new EndPointInfo(remoteServer.getHost(),remoteServer.getPort(), VirtualNetwork.ConnectionType.TCP));
        VirtualNetwork.getInstance().addRemoteTcpServer(server);

        return true;
    }

	/**
	 * Open new connection toward {@code sutServer} and buffer the content of {@code data}
	 * to be later sent once {@code sutServer} is opened
	 * 
	 * @param sutServer  the host/port of the SUT 
	 * @param data  if {@code null}, just simulate opening of connection
	 * @return {@code false} if {@code sutServer} is {@code null}
	 */
	public static boolean sendDataOnTcp(EvoSuiteLocalAddress sutServer, byte[] data){
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
     * Convert {@code message} to a byte array and send it with
     * {@link NetworkHandling#sendDataOnTcp}
     * @param sutServer
     * @param message
     * @return
     */
    public static boolean sendMessageOnTcp(EvoSuiteLocalAddress sutServer, String message){
        return sendDataOnTcp(sutServer,message.getBytes());
    }

    /**
     * Send UDP to SUT from an ephemeral port on a default remote host
     * @param sutAddress
     * @param data
     * @return
     */
    public static boolean sendUdpPacket(EvoSuiteLocalAddress sutAddress, byte[] data){
        return sendUdpPacket(sutAddress, new EvoSuiteRemoteAddress(DEFAULT_REMOTE_ADDRESS, VirtualNetwork.getInstance().getNewRemoteEphemeralPort()),data);
    }

    /**
     * Create a send a new UDP packet to the SUT. The packets are buffered till the SUT opens a socket
     * to read them.
     *
     * @param sutAddress
     * @param data
     * @return
     */
    public static boolean sendUdpPacket(EvoSuiteLocalAddress sutAddress, EvoSuiteRemoteAddress remoteAddress, byte[] data){
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
     *  Create a text file on mocked remote host that can be accessed with the given URL
     * @param url
     * @param text
     * @return
     */
    public static boolean createRemoteTextFile(EvoSuiteURL url, String text){
        if(url == null){
            return false;
        }
        return VirtualNetwork.getInstance().addRemoteTextFile(url.getUrl(),text);
    }
}
