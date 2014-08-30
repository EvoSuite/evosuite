package org.evosuite.runtime.mock.java.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

import org.evosuite.runtime.mock.StaticReplacementMock;

/**
 * We need to mock this class mainly to handle hostnames resolutions,
 * which usually will be done through DNS and host files 
 * 
 */
public class MockInetAddress implements StaticReplacementMock{

	@Override
	public String getMockedClassName() {
		return InetAddress.class.getName();
	} 
	
	//-----  public instance methods -----------------
	/*
	 * Note: as we create instances with Inet4AddressUtil,
	 * here we do not need to mock these methods
	 */
	
	public static boolean isMulticastAddress(InetAddress addr){
		return addr.isMulticastAddress();
	}
	
	public static boolean isAnyLocalAddress(InetAddress addr){
		return addr.isAnyLocalAddress();
	}
	
	public static boolean isLoopbackAddress(InetAddress addr){
		return addr.isLoopbackAddress();
	}
	
	public static boolean isLinkLocalAddress(InetAddress addr){
		return addr.isLinkLocalAddress();
	}
	
	public static boolean isSiteLocalAddress(InetAddress addr){
		return addr.isSiteLocalAddress();
	}
	
	public static boolean isMCGlobal(InetAddress addr){
		return addr.isMCGlobal();
	}
	
	public static boolean isMCNodeLocal(InetAddress addr){
		return addr.isMCNodeLocal();
	}
	
	public static boolean isMCLinkLocal(InetAddress addr){
		return addr.isMCLinkLocal();
	}
	
	public static boolean isMCSiteLocal(InetAddress addr){
		return addr.isMCSiteLocal();
	}
	
	public static boolean isMCOrgLocal(InetAddress addr){
		return addr.isMCOrgLocal();
	}
	
    
    public static byte[] getAddress(InetAddress addr) {
    		return addr.getAddress();
    }
    
    public static String getHostAddress(InetAddress addr) {
    		return addr.getHostAddress();
     }

    public static int hashCode(InetAddress addr) {
        return addr.hashCode(); 
    }

    public static boolean equals(InetAddress addr, Object obj) {
        return addr.equals(obj); 
    }

    
    public static String toString(InetAddress addr) {
    		return addr.toString(); 
    }
    

	// ----- public instance methods depending on virtual network -----
	
	public static boolean isReachable(InetAddress addr, int timeout) throws IOException{
		return isReachable(addr, null, 0 , timeout);
	}

	public static boolean isReachable(InetAddress addr, NetworkInterface netif, int ttl,
            int timeout) throws IOException {
    		//TODO
    		return false;
    }
    
    public static String getHostName(InetAddress addr) {
    		//TODO
    		return null;
    }
    
    public static String getCanonicalHostName(InetAddress addr) {
    		//TODO
    		return null;
    }
    //------ static methods in mocked ---------
    
    public static InetAddress getByAddress(String host, byte[] addr)
            throws UnknownHostException {
    		return null; //TODO
    }
    
    public static InetAddress getByName(String host){
    		return null; //TODO 
    }

    public static InetAddress[] getAllByName(String host){
    		return null; //TODO
    }
    
    public static InetAddress getLoopbackAddress() {
    		return null; //TODO
    }
    
    public static InetAddress getByAddress(byte[] addr)
    		throws UnknownHostException {
    		return null; //TODO
    }

    public static InetAddress getLocalHost() throws UnknownHostException {
    		return null; //TODO
    }

}	
