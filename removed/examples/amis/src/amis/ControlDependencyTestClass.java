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
package amis;

/**
 * @author Andre Mis
 *
 */
public class ControlDependencyTestClass {

	int anInt = 0;
	
	public ControlDependencyTestClass() {
	}
	
	public void setAnInt(int anInt) {
		this.anInt = anInt;
	}
	
	public void simpleDoWhile() {
		do {
			anInt++;
		} while(anInt >= 0 && (anInt % 13) !=0 );
	}
	
	public int ifMethod(int anInt) {
		int i = anInt;
		if(i%2 == 0)
			i++;
		else
			i--;
		
		return i;
	}
	
	public int simpleSwitchMethod() {
		switch(anInt) {
			case 2: return 1;
			case 3: return 2;
			case 4: return 3;
			default: return -1;
		}
	}
	
	public int bigIfMethod(int anInt) {
		int i = anInt;
		if(i%17 == 0)
			if(i%2 == 0)
				i++;
			else
				i--;
		else
			if(i%2 == 0)
				i--;
			else
				i++;
		
		return i;
	}
	
	public int bigIfMethod2(int anInt) {
		int i = anInt;
		if(i%17 == 0)
			if(i%2 == 0)
				i++;
			else
				i--;
		else
			if(i%2 == 0)
				return i-1;
			else
				i++;
		
		return i;
	}
	
	public int bigIfMethod3(int anInt) {
		int i = anInt;
		if(i%17 == 0)
			if(i%2 == 0)
				i++;
			else
				i--;
		else
			if(i%2 == 0)
				i--;
			else
				return i+1;
		
		return i;
	}
	
	public int ifMethodReturn(int anInt) {
		int i = anInt;
		if(i%2 == 0)
			return i;
		else
			return i-1;
	}
	
	public int bigIfMethodReturn(int anInt) {
		int i = anInt;
		if(i%17 == 0)
			if(i%2 == 0)
				return i;
			else
				return i-1;
		else
			if(i%2 == 0)
				return i+1;
			else
				return i;		
	}
	
	public int whileMethod(int anInt) {
		int i = 0;
		int c = 0;
		while(i != (anInt % 10)) {
			i++;
			c++;
			if(c>5)
				break;
			if(c<5)
				continue;
		}
		return i;
	}

	
	public int whileMethodReturn(int anInt) {
		int i = 0;
		while(i<anInt) {
			i++;
			return i;
		}
		return -1;
	}

	public int whileMethodConditionalReturn(int anInt) {
		int i = 0;
		int c = 0;
		while(i % 10 != (anInt % 10)) {
			i++;
			if(anInt%2 == 0)
				return i;
			c++;
			if(c>5)
				break;
			if(c<5)
				continue;
		}
		return -1;
	}	
	
	public int doWhileMethod(int anInt) {
		int i = anInt;
		do {
			i++;
		} while(i % 13 != 0);
	
		return i;
	}
	
	public int doWhileMethodReturn(int anInt) {
		int i = anInt;
		do {
			i++;
			if(i % 13 == 1)
				return i;
		} while(i % 13 != 0);
	
		return i;
	}
	
	public int forMethod(int anInt) {
		int r = 0;
		for(int i=0;i<10 && anInt != 13;i++) {
			r = r+i+anInt;
		}
		
		return r;
	}
	
	public int forMethodReturn(int anInt) {
		for(int i=0;i<anInt;i++)
			return i;
		
		return anInt;
	}
}
