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
import java.net.*;
import java.util.Scanner;

import org.junit.Assert;

import org.evosuite.runtime.vnet.EndPointInfo;
import org.evosuite.runtime.vnet.RemoteTcpServer;
import org.evosuite.runtime.vnet.VirtualNetwork;
import org.evosuite.runtime.vnet.VirtualNetwork.ConnectionType;
import org.junit.Before;
import org.junit.Test;

public class SocketTest {

	@Before
	public void init(){
		VirtualNetwork.getInstance().init();
	}
	
	@Test
	public void testNullConnect() throws IOException{
		
		MockSocket s = new MockSocket();
		try {
			s.connect(null);
		} catch (IllegalArgumentException e) {
			//expected
		}
	}
	
	@Test
	public void testConnectNoAnswer() throws IOException{
		
		InetAddress addr = MockInetAddress.getByName("127.42.42.42");
		SocketAddress saddr = new MockInetSocketAddress(addr, 12345);
		MockSocket s = new MockSocket();
				
		try{
			s.connect(saddr);
			Assert.fail();
		} catch(IOException e){
			//expected, as no listener
		}		
	}
	
	
	@Test
	public void testSingleConnection() throws IOException{
		
		String remoteHost = "127.42.0.42";
		int remotePort = 666;
		InetSocketAddress saddr = new MockInetSocketAddress(MockInetAddress.getByName(remoteHost), remotePort);
		MockSocket s = new MockSocket();
		
		RemoteTcpServer server = new RemoteTcpServer(new EndPointInfo(saddr.getAddress().getHostAddress(), saddr.getPort(), ConnectionType.TCP));
        VirtualNetwork.getInstance().addRemoteTcpServer(server);
        String msgFromServer = "server";
		server.sendMessage(msgFromServer);
		
		s.connect(saddr);
		
		String msgFromSUT = "SUT";
		s.getOutputStream().write(msgFromSUT.getBytes());
		
		String sutReceived = new Scanner(s.getInputStream()).nextLine();
		Assert.assertEquals(msgFromServer, sutReceived);
		Assert.assertEquals(msgFromSUT, server.getAllReceivedDataAsString());
		
		s.close();
	}


    @Test
    public void testOperationsWithNoException() throws Exception {

        /*
            test is due to issue in loading org.apache.mina.transport.socket.nio.SocketSessionConfigImpl
            accessed by ioproject.client.network.Server in 77_io-project
         */

        Socket socket = new MockSocket();

        try {
            socket.getReuseAddress();
            int DEFAULT_RECEIVE_BUFFER_SIZE = socket.getReceiveBufferSize();
            int DEFAULT_SEND_BUFFER_SIZE = socket.getSendBufferSize();
            socket.getKeepAlive();
            socket.getOOBInline();
            socket.getSoLinger();
            socket.getTcpNoDelay();

            try {
                socket.setReceiveBufferSize(DEFAULT_RECEIVE_BUFFER_SIZE);
            } catch (SocketException e) {
            }

            try {
                socket.setSendBufferSize(DEFAULT_SEND_BUFFER_SIZE);
            } catch (SocketException e) {
            }

            try {
                socket.getTrafficClass();

            } catch (SocketException e) {

            }
        } catch (SocketException se){
            try {
                socket.close();
            } catch (IOException ioe) {
            }
        }
    }
}
