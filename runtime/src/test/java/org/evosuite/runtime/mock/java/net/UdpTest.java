package org.evosuite.runtime.mock.java.net;

import org.evosuite.runtime.vnet.EvoSuiteAddress;
import org.evosuite.runtime.vnet.NetworkHandling;
import org.junit.Assert;
import org.junit.Test;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by arcuri on 12/9/14.
 */
public class UdpTest {

    @Test
    public void testReceivePacket() throws Exception {
        int port = 12345;
        String host = "127.0.0.1";
        MockDatagramSocket socket = new MockDatagramSocket(port, InetAddress.getByName(host));

        byte[] data = new byte[]{42};
        NetworkHandling.sendUdpPacket(new EvoSuiteAddress(host,port),data);

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
            socket.receive(p);
            Assert.fail(); //no incoming package
        } catch(Exception e){
            //expected
        }
    }

    @Test
    public void testSendPacket(){
        //TODO
    }

}
