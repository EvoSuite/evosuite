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

import org.evosuite.runtime.mock.OverrideMock;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketImpl;


public abstract class MockSocketImpl extends SocketImpl implements OverrideMock {

    /*
        does not seem to really need any rollback
     */

    protected MockSocket socket = null;
    protected MockServerSocket serverSocket = null;

    /*
    protected FileDescriptor fd;
    protected InetAddress address;
    protected int port;
    protected int localport;
     */
    
    /*
     * Abstract methods from superclass.
     * Still need to be declared here, because they might be
     * called by other net classes in same package
     */
    protected abstract void create(boolean stream) throws IOException;
    protected abstract void connect(String host, int port) throws IOException;
    protected abstract void connect(InetAddress address, int port) throws IOException;
    protected abstract void connect(SocketAddress address, int timeout) throws IOException;
    protected abstract void bind(InetAddress host, int port) throws IOException;
    protected abstract void listen(int backlog) throws IOException;
    protected abstract void accept(SocketImpl s) throws IOException;
    protected abstract InputStream getInputStream() throws IOException;
    protected abstract OutputStream getOutputStream() throws IOException;
    protected abstract int available() throws IOException;
    protected abstract void close() throws IOException;
    protected abstract void sendUrgentData (int data) throws IOException;
     

    @Override
    protected void shutdownInput() throws IOException {
      //throw new IOException("Method not implemented!");
    		super.shutdownInput();
    }

    @Override
    protected void shutdownOutput() throws IOException {
      //throw new IOException("Method not implemented!");
    		super.shutdownOutput();
    }

    @Override
    protected FileDescriptor getFileDescriptor() {
        //return fd;
    		return super.getFileDescriptor();
    }

    @Override
    protected InetAddress getInetAddress() {
        //return address;
    		return super.getInetAddress();
    }

    @Override
    protected int getPort() {
        //return port;
    		return super.getPort();
    }

    @Override
    protected int getLocalPort() {
        //return localport;
    		return super.getLocalPort();
    }
        
    @Override
    protected boolean supportsUrgentData () {
        //return false; // must be overridden in sub-class
    		return super.supportsUrgentData();
    }

    @Override
    protected void setPerformancePreferences(int connectionTime,
                                          int latency,
                                          int bandwidth){
        super.setPerformancePreferences(connectionTime, latency, bandwidth);
    }
    
    @Override
    public String toString() {
    		return super.toString();
        //return "Socket[addr=" + getInetAddress() +
          //  ",port=" + getPort() + ",localport=" + getLocalPort()  + "]";
    }

    //-----------------------------------------------

    protected void setRemoteAddress(InetAddress remoteAddress){
    		address = remoteAddress;
    }
        
    protected void setRemotePort(int p){
    		port = p;
    }
    
    protected void setLocalPort(int p){
    		localport = p;
    }
    
    //-----------------------------------------------
    
    /*
     *  Following are methods that are package level 
     *  in SocketImpl, and as such they cannot be overridden
     *  
     *  TODO need to check ALL of their callers in java.net.*
     */
    
    
    protected void setSocket(MockSocket soc) {
        this.socket = soc;
    }

    protected Socket getSocket() {
        return socket;
    }

    protected void setServerSocket(MockServerSocket soc) {
        this.serverSocket = soc;
    }

    protected ServerSocket getServerSocket() {
        return serverSocket;
    }


    protected void reset() throws IOException {
        address = null;
        port = 0;
        localport = 0;
    }

	
}
