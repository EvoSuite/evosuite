package org.evosuite.rmi;

import java.net.ServerSocket;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import junit.framework.Assert;

import org.junit.Test;

public class OpenRegestryTest {

	@Test
	public void openTest() throws RemoteException, NotBoundException{
		int port = 2000;
		
		LocateRegistry.createRegistry(port);		
		Registry registry = LocateRegistry.getRegistry(port);
		Assert.assertNotNull(registry);

		try{
			LocateRegistry.createRegistry(port);
			Assert.fail();
		} catch(Exception e){			
		}

		try{
			ServerSocket socket = new ServerSocket(port); 
			Assert.fail();
		} catch(Exception e){			
		}
		
		FooImpl foo = new FooImpl();
		Ifoo stub = (Ifoo) UnicastRemoteObject.exportObject(foo,0);
		String service = "Foo";
		registry.rebind(service, stub);
		
		Ifoo lookedup = (Ifoo) registry.lookup(service);
		Assert.assertEquals("Hello World", lookedup.getString());
	}
	
	interface Ifoo extends Remote{
		public String getString() throws RemoteException;
	}
	
	class FooImpl implements Ifoo{

		@Override
		public String getString() throws RemoteException {
			return "Hello World";
		}
		
	}
}
