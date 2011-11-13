/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.bytecode;

import gov.nasa.jpf.JPF;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.bytecode.INVOKEVIRTUAL;

/**
 * @author krusev
 *
 */
public class InvVFunctionLogger {
	
	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.invokevitrual");
	
	//TODO well ... make it work!
	//#####################################################################################################			
	//This code is to check which String functions ware used in a class/project.
	//It writes all in a file and counts how often a function was called.
	//the file must be empty or have the appropriate format: String###<counter> e.g. equals(...)...###345
	public static void LogStringFnc(String file_name, INVOKEVIRTUAL ins){
		String cname = ins.getInvokedMethodClassName();
		String mname = ins.getInvokedMethodName();
		
		if (cname.startsWith("java.lang.String")) {
	//			
			Hashtable<String, Integer> str_func = new Hashtable<String, Integer>();
			
			try{

				FileReader input = new FileReader(file_name);
				BufferedReader reader = new BufferedReader(input);
				
				String st = "";
	            while ((st = reader.readLine()) != null) {
	                    String[] splited_Str = st.split("###");
	                    if ( splited_Str.length != 2 ) {
	                    	log.warning("String spliting for count of functions got terribly wrong!");
	                    }
	                    String key = splited_Str[0];
	                    int counter = Integer.parseInt(splited_Str[1]);
	                    str_func.put(key, counter);
	            }
				reader.close();
				
				
				FileWriter fstream = new FileWriter(file_name, false);
				BufferedWriter writer = new BufferedWriter(fstream);
				
				
				int cnt = 0;
				try {
					cnt = str_func.get(mname);
					str_func.put(mname, cnt+1);
				} catch (Exception e) {
					str_func.put(mname, 1);
				}
				
				Set<String> set_str = str_func.keySet();
				Iterator<String> it = set_str.iterator();
				while (it.hasNext()) {
				    String element = it.next();
				    writer.append(element + "###" + str_func.get(element) + "\n");
				}
	
				//Close the output stream
				writer.close();
				
				
			}catch (Exception e){//Catch exception if any
				log.warning("Error: " + e.getMessage());
			}
	
		}

	}
}
