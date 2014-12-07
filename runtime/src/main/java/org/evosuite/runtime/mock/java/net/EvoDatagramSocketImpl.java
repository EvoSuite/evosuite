package org.evosuite.runtime.mock.java.net;

import java.io.IOException;
import java.net.*;

/**
 * TODO 
 * 
 * Created by arcuri on 12/7/14.
 */
public class EvoDatagramSocketImpl extends DatagramSocketImpl{

    /*
        note: most methods here have "protected" access in superclass.
        we had to overload to public
     */

    @Override
    public void connect(InetAddress address, int port) throws SocketException {}

    @Override
    public void disconnect() {}

    @Override
    public int getLocalPort() {
        return super.getLocalPort();
    }



    // ------   abstract methods   ------

    @Override
    public void create() throws SocketException {

    }

    @Override
    public void bind(int lport, InetAddress laddr) throws SocketException {

    }

    @Override
    public void send(DatagramPacket p) throws IOException {

    }

    @Override
    public int peek(InetAddress i) throws IOException {
        return 0;
    }

    @Override
    public int peekData(DatagramPacket p) throws IOException {
        return 0;
    }

    @Override
    public void receive(DatagramPacket p) throws IOException {

    }

    @Override
    public void setTTL(byte ttl) throws IOException {

    }

    @Override
    public byte getTTL() throws IOException {
        return 0;
    }

    @Override
    public void setTimeToLive(int ttl) throws IOException {

    }

    @Override
    public int getTimeToLive() throws IOException {
        return 0;
    }

    @Override
    public void join(InetAddress inetaddr) throws IOException {

    }

    @Override
    public void leave(InetAddress inetaddr) throws IOException {

    }

    @Override
    public void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {

    }

    @Override
    public void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf) throws IOException {

    }

    @Override
    public void close() {

    }

    @Override
    public void setOption(int optID, Object value) throws SocketException {

    }

    @Override
    public Object getOption(int optID) throws SocketException {
        return null;
    }
}
