package org.evosuite.runtime.vnet;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class used to store the state of a NetworkInterface.
 * Note: we cannot override NetworkInterface, and some of
 * its state is anyway not stored in its Java class (ie
 * it depends on the actual hardware, queried with native code).
 * 
 * @author arcuri
 *
 */
public class NetworkInterfaceState {

	private static final Logger logger = LoggerFactory.getLogger(NetworkInterfaceState.class);

	
	private NetworkInterface ni;	
	private final List<InetAddress> localAddresses;
	private final byte[] mac;
	private final int mtu;
	private final boolean loopback;
	
	public NetworkInterfaceState(
			String name,
			int index,
			byte[] mac,
			int mtu,
			boolean loopback,
			InetAddress anAddress) {
		
		this.mtu = mtu;
		this.loopback = loopback;		
		this.mac = mac!=null ? mac.clone() : null;
		// for now, we just consider one (IPv4) address per interface
		localAddresses = Collections.singletonList(anAddress);
		
		try {
			Constructor<NetworkInterface> constructor = NetworkInterface.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			ni = constructor.newInstance();
			Field nameField = NetworkInterface.class.getDeclaredField("name");
			nameField.setAccessible(true);
			Field indexField = NetworkInterface.class.getDeclaredField("index");
			indexField.setAccessible(true);
			nameField.set(ni, name);
			indexField.set(ni, index);
		} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			//shouldn't really happen
			logger.error("Reflection problems: "+e.getMessage());
		}
	}
	
	public NetworkInterface getNetworkInterface() {
		return ni;
	}

	
	public List<InetAddress> getLocalAddresses(){
		return localAddresses; 
	}

	public byte[] getMacAddr(){
		return mac.clone(); 
	}
	
	public int getMTU(){
		return mtu; 
	}

	public boolean isLoopback(){
		return loopback; 
	}

	/*
	 * For following methods we return constants.
	 * We could mock them, but not particularly important/useful
	 * for testing purposes
	 */
	
	public boolean isVirtual(){
		return false;
	}
	
	public boolean supportsMulticast(){
		return true;
	}
	
	public boolean isPointToPoint(){
		return false;
	}
	
	
	public boolean isUp(){
		return true;
	}
	
	public NetworkInterface getParent(){
		return null;
	}
}
