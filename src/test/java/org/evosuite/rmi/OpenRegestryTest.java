package org.evosuite.rmi;

import java.net.ServerSocket;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.junit.Assert;

import org.junit.Test;

public class OpenRegestryTest {

	@Test
	public void openTest() throws RemoteException, NotBoundException{
		int port = 2000;
		
		for(int i=0; i<10000; i++){
			try{
				LocateRegistry.createRegistry(port);	
				break;
			} catch(java.rmi.server.ExportException e){
				//it could happen that the port is already in use
				port++;
			}
		}
		
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
		Ifoo stub = (Ifoo) UnicastRemoteObject.exportObject(foo,port);
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
