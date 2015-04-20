package com.examples.with.different.packagename.stable;

public class HashCodeClassInit {

	static {
		int[] array = new int[10];
		for (int i =0;i<array.length;i++) {
			array[i]=new HashCodeClassInit().hashCode();
		}
	}


	public int getHashCode() {
		int hashCode = new HashCodeClassInit().hashCode();
		return hashCode;
	}

//	public int getHashCode2() {
//		int hashCode = new HashCodeClassInit().hashCode();
//		return hashCode;
//	}
//
//	public int getHashCode3() {
//		int hashCode = new HashCodeClassInit().hashCode();
//		return hashCode;
//	}

}
