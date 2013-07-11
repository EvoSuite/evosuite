package com.examples.with.different.packagename;

public class AccessExamples extends com.examples.with.different.packagename.test.AccessExamples {

	public String publicField = "";

	String defaultField = "";

	protected String protectedField = "";

	@SuppressWarnings("unused")
	private String privateField = "";

	public void publicMethod() {
		System.out.println("");
	}

	void defaultMethod() {
		System.out.println("");		
	}

	protected void protectedMethod() {
		System.out.println("");
	}

	@SuppressWarnings("unused")
	private void privateMethod() {
		System.out.println("");
	}

}
