package ncs;
public class Triangle2
{
	public int exe(int a, int b, int c)  
	{
		if(a<=0 || b<=0 || c<=0)
			return 1;// was 4
		
		int tmp = 0;
		
		if(a==b)
			tmp = tmp + 1;
		
		if(a==c)
			tmp = tmp + 2;
		
		if(b==c)
			tmp = tmp + 3;
		
		if(tmp == 0)
		{
			if((a+b<=c) || (b+c <=a) || (a+c<=b))
				tmp = 1; //was 4
			else
				tmp = 2; //was 1
			return tmp;
		}
		
		if(tmp > 3)
			tmp = 4;// was 3;
		else if(tmp==1 && (a+b>c))
			tmp = 3; // was 2
		else if(tmp==2 && (a+c>b))
			tmp = 3; // was 2
		else if(tmp==3 && (b+c>a))
			tmp = 3; // was 2
		else
			tmp = 1; // was 4
		
		return tmp;
	}
}