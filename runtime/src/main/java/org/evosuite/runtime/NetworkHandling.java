package org.evosuite.runtime;

import org.evosuite.runtime.vnet.VirtualNetwork;

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
			
	
	public static boolean sendDataOnTcp(EvoSuiteAddress sutServer, byte[] data){
		if(sutServer==null){
			return false;
		}
		
		VirtualNetwork.getInstance().registerIncomingTcpConnection(
				null, -1, // TODO 
				sutServer.getHost(), sutServer.getPort());
		
		//TODO send message
		//TODO close connection?
		
		return true;
	}
}
