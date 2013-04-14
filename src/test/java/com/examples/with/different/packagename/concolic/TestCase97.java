package com.examples.with.different.packagename.concolic;

import java.io.IOException;
import java.io.StringReader;

import org.evosuite.symbolic.Assertions;

public class TestCase97 {

	public static void test(String string0) {
		StringReader reader = new StringReader(string0);
		try {
			int int0 = reader.read();
			int int2 = (int) "Togliere sta roba".charAt(0);
			Assertions.checkEquals(int0, int2);
			
			int int1 = reader.read();
			int int3 = (int) "Togliere sta roba".charAt(1);
			Assertions.checkEquals(int1, int3);

			// consume remaining reader
			int int4 = reader.read();
			while (int4!=-1) {
				int4 =reader.read();
			}
			
		} catch (IOException e) {
			
		}
		
	}
}
