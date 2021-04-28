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
package org.evosuite.runtime.mock.java.net;

import org.evosuite.runtime.mock.java.io.MockIOException;
import org.evosuite.runtime.mock.java.lang.MockNullPointerException;
import org.evosuite.runtime.vnet.VirtualNetwork;

import java.io.IOException;
import java.net.*;

/**
 * Note: UDP is seldom used, and most of the functions here are not called in SF110.
 * Implementing all of them is therefore not the top priority
 * 
 * Created by arcuri on 12/7/14.
 */
public class EvoDatagramSocketImpl extends DatagramSocketImpl{

    private String localHost;

    /*
        note: most methods here have "protected" access in superclass.
        we had to overload to public
     */

    @Override
    public void connect(InetAddress address, int port) throws SocketException {
        //TODO
    }

    @Override
    public void disconnect() {
        //TODO
    }

    @Override
    public int getLocalPort() {
        return super.getLocalPort();
    }



    // ------   abstract methods   ------

    @Override
    public void create() throws SocketException {
        //nothing to do?
    }

    @Override
    public void bind(int lport, InetAddress laddr) throws SocketException {

        if(lport == 0){
            lport = VirtualNetwork.getInstance().getNewLocalEphemeralPort();
        }

        localPort = lport;
        localHost = laddr.getHostAddress();
        VirtualNetwork.getInstance().openUdpServer(localHost,localPort);
    }

    @Override
    public void send(DatagramPacket p) throws IOException {
        if(p.getData()==null || p.getAddress()==null){
            throw new MockNullPointerException("null buffer || null address");
        }
        VirtualNetwork.getInstance().sentPacketBySUT(p);
    }

    @Override
    public void receive(DatagramPacket p) throws IOException {
       DatagramPacket received = VirtualNetwork.getInstance().pullUdpPacket(localHost,localPort);
       if(received != null){
           p.setData(received.getData());
           p.setAddress(received.getAddress());
           p.setPort(received.getPort());
           p.setLength(received.getLength());
       } else {
           //no point in simulating a blocking call
           throw new MockIOException("Simulated IO exception");
       }
    }

    @Override
    public int peek(InetAddress i) throws IOException {
        //TODO
        return 0;
    }

    @Override
    public int peekData(DatagramPacket p) throws IOException {
        //TODO
        return 0;
    }

    @Override
    public void setTTL(byte ttl) throws IOException {
        //TODO
    }

    @Override
    public byte getTTL() throws IOException {
        //TODO
        return 0;
    }

    @Override
    public void setTimeToLive(int ttl) throws IOException {
        //TODO
    }

    @Override
    public int getTimeToLive() throws IOException {
        //TODO
        return 0;
    }

    @Override
    public void join(InetAddress inetaddr) throws IOException {
        //TODO
    }

    @Override
    public void leave(InetAddress inetaddr) throws IOException {
        //TODO
    }

    @Override
    public void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        //TODO
    }

    @Override
    public void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {
        //TODO
    }

    @Override
    public void close() {
        //TODO
    }

    @Override
    public void setOption(int optID, Object value) throws SocketException {
        //TODO
    }

    @Override
    public Object getOption(int optID) throws SocketException {
        //TODO
        return null;
    }
}
