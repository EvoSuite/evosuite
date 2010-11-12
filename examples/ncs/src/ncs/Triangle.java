package ncs;

public class Triangle
{
	public int exe(int a, int b, int c)  
	{
		if (a > b) 
		{ int tmp = a; a = b; b = tmp; }

		if (a > c) 
		{ int tmp = a; a = c; c = tmp; }

		if (b > c) 
		{ int tmp = b; b = c; c = tmp; }

		if(c >= a+b)
			return 1;
		else
		{
			if(a == b && b == c)
				return 4;
			else if(a == b  || b == c)
				return 3;
			else
				return 2;
		}
	}
	
	public static void main(String[] args) {
	  Triangle t = new Triangle();
	  t.exe(2,2,2);
	}
}