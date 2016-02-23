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
package org.evosuite.idNaming;

import java.util.ArrayList;
import java.util.List;

public abstract class MethodArguments {
	protected int countArguments(String str){
		  int noOfArguments=0;
		//count arguments in a method goal
			if(str.contains(";")){
				//noOfArguments=StringUtils.countMatches(str, ";");
				String[] strParts= str.split(";");
				for(String token: strParts){
					if(token.contains("/")){
						noOfArguments++;
					}else{
		  				for (int i = token.length() - 1; i >= 0; i--) {
		  			        if (Character.isUpperCase(token.charAt(i))) {
		  			        	noOfArguments++;
		  			        }
		  			    }
					}
				}
			}else{
				for (int i = str.length() - 1; i >= 0; i--) {
			        if (Character.isUpperCase(str.charAt(i))) {
			        	noOfArguments++;
			        }
			    }
			}
			return noOfArguments;
			//end argument counting
	  }
	protected String getArgumentTypes(String str){	  
		//str=Lcom/examples/with/different/packagename/idnaming/gnu/trove/map/TShortShortMap;
	  List<String> argType = new ArrayList<String>();
	  String names="";
	  if(str.contains(";")){			
			String[] strParts= str.split(";");
			for(String token: strParts){
				if(token.contains("/")){
					argType.add(token.substring(token.lastIndexOf("/")+1,token.length()));
				}else{
	  				for (int i = 0; i <token.length() ; i++) {
	  			       switch(token.charAt(i)){
	  			        	case 'Z': argType.add("Boolean");
	  			        	break;
	  			        	case 'B': argType.add("Byte");
	  			        	break;
	  			        	case 'C': argType.add("Char");
	  			        	break;
	  			        	case 'S': argType.add("Short");
	  			        	break;
	  			        	case 'I': argType.add("Int");
	  			        	break;
	  			        	case 'J': argType.add("Long");
	  			        	break;
	  			        	case 'F': argType.add("Float");
	  			        	break;
	  			        	case 'D': argType.add("Double");
	  			        	break;
	  			        	case '[': argType.add("Array");
	  			        	break;
	  			        }
	  			    }
				}
			}
		}else{
			for (int i = 0; i <str.length() ; i++) {
				switch(str.charAt(i)){
			        	case 'Z': argType.add("Boolean");
			        	break;
			        	case 'B': argType.add("Byte");
			        	break;
			        	case 'C': argType.add("Char");
			        	break;
			        	case 'S': argType.add("Short");
			        	break;
			        	case 'I': argType.add("Int");
			        	break;
			        	case 'J': argType.add("Long");
			        	break;
			        	case 'F': argType.add("Float");
			        	break;
			        	case 'D': argType.add("Double");
			        	break;
			        	case '[': argType.add("Array");
			        	break;
			        	
		        }
		    }
		}
	  for(String s:argType){
		  names+=s;
	  }
		return names;
		//end argument counting
}
}
