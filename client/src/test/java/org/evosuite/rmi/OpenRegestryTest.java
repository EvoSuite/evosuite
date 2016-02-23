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
