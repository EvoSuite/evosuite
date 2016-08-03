package com.examples.with.different.packagename.ncs;

public class Flag {

	public static int coverMe(int a, int b, int c) {
		int flag = 0;
		if (a <= 0) {
			return -1;
		}
		if (b <= 0) {
			return -1;
		}
		if (c <= 0) {
			return -1;
		}
		if (a == b) {
			flag = flag + 1;
		}
		if (a == c) {
			flag = flag + 2;
		}
		if (flag == 1) {
			if (c!=0) {
				return 0;
			}
		}
		return 1;
	}

}
