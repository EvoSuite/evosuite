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