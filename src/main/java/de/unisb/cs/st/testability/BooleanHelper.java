package de.unisb.cs.st.testability;

import java.util.Stack;

import org.apache.log4j.Logger;

/**
 * Created by Yanchuan Li Date: 12/20/10 Time: 9:36 PM
 */
public class BooleanHelper<E> {
	private static Logger log = Logger.getLogger(BooleanHelper.class);
	private static Stack<Integer> iStack = new Stack<Integer>();

	private static Stack<Object> parametersObject = new Stack<Object>();
	private static Stack<Boolean> parametersBoolean = new Stack<Boolean>();
	private static Stack<Character> parametersChar = new Stack<Character>();
	private static Stack<Byte> parametersByte = new Stack<Byte>();
	private static Stack<Short> parametersShort = new Stack<Short>();
	private static Stack<Integer> parametersInteger = new Stack<Integer>();
	private static Stack<Float> parametersFloat = new Stack<Float>();
	private static Stack<Long> parametersLong = new Stack<Long>();
	private static Stack<Double> parametersDouble = new Stack<Double>();

	private static Stack<Double> dStack = new Stack<Double>();
	private static Stack<Long> lStack = new Stack<Long>();

	public static int getK() {
		return 10;
	}

	public static double popDoubleOperand() {
		return dStack.pop();
	}

	public static int computeDoubleDifference(double d1, double d2) {
		int difference = (int) (d1 - d2);
		dStack.push(d2);
		dStack.push(d1);
		return difference;
	}

	public static long popLongOperand() {
		return lStack.pop();
	}

	public static int computeLongDifference(long l1, long l2) {
		int difference = (int) (l1 - l2);
		lStack.push(l2);
		lStack.push(l1);
		return difference;
	}

	public static boolean RevertIntToBoolean(int i) {
		log.debug("RevertIntToBoolean:" + String.valueOf(i) + "->"
		        + String.valueOf(i > 0));
		return i > 0;
	}

	public static int convertBooleanToInt(boolean b) {
		log.debug("convertBooleanToInt:" + String.valueOf(b) + "->"
		        + String.valueOf(b ? 1 : -1));
		return b ? 1 : -1;
	}

	public static int getObjectEquality(Object a, Object b) {
		log.debug("getObjectEquality:" + String.valueOf(a == b ? 1 : -1));
		return a == b ? 1 : -1;
	}

	public static int isNull(Object o) {
		log.debug("isNull:" + String.valueOf(o == null ? 1 : -1));
		return o == null ? 1 : -1;
	}

	public static int isNotNull(Object o) {
		log.debug("isNotNull:" + String.valueOf(o != null ? 1 : -1));
		return o != null ? 1 : -1;
	}

	public static int popTrue() {
		if (iStack.empty()) {
			return getK();
		} else {
			int i = iStack.pop();
			long l = i;
			l = l > 0 ? l : -l;
			l = l + getK();
			i = l >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) l;
			log.debug("popTrue:" + i);
			return i;
		}
	}

	public static int popFalse() {
		if (iStack.empty()) {
			return 0 - getK();
		} else {
			int i = iStack.pop();
			long l = i;

			l = l <= 0 ? l : -l;
			l = l - getK();
			i = l <= Integer.MIN_VALUE ? Integer.MIN_VALUE : (int) l;
			log.debug("popFalse:" + i);
			return i;
		}
	}

	public static int pop() {
		if (iStack.empty()) {
			return 0;
		} else {
			return iStack.pop();
		}
	}

	public static void push(int i) {
		if (!iStack.isEmpty())
			log.warn("Stack size: " + iStack.size());
		iStack.push(i);
	}

	public static boolean popParameterBooleanFromInt() {
		int i = parametersInteger.pop();
		boolean result = i > 0;
		log.debug("popParameterBooleanFromInt:" + String.valueOf(i) + "->"
		        + String.valueOf(result));
		return result;
	}

	public static boolean popParameterBoolean() {
		return parametersBoolean.pop();
	}

	public static char popParameterChar() {
		return parametersChar.pop();
	}

	public static byte popParameterByte() {
		return parametersByte.pop();
	}

	public static short popParameterShort() {
		return parametersShort.pop();
	}

	public static int popParameterInt() {
		return parametersInteger.pop();
	}

	public static float popParameterFloat() {
		return parametersFloat.pop();
	}

	public static long popParameterLong() {
		return parametersLong.pop();
	}

	public static double popParameterDouble() {
		return parametersDouble.pop();
	}

	public static Object popParameterObject() {
		return parametersObject.pop();
	}

	public static Object popParameter(Object o) {
		return parametersObject.pop();
	}

	public static void pushParameter(boolean o) {
		log.debug("pushParameterBoolean:" + o);
		parametersBoolean.push(o);
	}

	public static void pushParameter(char o) {
		parametersChar.push(o);
	}

	public static void pushParameter(byte o) {
		parametersByte.push(o);
	}

	public static void pushParameter(short o) {
		parametersShort.push(o);
	}

	public static void pushParameter(int o) {
		log.debug("pushParameterInt:" + o);
		parametersInteger.push(o);
	}

	public static void pushParameter(float o) {
		parametersFloat.push(o);
	}

	public static void pushParameter(long o) {
		parametersLong.push(o);
	}

	public static void pushParameter(double o) {
		parametersDouble.push(o);
	}

	public static void pushParameter(Object o) {
		parametersObject.push(o);
	}

	public static int isInstanceOf(Object o, Class c) {
		if (o == null) {
			log.debug("isInstanceOf:-1");
			return -1;
		} else {
			log.debug("isInstanceOf:"
			        + String.valueOf(c.isAssignableFrom(o.getClass()) ? 1 : -1));
			return c.isAssignableFrom(o.getClass()) ? 1 : -1;
		}

	}

	public static int convertFloatDistance(float a) {
		//        if (a <= 0) {
		//            return (int) Math.floor(a);
		//        } else {
		return Math.round(a);
		//        }
	}

	public static int convertDoubleDistance(double a) {
		if (a <= 0) {
			return (int) Math.floor(a);
		} else {
			return (int) Math.round(a);
		}
	}

}
