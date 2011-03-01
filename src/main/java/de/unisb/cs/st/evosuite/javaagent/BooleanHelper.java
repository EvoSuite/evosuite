/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.Stack;

/**
 * @author Gordon Fraser
 * 
 */
public class BooleanHelper {

	private static Stack<Object> parametersObject = new Stack<Object>();
	private static Stack<Boolean> parametersBoolean = new Stack<Boolean>();
	private static Stack<Character> parametersChar = new Stack<Character>();
	private static Stack<Byte> parametersByte = new Stack<Byte>();
	private static Stack<Short> parametersShort = new Stack<Short>();
	private static Stack<Integer> parametersInteger = new Stack<Integer>();
	private static Stack<Float> parametersFloat = new Stack<Float>();
	private static Stack<Long> parametersLong = new Stack<Long>();
	private static Stack<Double> parametersDouble = new Stack<Double>();

	public static boolean popParameterBooleanFromInt() {
		int i = parametersInteger.pop();
		boolean result = i > 0;
		return result;
	}

	public static int popParameterIntFromBoolean() {
		boolean i = parametersBoolean.pop();
		if (i)
			return TestabilityTransformation.K;
		else
			return -TestabilityTransformation.K;
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
}
