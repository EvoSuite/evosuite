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

import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.testdata.EvoSuiteLocalAddress;
import org.evosuite.runtime.vnet.EndPointInfo;
import org.evosuite.runtime.testdata.EvoSuiteAddress;
import org.evosuite.runtime.testdata.NetworkHandling;
import org.evosuite.runtime.vnet.VirtualNetwork;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by arcuri on 12/9/14.
 */
public class UdpTest {

	@Before
	public void setupMock() {
		MockFramework.enable();
	}
	
	@After
	public void tearDownMock() {
		MockFramework.disable();
	}


    @Test
    public void multiOpenings() throws Exception{
        //be sure that no real UDP socket is opened. if it happens, then exception is thrown due to too many opened

        int n = 10_000;
        List<MockDatagramSocket> list = new ArrayList<>(n); //to avoid GC
        for(int i=0; i<n; i++){
            byte[] data = "Hello".getBytes();
            //send the message
            DatagramPacket packet = new DatagramPacket(data,data.length,
                    MockInetAddress.getByName("255.255.255.255"),12345);
            MockDatagramSocket socket = new MockDatagramSocket();
            socket.send(packet);

            list.add(socket);
        }
    }

    @Test
    public void testReceivePacket() throws Exception {
        int port = 12345;
        String host = "127.0.0.1";
        MockDatagramSocket socket = new MockDatagramSocket(port, InetAddress.getByName(host));

        byte[] data = new byte[]{42};
        NetworkHandling.sendUdpPacket(new EvoSuiteLocalAddress(host,port),data);

        DatagramPacket p = new DatagramPacket(new byte[1],1);
        socket.receive(p);

        byte[] received = p.getData();
        Assert.assertNotNull(received);
        Assert.assertEquals(42, received[0]);
    }

    @Test
    public void testNonBlockingRead() throws Exception{
        int port = 12345;
        String host = "127.0.0.1";
        MockDatagramSocket socket = new MockDatagramSocket(port, InetAddress.getByName(host));

        DatagramPacket p = new DatagramPacket(new byte[1],1);

        try {
            socket.receive(p); //no incoming package, but still should not block
            Assert.fail();
        } catch(Exception e){
            //expected
        }
    }

    @Test
    public void testSendPacket() throws Exception{
        String first = "127.0.42.1";
        String second = "127.0.42.2";
        MockDatagramSocket socket = new MockDatagramSocket(500);

        byte[] data = new byte[0];
        DatagramPacket packet = new DatagramPacket(
                data, 0 , InetAddress.getByName(first), 1234
        );

        //1 to first address
        socket.send(packet);

        packet.setAddress(InetAddress.getByName(second));
        packet.setPort(4567);

        //3 packets to the other
        socket.send(packet);
        socket.send(packet);
        socket.send(packet);

        Map<EndPointInfo,Integer> map = VirtualNetwork.getInstance().getCopyOfSentUDP();
        Assert.assertEquals(2 , map.size());

        for(Map.Entry<EndPointInfo,Integer> entry : map.entrySet()){
            if(entry.getKey().getHost().equals(first)){
                Assert.assertEquals(1 , entry.getValue().intValue());
            } else if(entry.getKey().getHost().equals(second)){
                Assert.assertEquals(3 , entry.getValue().intValue());
            } else {
                Assert.fail();
            }
        }
    }

}
