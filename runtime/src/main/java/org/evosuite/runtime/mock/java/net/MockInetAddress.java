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
package org.evosuite.runtime.mock.java.net;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.vnet.EvoIPAddressUtil;
import org.evosuite.runtime.vnet.NetworkInterfaceState;
import org.evosuite.runtime.vnet.VirtualNetwork;

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

		if (ttl < 0)
			throw new IllegalArgumentException("ttl can't be negative");
		if (timeout < 0)
			throw new IllegalArgumentException("timeout can't be negative");

		/*
		 * TODO: for now, we are assuming all hosts are reachable
		 */
		return true;
	}

	public static String getHostName(InetAddress addr) {
		/*
		 * We return the textual IP address instead of a lookup
		 * as that is what would be returned in case of error 
		 */
		return addr.getHostAddress();
	}

	public static String getCanonicalHostName(InetAddress addr) {
		//see the callee
		return getHostName(addr);
	}



	//------ static methods in mocked ---------

	public static InetAddress getByAddress(byte[] addr)
			throws UnknownHostException {
		return getByAddress(null, addr);
	}


	public static InetAddress getByAddress(String host, byte[] addr)
			throws UnknownHostException {

		if (host != null && host.length() > 0 && host.charAt(0) == '[') {
			if (host.charAt(host.length()-1) == ']') {
				host = host.substring(1, host.length() -1);
			}
		}

		if (addr != null && addr.length == Inet4AddressUtil.INADDRSZ) {
			return Inet4AddressUtil.createNewInstance(host, addr);
		}

		throw new UnknownHostException("Not IPv4: "+Arrays.toString(addr)); 
	}


	public static InetAddress getByName(String host)
			throws UnknownHostException{
		return getAllByName(host)[0]; 
	}

	public static InetAddress[] getAllByName(String host)
			throws UnknownHostException{

		//if no specified host, return loopback
		if (host == null || host.length() == 0) {
			InetAddress[] ret = new InetAddress[1];
			ret[0] = getLoopbackAddress();
			return ret;
		}

		// if host is an IPv4 address, we won't do further lookup
		if (Character.digit(host.charAt(0), 16) != -1) {

			// see if it is IPv4 address
			byte[] addr = EvoIPAddressUtil.textToNumericFormatV4(host); 

			if(addr != null && addr.length == Inet4AddressUtil.INADDRSZ) {
				InetAddress[] ret = new InetAddress[1];
				ret[0] = Inet4AddressUtil.createNewInstance(null, addr); 
				return ret;
			}
		} 

		String resolved = VirtualNetwork.getInstance().dnsResolve(host);
		if(resolved == null){
			throw new UnknownHostException("Cannot resolve: "+resolved);
		}

		byte[] addr = EvoIPAddressUtil.textToNumericFormatV4(resolved);
		InetAddress[] ret = new InetAddress[1];
		ret[0] = Inet4AddressUtil.createNewInstance(host, addr); 
		return ret;		
	}


	public static InetAddress getLoopbackAddress() {
        return 	getFirstValid(true);
	}

	public static InetAddress getLocalHost() throws UnknownHostException {
		/* 
		 * for simplicity, just return the first address, and fall back
		 * to loopback if none exists.
		 * TODO: this ll need to be changed if we mock the name
		 * of the machine
		 */
        InetAddress addr = getFirstValid(false);
        if(addr == null){
            getLoopbackAddress();
        }
		return addr;
	}

    private static InetAddress getFirstValid(boolean loopback){
        List<NetworkInterfaceState> list =
                VirtualNetwork.getInstance().getAllNetworkInterfaceStates();

        for(NetworkInterfaceState nis : list){
            if(nis.isLoopback() != loopback){
                continue;
            }

            List<InetAddress> addresses = nis.getLocalAddresses();
            if(addresses==null || addresses.isEmpty()){
                continue;
            }

            return addresses.get(0);
        }

        return null; //nothing  found.
    }

	// ------- package level ----------


	public static InetAddress anyLocalAddress() {

        /*
            TODO: handling 0.0.0.0 is tricky, as it bounds to all local interfaces.
            As multi-homing is not so common, for now we just use localhost.
         */

        try {
            return getLocalHost();
			//return getByName("0.0.0.0"); //TODO would need to modify VirtualNetwork to support it
		} catch (UnknownHostException e) {
			//should never happen
			return null;
		}
	}
}	
