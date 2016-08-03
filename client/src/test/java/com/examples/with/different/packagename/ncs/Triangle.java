package com.examples.with.different.packagename.ncs;

public class Triangle {

	public enum TriangleType {
		INVALID, SCALENE, EQUILATERAL, ISOSCELES
	}

	public static TriangleType exe(int a, int b, int c) {
		int trian;
		if (a <= 0 || b <= 0 || c <= 0)
			return TriangleType.INVALID;
		trian = 0;
		if (a == b)
			trian = trian + 1;
		if (a == c)
			trian = trian + 2;
		if (b == c)
			trian = trian + 3;

		// if (trian == 0) {
		// if (a + b < c || a + c < b || b + c < a) {
		// return TriangleType.INVALID;
		// } else {
		// return TriangleType.SCALENE;
		// }
		// }
		//
		// if (trian > 3) {
		// return TriangleType.EQUILATERAL;
		// }

		if (trian == 1 && a + b > c) {
			return TriangleType.ISOSCELES;
		} else if (trian == 2 && a + c > b) {
			return TriangleType.ISOSCELES;
		} else if (trian == 3 && b + c > a) {
			return TriangleType.ISOSCELES;
		}

		return TriangleType.INVALID;
	}

	public static void main(String[] args) {

		{
			int int0 = 1;
			int int1 = 0;
			int int2 = 0;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 1;
			int int2 = 0;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 1;
			int int2 = 1;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 1;
			int int2 = 1;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 1;
			int int2 = 0;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 1;
			int int2 = 1;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 0;
			int int2 = 0;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 1;
			int int2 = 1;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 1;
			int int2 = 1;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 0;
			int int1 = 0;
			int int2 = 0;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 1;
			int int2 = 1;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 0;
			int int1 = 0;
			int int2 = 0;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 2;
			int int1 = 1;
			int int2 = 1;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 1;
			int int1 = 1;
			int int2 = 1;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 2;
			int int1 = 1;
			int int2 = 1;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 3;
			int int1 = 2;
			int int2 = 1;
			Triangle.exe(int0, int1, int2);
		}
		{
			int int0 = 3;
			int int1 = 2;
			int int2 = 2;
			Triangle.exe(int0, int1, int2);
		}

	}

}