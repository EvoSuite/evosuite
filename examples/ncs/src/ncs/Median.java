package ncs;
public class Median
{
	public int exe(int[] v)
	{
		int[] a = new int[v.length];
		for(int i=0;i<v.length; i++)
			a[i] = v[i];
			
		for(int i = 0;  i < a.length; i++)
			for(int j=0; j < a.length - 1; j++)
				if(a[j] > a[j+1])
				{
					int k = a[j];
					a[j] = a[j+1];
					a[j+1] = k;
				}
		
		return a[a.length/2];
	}
}