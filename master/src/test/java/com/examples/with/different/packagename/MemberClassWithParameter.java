package com.examples.with.different.packagename;

public class MemberClassWithParameter {

	public class MemberClass {
		private Integer x;
		
		public MemberClass(Integer x) {
			this.x = x;
		}
		
		public Integer getX() {
			return x;
		}		
	}
	
	public boolean testMe(MemberClass x, Integer y) {
		if(x.getX().equals(y))
			return true;
		else
			return false;
	}
}
