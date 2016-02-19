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