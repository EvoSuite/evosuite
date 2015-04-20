package com.examples.with.different.packagename.reset;

public class ClassWithStaticInitializerTryCatch {
	static{
		try {
			String.class.getMethod("A non existing method");
		} catch (NoSuchMethodException e) {
			//expected
		}
	}
}
