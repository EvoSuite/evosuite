package de.unisb.cs.st.evosuite.testcase;

public class IF_CMP_Test {

	public void greaterEqual_IF_CMPLT(Integer val1, Integer val2) {
		if (val1 >= val2) {
			System.out.println(val1 + " >= " + val2);
			return;
		}
		System.out.println(val1 + " < " + val2);
	}

	public void greaterThan_IF_CMPLE(Integer val1, Integer val2) {
		if (val1 > val2) {
			System.out.println(val1 + " > " + val2);
			return;
		}
		System.out.println(val1 + " <= " + val2);
	}

	public void lesserEqual_IF_CMPGT(Integer val1, Integer val2) {
		if (val1 <= val2) {
			System.out.println(val1 + " <= " + val2);
			return;
		}
		System.out.println(val1 + " > " + val2);
	}

	public void lesserThan_IF_CMPGE(Integer val1, Integer val2) {
		if (val1 < val2) {
			System.out.println(val1 + " < " + val2);
			return;
		}
		System.out.println(val1 + " >= " + val2);
	}

}
