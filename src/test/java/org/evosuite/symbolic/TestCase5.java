package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase5 {

	public static void main(String[] args) {
		MathFloat mathClass0 = new MathFloat();
		float float0 = ConcolicMarker.mark(882.70544F,"var1");
		float float1 = ConcolicMarker.mark(882.70544F,"var2");
		float float2 = ConcolicMarker.mark(882.70544F, "var3");
		int int0 = mathClass0.castToInt(float0);
		float float3 = ConcolicMarker.mark(1.0F,"var4");
		mathClass0.unreach();
		float float4 = ConcolicMarker.mark(63.534046F,"var5");
		float float5 = mathClass0.multiply(float4, float2);
		float float6 = mathClass0.divide(float3, float1);
		int int1;
		if (float5>float6) {
			int1 = mathClass0.castToInt(float5);
		} else {
			int1 = mathClass0.castToInt(float6);
		}
	}
}
