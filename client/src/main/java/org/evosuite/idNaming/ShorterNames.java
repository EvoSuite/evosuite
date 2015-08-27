/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.idNaming;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ShorterNames {
	protected String[] minimizePair(String name1, String name2, List<String> nameList){		
		List<String> list1 = new ArrayList(Arrays.asList(name1.split("_")));
		List<String> list2 = new ArrayList(Arrays.asList(name2.split("_")));
		List<String> union = new ArrayList<String>(list1);
		union.addAll(list2);
		List<String> intersection=null;
		// Prepare an intersection
		if(list1.size()>list2.size()){
			intersection = new ArrayList<String>(list2);	
			intersection.retainAll(list1);
		} else {
			intersection = new ArrayList<String>(list1);	
			intersection.retainAll(list2);
		}
		for(String str:intersection){
			if(list1.contains(str)){
				list1.remove(str);
			}
			if(list2.contains(str)){
				list2.remove(str);
			}			
		}
		String nameFirst = "test";
		String nameSecond = "test";
		if(list1.size()>0){
			for(String name: list1){
				nameFirst+="_"+name;
			}
		}else{
			nameFirst+="_"+name1.split("_")[1];
		}
		if(list2.size()>0){
			for(String name: list2){
				nameSecond+="_"+name;
			}
		}else{
			nameSecond+="_"+name2.split("_")[1];
		}
		// in case that after minimizing a name it became equal with another name in the list, do not minimize it
		for(String str: nameList){
			if(str.equals(nameFirst)){
				nameFirst = name1;
			}
			if(str.equals(nameSecond)){
				nameSecond = name2;
			}
		}
		String [] result= {nameFirst, nameSecond};
		return result;    			
	}
}
