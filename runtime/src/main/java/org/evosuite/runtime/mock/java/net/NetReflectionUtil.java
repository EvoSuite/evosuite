package org.evosuite.runtime.mock.java.net;

import java.lang.reflect.Method;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class used to call package level methods in java.net
 * 
 * @author arcuri
 *
 */
public class NetReflectionUtil {

	private static final Logger logger = LoggerFactory.getLogger(NetReflectionUtil.class);
	
	/**
	 * Wrapper for {@code InetAddress.anyLocalAddress()} 
	 * @return
	 */
	public static InetAddress anyLocalAddress(){
				
		try {
			Method m = InetAddress.class.getDeclaredMethod("anyLocalAddress");
			m.setAccessible(true);
			return (InetAddress) m.invoke(null);
		} catch (Exception e) {
			//should never happen
			logger.error("Failed to use reflection on InetAddress.anyLocalAddress(): "+e.getMessage(),e);
		} 
		
		return null;
	}
}
