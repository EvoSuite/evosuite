package com.examples.with.different.packagename.stable;

import java.util.HashMap;
import java.util.Map;

public class MapContainerUser {

	private final Map<Object, Object> myMap = new HashMap<Object, Object>();

	private final Map<Object, Object> emptyMap = new HashMap<Object, Object>();

	private final Object myKey;

	private final Object myValue;

	private final Object myOtherKey;

	private final Object myOtherValue;

	public MapContainerUser() {
		myKey = new Object();
		myValue = new Object();
		myMap.put(myKey, myValue);
		myOtherKey = new Object();
		myOtherValue = new Object();
	}

	public boolean containsKeyShouldReturnTrue() {
		return myMap.containsKey(myKey);
	}

	public boolean containsValueShouldReturnTrue() {
		return myMap.containsValue(myValue);
	}
	
	public boolean containsKeyOnEmptyShouldReturnFalse() {
		return emptyMap.containsKey(myKey);
	}

	public boolean containsValueOnEmptyShouldReturnFalse() {
		return emptyMap.containsValue(myValue);
	}

	public boolean containsValueOnNonEmptyShouldReturnFalse() {
		return myMap.containsValue(myOtherValue);
	}

	public boolean containsKeyOnNonEmptyShouldReturnFalse() {
		return myMap.containsKey(myOtherKey);
	}

	public boolean isEmptyShouldReturnFalse() {
		return myMap.isEmpty();
	}

	public boolean isEmptyShouldReturnTrue() {
		return emptyMap.isEmpty();
	}

}
