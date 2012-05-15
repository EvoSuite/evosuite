package de.unisb.cs.st.evosuite.utils;

public abstract class HashUtil {
	public static final int DEFAULT_PRIME = 31;
	
	public static final int hashCodeWithPrime(int prime, Object... components) {
		int result = 1;
		
		for (Object component : components) {
			result = prime * result + component.hashCode();
		}
		
		return result;
	}
	
	public static final int hashCodeWithPrime(int prime, int... componentHashCodes) {
		int result = 1;
		
		for (int componentHashCode : componentHashCodes) {
			result = prime * result + componentHashCode;
		}
		
		return result;
	}
	
	public static final int hashCode(Object... components) {
		return hashCodeWithPrime(DEFAULT_PRIME, components);
	}
/*	
	public static final int hashCode(int... componentHashCodes) {
		return hashCodeWithPrime(DEFAULT_PRIME, componentHashCodes);
	}
*/
}
