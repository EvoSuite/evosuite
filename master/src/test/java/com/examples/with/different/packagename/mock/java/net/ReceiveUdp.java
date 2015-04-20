package com.examples.with.different.packagename.mock.java.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by arcuri on 12/17/14.
 */
public class ReceiveUdp {

    public boolean listenForUdpPacket() throws Exception {

        byte[] buffer = new byte[512];
        DatagramPacket packet = new DatagramPacket(buffer,buffer.length);

        DatagramSocket socket = new DatagramSocket(1234, InetAddress.getByName("127.0.0.2"));
        socket.receive(packet);

        return packet.getPort() == 42;
    }

}
