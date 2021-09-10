/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package com.examples.with.different.packagename.papers.vnet;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class Example_UDP_TCP {

    public String getMessage(int port) throws IOException {

        //defines message to send in UDP broadcast
        InetAddress addr = InetAddress.getByName("192.168.0.1");
        String handShake = "HAND_SHAKE";
        String outMsg = addr + ":" + port + ":" + handShake;
        byte[] data = outMsg.getBytes();

        //send the message
        DatagramPacket packet = new DatagramPacket(data, data.length,
                InetAddress.getByName("255.255.255.255"), 12345);
        DatagramSocket udpSocket = new DatagramSocket();
        udpSocket.send(packet);

        //open listening TCP server based on sent out message
        ServerSocket tcpServer = new ServerSocket(port, 1, addr);
        Socket tcpSocket = tcpServer.accept();

        //read string from incoming TCP connection
        Scanner in = new Scanner(tcpSocket.getInputStream());
        String msg = in.nextLine();

        //close all resources
        in.close();
        tcpSocket.close();
        tcpServer.close();

        //check if first line contains the handshake token
        if (msg.startsWith(handShake)) {
            return msg;
        } else {
            throw new IOException("Invalid header in: " + msg);
        }
    }
}
