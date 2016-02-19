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
}