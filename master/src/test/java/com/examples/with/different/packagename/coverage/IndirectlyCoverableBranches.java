package com.examples.with.different.packagename.coverage;

public class IndirectlyCoverableBranches {

	private String str1 = "suf";
	private String str2 = "fix";
	
	public void someTopLevelMethod() {
		testMe(42, str1 + str2);
		testMe(42, "bla");
		testMe(40, "foo");
	}
	
	public void otherTopLevelMethod(int x) {
		testMe(x, str1 + str2);
	}
	
	public boolean testMe(int x, String foo) {
		if(x == 42 && foo.endsWith(str1 + str2))
			return true;
		else
			return false;
	}
}
