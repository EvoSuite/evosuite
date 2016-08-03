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

	public static void main(String[] args) {
		{
			int int0 = 1;
			int int1 = 1;
			int int2 = 0;
			Flag.coverMe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 0;
			int int2 = 0;
			Flag.coverMe(int0, int1, int2);
		}
		{
			int int0 = 0;
			int int1 = 0;
			int int2 = 0;
			Flag.coverMe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 0;
			int int2 = 0;
			Flag.coverMe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 1;
			int int2 = 1;
			Flag.coverMe(int0, int1, int2);
		}
		{
			int int0 = 0;
			int int1 = 0;
			int int2 = 0;
			Flag.coverMe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 1;
			int int2 = 0;
			Flag.coverMe(int0, int1, int2);
		}
		{
			Flag flag0 = new Flag();
		}
		{
			int int0 = 1;
			int int1 = 1;
			int int2 = 1;
			Flag.coverMe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 1;
			int int2 = 1;
			Flag.coverMe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 1;
			int int2 = 1;
			Flag.coverMe(int0, int1, int2);
		}
		{
			int int0 = 2;
			int int1 = 1;
			int int2 = 1;
			Flag.coverMe(int0, int1, int2);
		}
	}
}
