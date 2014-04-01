package com.examples.with.different.packagename.stable;

public class StringUser {

	private final String myStr = "Hello World!";
	private final String emptyStr ="";
	private final String trueStr = "True";

	
	public StringUser() {
	}
	
	public boolean isEmptyShouldReturnFalse() {
		return myStr.isEmpty();
	}
	
	public boolean isEmptyShouldReturnTrue() {
		return emptyStr.isEmpty();
	}

	public boolean equalsShouldReturnFalse() {
		return myStr.equals(emptyStr);
	}

	public boolean equalsShouldReturnTrue() {
		return myStr.equals(myStr);
	}

	public boolean equalsIgnoreCaseShouldReturnFalse() {
		return myStr.equalsIgnoreCase(emptyStr);
	}

	public boolean equalsIgnoreCaseShouldReturnTrue() {
		return myStr.equalsIgnoreCase(myStr);
	}

	public boolean startsWithShouldReturnFalse() {
		return emptyStr.startsWith(myStr);
	}

	public boolean startsWithShouldReturnTrue() {
		return myStr.startsWith(emptyStr);
	}

	public boolean endsWithShouldReturnFalse() {
		return emptyStr.endsWith(myStr);
	}

	public boolean endsWithShouldReturnTrue() {
		return myStr.endsWith(emptyStr);
	}
	
	public boolean matchesShouldReturnTrue() {
		return trueStr.endsWith("[tT]rue");
	}

	public boolean matchesShouldReturnFalse() {
		return myStr.endsWith("[tT]rue");
	}

}
