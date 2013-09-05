package org.evosuite.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.evosuite.Properties;
import org.evosuite.setup.TestCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This singleton class is used to handle calls to System.in by
 * replacing them with a smart stub
 * 
 * @author arcuri
 *
 */
public class SystemInUtil extends InputStream{

	public final InputStream defaultIn = System.in;

	private static Logger logger = LoggerFactory.getLogger(SystemInUtil.class);

	private static volatile SystemInUtil singleton = new SystemInUtil();

	/**
	 * Has System.in ever be used by the SUT?
	 */
	private volatile boolean beingUsed;

	/**
	 * The data that will be taken from System.in
	 */
	private volatile List<Byte> data;

	private volatile AtomicInteger counter;

	private boolean hasAddedSupport;

	/**
	 * Hidden constructor
	 */
	protected SystemInUtil(){
		super();
		beingUsed = false;
		if(Properties.REPLACE_SYSTEM_IN){
			System.setIn(this);
		}
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
		data = new ArrayList<Byte>();
		counter = new AtomicInteger(0);
	}

	/**
	 * Use given <code>input</code> string to represent the data
	 * that will be provided by System.in.
	 * The string will be appended to current buffer as a new line,
	 * i.e. by adding "\n" to the <code>input</code> string
	 * 
	 * @param input	A string representing an input on the console
	 */
	public static void addInputLine(String input){
		if(input==null){
			return;
		} 

		/*
		 * Note: this method needs to be static, as we call it directly in the test cases.
		 */

		synchronized(singleton.data){
			String line = input+"\n";
			for(byte b : line.getBytes()){
				singleton.data.add((Byte)b);
			}		
		}
	}

	/**
	 * If System.in was used, add methods to handle/simulate it
	 */
	public void addSupportInTestClusterIfNeeded(){
		if(!beingUsed || hasAddedSupport){
			return;
		}

		logger.debug("Going to add support for System.in");
		hasAddedSupport = true;

		try {
			TestCluster.getInstance().addTestCall(new GenericMethod(
					SystemInUtil.class.getMethod("addInputLine",new Class<?>[] { String.class }),
					new GenericClass(SystemInUtil.class)));
		} catch (SecurityException e) {
			logger.error("Error while handling Random: "+e.getMessage(),e);
		} catch (NoSuchMethodException e) {
			logger.error("Error while handling Random: "+e.getMessage(),e);
		}
	}


	@Override
	public int read() throws IOException {

		beingUsed = true;

		int i = counter.getAndIncrement();

		if(i==data.size()){
			//first time we reach end of buffer, we return -1 to represents its end
			return -1; 
		}

		if(i>data.size()){
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
		return (int) data.get(i);
	}

	/**
	 * Has there be any call to System.in.read()?
	 * @return
	 */
	public boolean hasBeenUsed() {
		return beingUsed;
	}

}
