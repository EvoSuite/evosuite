package org.evosuite.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This singleton class is used to handle calls to System.in by
 * replacing them with a smart stub
 * 
 * @author arcuri
 *
 */
public class SystemInUtil extends InputStream{

	public final InputStream defaultIn = System.in;

	private static volatile SystemInUtil singleton = new SystemInUtil();
	
	/**
	 * Has System.in ever be used by the SUT?
	 */
	private volatile boolean beingUsed;
	
	/**
	 * The data that will be taken from System.in
	 */
	private volatile byte[] data;
	
	private volatile AtomicInteger counter;
	
	/**
	 * Hidden constructor
	 */
	protected SystemInUtil(){
		super();
		System.setIn(this);
		beingUsed = false;
	}
	
	public static synchronized SystemInUtil getInstance(){
		return singleton;
	}
	
	/**
	 * Reset the static state be re-instantiate the singleton
	 */
	public static synchronized void resetSingleton(){
		singleton = new SystemInUtil();
	}
	
	/**
	 * Setup mocked/stubbed System.in for the test case
	 */
	public void initForTestCase(){
		data = new byte[0];
		counter = new AtomicInteger(0);
	}
	
	/**
	 * Use given <code>input</code> string to represent the data
	 * that will be provided by System.in
	 * 
	 * @param input
	 */
	public void setInput(String input){
		if(input==null || input.isEmpty()){
			data = new byte[0];
		} else {			
			data = input.getBytes();
		}
	}
	
	@Override
	public int read() throws IOException {
		
		beingUsed = true;
		
		int i = counter.getAndIncrement();
		
		if(i==data.length){
			//first time we reach end of buffer, we return -1 to represents its end
			return -1; 
		}
		
		if(i>data.length){
			/*
			 * this is bit tricky situation.
			 * if we arrive here, it means that SUT has already asked for read(),
			 * got a -1, but then keep asking it.
			 * To avoid infinite loops and hard to kill threads, here we throw an
			 * IO exception (which would still be as part of normal behavior of read())
			 */
			throw new IOException("Asked to read from System.in when test case has decided to simulate an IO exception");
		}
		
		/*
		 * Note: it is important here that this read() is not blocking.
		 * 
		 * TODO: if needed, we could have a delay here to simulate a blocking
		 * operation till new data is provided to System.in.
		 * But it is likely not going to be so useful for unit testing
		 */
		return (int) data[i];
	}

	/**
	 * Has there be any call to System.in.read()?
	 * @return
	 */
	public boolean hasBeenUsed() {
		return beingUsed;
	}
	
}
