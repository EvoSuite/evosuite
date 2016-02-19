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
/*
 double fisher(m, n, x)
int m, n;
double x;
  {
  int a, b, i, j;
  double w, y, z, zk, d, p;
  a = 2*(m/2)-m+2;
  b = 2*(n/2)-n+2;
  w = (x*m)/n;
  z = 1.0/(1.0+w);
  if(a == 1)
    {
    if(b == 1)
      {
      p = sqrt(w);
      y = 0.3183098862;
      d = y*z/p;
      p = 2.0*y*atan(p);
      }
    else
      {
      p = sqrt(w*z);
      d = 0.5*p*z/w;
      }
    }
  else if(b == 1)
    {
    p = sqrt(z);
    d = 0.5*z*p;
    p = 1.0-p;
    }
  else
    {
    d = z*z;
    p = w*z;
    }
  y = 2.0*w/z;
  if(a == 1)
    for(j = b+2; j <= n; j += 2)
      {
      d *= (1.0+1.0/(j-2))*z;
      p += d*y/(j-1);
      }
  else
    {
    zk = pow(z, (double)((n-1)/2));
    d *= (zk*n)/b;
    p = p*zk+w*z*(zk-1.0)/(z-1.0);
    }
  y = w*z;
  z = 2.0/z;
  b = n-2;
  for(i = a+2; i <= m; i += 2)
    {
    j = i+b;
    d *= (y*j)/(i-2);
    p -= z*d/j;
    }
  return(p<0.0? 0.0: p>1.0? 1.0: p);
  } 
 */


public class Fisher
{
	public double exe(int m, int n, double x)
	{
		int a, b, i, j;
		double w, y, z, zk, d, p;
		
		a = 2*(m/2)-m+2;
		b = 2*(n/2)-n+2;
		w = (x*m)/n;
		z = 1.0/(1.0+w);
		
		if(a == 1)
		{
			if(b == 1)
			{
				p = Math.sqrt(w);
				y = 0.3183098862;
				d = y*z/p;
				p = 2.0*y*Math.atan(p);
			}
			else
			{
				p = Math.sqrt(w*z);
				d = 0.5*p*z/w;
			}
		}
		else if(b == 1)
		{
			p = Math.sqrt(z);
			d = 0.5*z*p;
			p = 1.0-p;
		}
		else
		{
			d = z*z;
			p = w*z;
		}
		
		y = 2.0*w/z;
		
		if(a == 1)
			for(j = b+2; j <= n; j += 2)
			{
				d *= (1.0+1.0/(j-2))*z;
				p += d*y/(j-1);
			}
		else
		{
			zk = Math.pow(z, (double)((n-1)/2));
			d *= (zk*n)/b;
			p = p*zk+w*z*(zk-1.0)/(z-1.0);
		}
		
		y = w*z;
		z = 2.0/z;
		b = n-2;
		for(i = a+2; i <= m; i += 2)
		{
			j = i+b;
			d *= (y*j)/(i-2);
			p -= z*d/j;
		}
		
		if(p<0.0)
			return 0.0;
		else if(p>1.0)
			return 1.0;
		else
			return p;
	}
}
