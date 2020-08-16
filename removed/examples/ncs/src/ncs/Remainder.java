/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
public class Remainder
{
	public int exe(int a, int b)
	{
		int r = 0-1;
		int cy = 0;
		int ny = 0;

		if (a==0);
		else
			if (b==0);
			else
				if (a>0)
					if (b>0)
						while((a-ny)>=b)
						{
							ny=ny+b;
							r=a-ny;
							cy=cy+1;
						}
					else	// b<0
						//while((a+ny)>=Math.abs(b))
						while((a+ny)>= ((b>=0) ? b : -b))
						{
							ny=ny+b;
							r=a+ny;
							cy=cy-1;
						}
				else	// a<0
					if (b>0)
						//while(Math.abs(a+ny)>=b)
						while( ((a+ny)>=0 ? (a+ny) : -(a+ny))   >=b)
						{
							ny=ny+b;
							r=a+ny;
							cy=cy-1;
						}
					else
						while(b>=(a-ny))
						{
							ny=ny+b;
							//r=Math.abs(a-ny);
							r= ((a-ny)>=0 ? (a-ny) : -(a-ny));
							cy=cy+1;
						}
		return r;
	}
}