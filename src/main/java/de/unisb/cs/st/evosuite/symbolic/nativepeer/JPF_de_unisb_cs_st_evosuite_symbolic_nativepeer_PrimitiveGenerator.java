package de.unisb.cs.st.evosuite.symbolic.nativepeer;

import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.search.Search;

import java.util.HashMap;
import java.util.Map;

import jpf.mytest.integer.IntegerNextChoiceProvider;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.RealVariable;

public class JPF_de_unisb_cs_st_evosuite_symbolic_nativepeer_PrimitiveGenerator {
	//extends
	//}
	//        ChoiceStorage {

	private static IntegerNextChoiceProvider provider = new IntegerNextChoiceProvider();

	private static String prefix;
	private static Map<Integer, String> storeMap;

	public final static char BOOLEAN_CHARACTER = 'B';
	public final static char BYTE_CHARACTER = 'b';
	public final static char SHORT_CHARACTER = 's';
	public final static char CHAR_CHARACTER = 'c';
	public final static char INT_CHARACTER = 'i';
	public final static char LONG_CHARACTER = 'l';
	public final static char FLOAT_CHARACTER = 'f';
	public final static char DOUBLE_CHARACTER = 'd';

	static public void init() {
		storeMap = new HashMap<Integer, String>();
		prefix = "";
	}

	/**
	 * Called from Dummy
	 * 
	 * @param search
	 *            the search object
	 */
	static public void stateStored(Search search) {
		int i = JVM.getVM().getStateId();
		storeMap.put(i, prefix);
	}

	/**
	 * Called from Dummy
	 * 
	 * @param search
	 *            the search object
	 */
	static public void stateRestored(Search search) {
		int i = JVM.getVM().getStateId();
		String restore = storeMap.get(i);
		if (restore == null) {
			throw new RuntimeException("tried to restore a not stored state");
		}
		prefix = restore;
	}

	public static byte getByte(MJIEnv env, int rcls) {
		prefix += BYTE_CHARACTER;
		env.setReturnAttribute(new IntegerVariable(prefix + "__SYM", Byte.MIN_VALUE,
		        Byte.MAX_VALUE));
		return provider.nextByteChoice();
	}

	public static short getShort(MJIEnv env, int rcls) {
		prefix += SHORT_CHARACTER;
		env.setReturnAttribute(new IntegerVariable(prefix + "__SYM", Short.MIN_VALUE,
		        Short.MAX_VALUE));
		return provider.nextShortChoice();
	}

	public static char getChar(MJIEnv env, int rcls) {
		prefix += CHAR_CHARACTER;
		env.setReturnAttribute(new IntegerVariable(prefix + "__SYM", Character.MIN_VALUE,
		        Character.MAX_VALUE));
		return provider.nextCharChoice();
	}

	public static int getInt(MJIEnv env, int rcls) {
		prefix += INT_CHARACTER;
		env.setReturnAttribute(new IntegerVariable(prefix + "__SYM", Integer.MIN_VALUE,
		        Integer.MAX_VALUE));
		return provider.nextIntChoice();
	}

	public static long getLong(MJIEnv env, int rcls) {
		prefix += LONG_CHARACTER;
		env.setReturnAttribute(new IntegerVariable(prefix + "__SYM", Long.MIN_VALUE,
		        Long.MAX_VALUE));
		return provider.nextLongChoice();
	}

	public static float getFloat(MJIEnv env, int rcls) {
		prefix += FLOAT_CHARACTER;
		env.setReturnAttribute(new RealVariable(prefix + "__SYM", -Float.MAX_VALUE,
		        Float.MAX_VALUE));
		return provider.nextFloatChoice();
	}

	public static double getDouble(MJIEnv env, int rcls) {
		prefix += DOUBLE_CHARACTER;
		env.setReturnAttribute(new RealVariable(prefix + "__SYM", -Double.MAX_VALUE,
		        Double.MAX_VALUE));
		return provider.nextDoubleChoice();
	}

	public static boolean getBoolean(MJIEnv env, int rcls) {
		prefix += BOOLEAN_CHARACTER;
		env.setReturnAttribute(new IntegerVariable(prefix + "__SYM", Integer.MIN_VALUE,
		        Integer.MAX_VALUE));
		return provider.nextBooleanChoice();
	}
}