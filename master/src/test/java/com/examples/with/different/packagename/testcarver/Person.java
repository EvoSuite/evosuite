/**
 * 
 */
package com.examples.with.different.packagename.testcarver;

/**
 * @author Gordon Fraser
 * 
 */
public class Person extends Owner {

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	private final String firstName;

	private final String lastName;

	public Person(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}
}
