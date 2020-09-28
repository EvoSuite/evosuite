/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import org.evosuite.runtime.mock.StaticReplacementMock;
import org.evosuite.runtime.vnet.NetworkInterfaceState;
import org.evosuite.runtime.vnet.VirtualNetwork;

public class MockNetworkInterface implements StaticReplacementMock{

	@Override
	public String getMockedClassName() {		
		return NetworkInterface.class.getName();
	}


	public static Enumeration<InetAddress> getInetAddresses(final NetworkInterface ni) {

		class Enumuerator implements Enumeration<InetAddress> {
			private int i=0;
			private List<InetAddress> local_addrs;

			Enumuerator() {
				local_addrs = VirtualNetwork.getInstance().getNetworkInterfaceState(ni.getName()).getLocalAddresses();
			}

			public InetAddress nextElement() {
				if (i < local_addrs.size()) {
					return local_addrs.get(i++);
				} else {
					throw new NoSuchElementException();
				}
			}

			public boolean hasMoreElements() {
				return (i < local_addrs.size());
			}
		}
		return new Enumuerator();
	}

	
	public static List<InterfaceAddress> getInterfaceAddresses(NetworkInterface ni) {
		/*
		 * TODO: bit cumbersome to mock, and never used in SF110
		 */
		throw new RuntimeException("Not supported method by EvoSuite");
	}

	
	public static Enumeration<NetworkInterface> getSubInterfaces(NetworkInterface ni) {
		// No sub-interface
		class subIFs implements Enumeration<NetworkInterface> {
			public NetworkInterface nextElement() {
					throw new NoSuchElementException();
			}
			public boolean hasMoreElements() {
				return false;
			}
		}
		return new subIFs();
	}

	public static int getIndex(NetworkInterface ni) {
		return ni.getIndex();
	}

	public static String getName(NetworkInterface ni) {
		return ni.getName();
	}

	public static String getDisplayName(NetworkInterface ni) {
		//simplified, just return name
		return ni.getName();
	}

	public static boolean isUp(NetworkInterface ni) throws SocketException {
		return VirtualNetwork.getInstance().getNetworkInterfaceState(ni.getName()).isUp();
	}

	public static NetworkInterface getParent(NetworkInterface ni) {
		return VirtualNetwork.getInstance().getNetworkInterfaceState(ni.getName()).getParent();
	}

	public static boolean isLoopback(NetworkInterface ni) throws SocketException {
		return VirtualNetwork.getInstance().getNetworkInterfaceState(ni.getName()).isLoopback(); 
	}

	public static boolean isPointToPoint(NetworkInterface ni) throws SocketException {
		return VirtualNetwork.getInstance().getNetworkInterfaceState(ni.getName()).isPointToPoint();
	}


	public static boolean supportsMulticast(NetworkInterface ni) throws SocketException {
		return VirtualNetwork.getInstance().getNetworkInterfaceState(ni.getName()).supportsMulticast();
	}

	public static byte[] getHardwareAddress(NetworkInterface ni) throws SocketException {
		return VirtualNetwork.getInstance().getNetworkInterfaceState(ni.getName()).getMacAddr();
	}

	public static int getMTU(NetworkInterface ni) throws SocketException {
		return VirtualNetwork.getInstance().getNetworkInterfaceState(ni.getName()).getMTU();
	}

	public static boolean isVirtual(NetworkInterface ni) {
		return VirtualNetwork.getInstance().getNetworkInterfaceState(ni.getName()).isVirtual();
	}


	// ---- static in NetworkInterface -------
	
	public static NetworkInterface getByName(String name) throws SocketException {
		if (name == null)
			throw new NullPointerException();
		NetworkInterfaceState state =  VirtualNetwork.getInstance().getNetworkInterfaceState(name);
		if(state == null){
			return null;
		}
		return state.getNetworkInterface();
	}

	
	public static NetworkInterface getByIndex(int index) throws SocketException {
		if (index < 0)
			throw new IllegalArgumentException("Interface index can't be negative");
		
		for(NetworkInterfaceState nis : VirtualNetwork.getInstance().getAllNetworkInterfaceStates()){
			if(nis.getNetworkInterface().getIndex()==index){
				return nis.getNetworkInterface();
			}
		}		
		return null; 
	}

	
	public static NetworkInterface getByInetAddress(InetAddress addr) throws SocketException {
		if (addr == null) {
			throw new NullPointerException();
		}
		if (!(addr instanceof Inet4Address || addr instanceof Inet6Address)) {
			throw new IllegalArgumentException ("invalid address type");
		}
		
		for(NetworkInterfaceState nis : VirtualNetwork.getInstance().getAllNetworkInterfaceStates()){
			if(nis.getLocalAddresses().contains(addr)){
				return nis.getNetworkInterface();
			}
		}
		
		return null; 
	}

	
	public static Enumeration<NetworkInterface> getNetworkInterfaces()
			throws SocketException {
		
		final List<NetworkInterfaceState> netifs = 
				VirtualNetwork.getInstance().getAllNetworkInterfaceStates();

		// specified to return null if no network interfaces
		if (netifs == null || netifs.isEmpty()){
			return null;
		}

		return new Enumeration<NetworkInterface>() {
			private int i = 0;
			public NetworkInterface nextElement() {
				if (netifs != null && i < netifs.size()) {
					NetworkInterface netif = netifs.get(i).getNetworkInterface();
					i++;
					return netif;
				} else {
					throw new NoSuchElementException();
				}
			}
			public boolean hasMoreElements() {
				return (netifs != null && i < netifs.size());
			}
		};
	}


	// ----- Object overridden ----------
	
	public static boolean equals(NetworkInterface ni, Object obj) {
		return ni.equals(obj);
	}

	public static int hashCode(NetworkInterface ni) {
		return ni.hashCode();
	}

	public static String toString(NetworkInterface ni) {
		return ni.toString();
	}

}
