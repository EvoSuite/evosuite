package ncs;
public class Variance
{
	//assuming v.length>0
	public double exe(int[] v)
	{
		
		//first calculate the mean
		double sum = 0;
		for(int i=0;i<v.length; i++)
			sum += v[i];
		double mean = sum/ (double)v.length;
		
		double var = 0; 
		for(int i=0; i<v.length; i++)
		{
			double dif = v[i] - mean;
			var += (dif*dif);
		}
		
		return var;
	}
}
