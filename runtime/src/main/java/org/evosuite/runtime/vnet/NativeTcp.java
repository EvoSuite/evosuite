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
package org.evosuite.runtime.vnet;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Class used to simulate a bi-directional TCP socket connection between two hosts.
 * This class only handle the exchange of data between SUT and EvoSuite tests, and not
 * whether the connections are actually in place
 * 
 * <p>
 * This class is thread-safe
 * 
 * @author arcuri
 *
 */
public class NativeTcp {

	/*
	 * Note: actual buffer sizes can be influenced by SO_SNDBUF and SO_RCVBUF socket options.
	 * But those are just "hints" for the OS
	 */
	
	/**
	 * The TCP buffer used locally by the SUT
	 */
	private final Queue<Byte> localBuffer;
	
	/**
	 * The TCP buffer used by the EvoSuite tests to simulate a remote connection
	 */
	private final Queue<Byte> remoteBuffer;
	
	/**
	 * Info on local (SUT) address/port
	 */
	private volatile EndPointInfo localEndPoint;
	
	/**
	 * Info on remote (EvoSuite tests) address/port
	 */
	private final EndPointInfo remoteEndPoint;
	
	
	public NativeTcp(EndPointInfo localEndPoint, EndPointInfo remoteEndPoint){
		if(remoteEndPoint==null){
			throw new IllegalArgumentException("Remote end point cannot be null");
		}
		
		this.localEndPoint = localEndPoint; //this can be null
		this.remoteEndPoint = remoteEndPoint;
		localBuffer = new ArrayDeque<>();
		remoteBuffer = new ArrayDeque<>();
	}
	
	public boolean isBound(){
		return localEndPoint != null;
	}
	
	
	public void bind(EndPointInfo local) throws IllegalStateException{
		if(isBound()){
			throw new IllegalStateException("Connection is already bound");
		}
		localEndPoint = local;
	}
	
	/**
	 *  Used by SUT to simulate sending of data to remote host
	 */
	public synchronized void writeToRemote(byte b){
		//the data is directly added to remote buffer
		remoteBuffer.add(b);
	}
	
	/**
	 *  Read one byte from stream
	 *  
	 *   @return a value between 0 and 255 representing a byte, or -1 if stream is empty.
     *   Note: in Java bytes are signed in -128,127, whereas here we need to return a unsigned
     *   int representation
	 */
	public synchronized int readInSUTfromRemote(){
		if(localBuffer.isEmpty()){
			/*
			 * Note: in  TCP, a read operation on a empty buffer would be blocking 
			 */
			return -1;  
		}
		
		return localBuffer.poll() & 0xFF;
	}
	
	/**
	 * Used by tests to simulate sending of data to the SUT opening a server connection
	 * @param b
	 */
	public synchronized void writeToSUT(byte b){
		localBuffer.add(b);
	}
		
	/**
	 * Get the data sent by the SUT.
	 * This would mainly be useful for assertion generation and
	 * not the search.
	 * 
	 * @return
	 */
	public synchronized int readInTestFromSUT(){
		if(remoteBuffer.isEmpty()){
			return -1;
		}
		return remoteBuffer.poll() & 0xFF;
	}

	/**
	 * @return the amount of data sent by the remote host and that
	 * has not been read yet by the local SUT 
	 */
	public synchronized int getAmountOfDataInLocalBuffer(){
		return localBuffer.size();
	}
	
	/**
	 * @return the amount of data sent by the SUT to the remote host
	 */
	public synchronized int getAmountOfDataInRemoteBuffer(){
		return remoteBuffer.size();
	}

	public EndPointInfo getLocalEndPoint() {
		return localEndPoint;
	}

	public EndPointInfo getRemoteEndPoint() {
		return remoteEndPoint;
	}
}
