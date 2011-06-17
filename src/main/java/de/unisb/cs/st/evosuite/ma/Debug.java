package de.unisb.cs.st.evosuite.ma;

/**
 * @author Yury Pavlov
 * 
 */
public class Debug {
	static boolean DEBUG = true;
	static boolean DEBUG_CONN = true;
	

	static public void printDeb(String str) {
		if (DEBUG)
			System.out.println(str);
	}

	static public void printDebConn(String str) {
		if (DEBUG_CONN)
			System.out.println(str);
	}

}
