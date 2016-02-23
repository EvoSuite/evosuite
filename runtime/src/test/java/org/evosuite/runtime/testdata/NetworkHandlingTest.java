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
package org.evosuite.runtime.testdata;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.mock.MockFramework;
import org.evosuite.runtime.mock.java.net.MockDatagramSocket;
import org.evosuite.runtime.mock.java.net.MockInetAddress;
import org.evosuite.runtime.mock.java.net.MockURL;
import org.evosuite.runtime.vnet.VirtualNetwork;
import org.junit.After;
import org.junit.Assert;
import org.evosuite.runtime.mock.java.net.MockServerSocket;
import org.junit.Before;
import org.junit.Test;

import java.net.*;
import java.util.Scanner;

/**
 * Created by arcuri on 12/12/14.
 */
public class NetworkHandlingTest {

    private static final boolean VNET = RuntimeSettings.useVNET;

    @Before
    public void init(){
        RuntimeSettings.useVNET = true;
        VirtualNetwork.getInstance().reset();
        MockFramework.enable();
    }

    @After
    public void tearDown(){
        RuntimeSettings.useVNET = VNET;
        MockFramework.disable();
    }

    @Test(timeout = 500)
    public void testOpenedRemoteTCP() throws Exception{

        EvoSuiteLocalAddress addr = new EvoSuiteLocalAddress("127.42.42.42",42);
        NetworkHandling.sendDataOnTcp(addr,null);

        MockServerSocket sut = new MockServerSocket(addr.getPort(), 1,
                MockInetAddress.getByName(addr.getHost()));
        Socket socket = sut.accept(); //should not block, should not timeout
        Assert.assertNotNull(socket);
    }

    @Test (timeout = 500)
    public void testSendUdp() throws  Exception{

        EvoSuiteLocalAddress sut = new EvoSuiteLocalAddress("127.42.42.42",42);
        EvoSuiteRemoteAddress remote = new EvoSuiteRemoteAddress("127.62.62.62",62);

        String msg = "foo";
        byte[] data = msg.getBytes();
        NetworkHandling.sendUdpPacket(sut,remote,data);

        DatagramPacket packet = new DatagramPacket(new byte[10],10);
        MockDatagramSocket socket = new MockDatagramSocket(sut.getPort(),
                 MockInetAddress.getByName(sut.getHost()));

        socket.receive(packet); //no blocking, no exception
        Assert.assertEquals(remote.getPort(), packet.getPort());
        Assert.assertEquals(remote.getHost(), packet.getAddress().getHostAddress());
        Assert.assertEquals(msg , new String(packet.getData()));
    }


    @Test
    public void testURL() throws Exception{
        String text = "Hello World!";
        EvoSuiteURL url = new EvoSuiteURL("http://evosuite.org/hello.txt");

        NetworkHandling.createRemoteTextFile(url,text);

        URL mock = MockURL.URL(url.getUrl());
        URLConnection connection = mock.openConnection();
        Scanner in = new Scanner(connection.getInputStream());
        String res = in.nextLine();
        Assert.assertEquals(text,res);
    }
}
