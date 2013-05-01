/**
 * 
 */
package com.examples.with.different.packagename.test;

/**
 * @author fraser
 * 
 */
public class ExampleComplexReturnClass {

	private final ExampleObserverClass value;

	public ExampleComplexReturnClass(ExampleObserverClass value) {
		this.value = value;
	}

	public void add(ExampleObserverClass value) {
		this.value.setMember(this.value.getMember() + value.getMember());
	}

	public ExampleObserverClass get() {
		return value;
	}

}
