package com.examples.with.different.packagename.purity;

public class JdkPureInspector {

	private final Character character;
	private final char char_value;
	
	public JdkPureInspector(char c) {
		character = new Character(c);
		char_value = c;
	}
	
	public boolean equalsToZ() {
		return character.charValue()=='z';
	}
	
	public boolean isLowerCase() {
		return Character.isLowerCase(char_value);
	}
	
}
