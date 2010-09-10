/**
 * 
 */
package de.unisb.cs.st.evosuite.string;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import de.unisb.cs.st.ga.Randomness;

/**
 * @author Gordon Fraser
 *
 */
public class StringPool {

	private Logger logger = Logger.getLogger(StringPool.class);
	
	private static StringPool instance = null;
	
	private Randomness randomness = Randomness.getInstance();
	
	private Set<String> pool = null;
	
	private StringPool() {
		pool = new HashSet<String>();
		pool.add("<xml>");
		pool.add("</xml>");
		pool.add("<test>");
		pool.add("</test>");
	}
	
	public static StringPool getInstance() {
		if(instance == null)
			instance = new StringPool();
		
		return instance;
	}
	
	public void addString(String str) {
		pool.add(str);
		logger.info("Added new string to pool: "+str);
	}
	
	public String getRandomString() {
		return randomness.choice(pool);
	}
}
