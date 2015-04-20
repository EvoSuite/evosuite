package org.evosuite.runtime.mock.java.net;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import org.junit.Assert;

import org.evosuite.runtime.vnet.NetworkInterfaceState;
import org.evosuite.runtime.vnet.VirtualNetwork;
import org.junit.Test;

public class NetworkInterfaceTest {

	@Test
	public void test() throws SocketException{
		VirtualNetwork.getInstance().init();
		
		List<NetworkInterfaceState> list = 
				VirtualNetwork.getInstance().getAllNetworkInterfaceStates();
		
		//we want to mock at least 2 interfaces
		Assert.assertTrue(list.size() >= 2);
		
		boolean loopback = false;
		boolean no_loopback = false;
		
		for(NetworkInterfaceState nis : list){
			if(nis.isLoopback()){
				loopback = true;
			} else {
				no_loopback = true;
			}
			
			Assert.assertNotNull(nis.getNetworkInterface());
			Assert.assertNotNull(nis.getNetworkInterface().getName());
			Assert.assertTrue(! nis.getNetworkInterface().getName().trim().isEmpty());
			Assert.assertTrue(nis.getNetworkInterface().getIndex() > 0); //check that it is set, and we did not put any 0 
		}
		
		//we want to cover both cases
		Assert.assertTrue(loopback);
		Assert.assertTrue(no_loopback);
		
		int n = 0; 
		Enumeration<NetworkInterface> en = MockNetworkInterface.getNetworkInterfaces();
		while(en.hasMoreElements()){
			n++;
			en.nextElement();
		}
		
		Assert.assertEquals(list.size(), n);
	}
}
