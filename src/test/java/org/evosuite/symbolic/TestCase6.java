package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase6 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MathFloat mathClass0 = new MathFloat();
		float float0 = ConcolicMarker.mark(1442.5817F,"var1");
		long long0 = mathClass0.castToLong(float0);
		float float1 = ConcolicMarker.mark(1.0F,"var2");
		short short0 = mathClass0.castToByte((float) long0);
		mathClass0.unreach();
		char char0 = mathClass0.castToChar(float1);
		float float2 = mathClass0.substract((float) char0, float1);
		if (float2!=0) {
			mathClass0.castToInt(float2);
		}
	}

}
