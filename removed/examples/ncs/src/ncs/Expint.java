/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package ncs;

public class Expint 
{
	private static final double MAXIT = 100;
	private static final double EULER = 0.5772156649;
	private static final double FPMIN = 1.0e-30;
	private static final double EPS = 1.0e-7;

	public double exe(int n, double x) 
	{
		int i,ii,nm1;
		double a,b,c,d,del,fact,h,psi,ans;

		nm1=n-1;
		
		if (n < 0 || x < 0.0 || (x==0.0 && (n==0 || n==1)))
			throw new RuntimeException("error: n < 0 or x < 0");
		else 
		{
			if (n == 0) 
				ans = Math.exp(-x)/x;
			else 
			{
				if (x == 0.0) 
					ans=1.0/nm1;
				else 
				{
					if (x > 1.0) 
					{
						b=x+n;
						c=1.0/FPMIN;
						d=1.0/b;
						h=d;
						
						for (i=1;i<=MAXIT;i++) 
						{
							a = -i*(nm1+i);
							b += 2.0;
							d=1.0/(a*d+b);
							c=b+a/c;
							del=c*d;
							h *= del;
							
							if (Math.abs(del-1.0) < EPS) 
							{
								return h*Math.exp(-x);
							}
						}
						
						throw new RuntimeException("continued fraction failed in expint");	
					}
					
					else 
					{
						ans = (nm1!=0 ? 1.0/nm1 : -Math.log(x)-EULER);
						fact=1.0;
						
						for (i=1;i<=MAXIT;i++) {
							fact *= -x/i;
							
							if (i != nm1) 
								del = -fact/(i-nm1);
							else 
							{
								psi = -EULER;
								
								for (ii=1;ii<=nm1;ii++) 
									psi += 1.0/ii;
								
								del = fact*(-Math.log(x)+psi);
							}
							
							ans += del;
							
							if (Math.abs(del) < Math.abs(ans)*EPS) 
							{
								return ans;
							}
						}
						throw new RuntimeException("series failed in expint");
					}
				}
			}
		}
		return ans;
	}
}

