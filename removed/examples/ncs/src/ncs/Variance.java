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
