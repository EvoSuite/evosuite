/**
 * 
 */
package de.unisb.cs.st.evosuite.OUM;

import java.lang.reflect.AccessibleObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.unisb.cs.st.javalanche.ga.Randomness;

/**
 * @author Gordon Fraser
 *
 */
public class MethodUsage {
	
	private static Logger logger = Logger.getLogger(MethodUsage.class);
	
	private AccessibleObject call; 
	
	private class ParameterUsage {
		//List<ParameterInstance> instances;
		
		Map<AccessibleObject, Integer> usage = new HashMap<AccessibleObject, Integer>();
		
		int total = 0;
		
		public void add(AccessibleObject call) {
			if(!usage.containsKey(call))
				usage.put(call, 1);
			else
				usage.put(call, usage.get(call) + 1);
			
			total++;
		}
		
		public AccessibleObject getNextGenerator() {
			int index = Randomness.getInstance().nextInt(total);
			Iterator<Entry<AccessibleObject, Integer>> i = usage.entrySet().iterator();

	        while (i.hasNext()) {
	        	Entry<AccessibleObject, Integer> link = i.next();
	            int count = link.getValue();

	            if (index < count) {
	                return link.getKey();
	            }

	            index -= count;
	        }
	        
	        return null;
		}
		
		public boolean hasGenerator(AccessibleObject generator) {
			return usage.containsKey(generator);
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for(AccessibleObject o : usage.keySet()) {
				sb.append("      ");
				sb.append(o);
				sb.append(": ");
				sb.append(usage.get(o));
				sb.append("\n");
			}
			return sb.toString();
		}
	}
	
	private Map<Integer, ParameterUsage> parameter_usage = new HashMap<Integer, ParameterUsage>();
	
	public MethodUsage(AccessibleObject call) {
		this.call = call;
	}
	
	public void addUsage(int parameter, AccessibleObject call) {
		if(!parameter_usage.containsKey(parameter))
			parameter_usage.put(parameter, new ParameterUsage());
		
		ParameterUsage usage = parameter_usage.get(parameter);
		usage.add(call);
	}
	
	public boolean hasUsage(int parameter) {
		return parameter_usage.containsKey(parameter);
	}
	
	public boolean hasUsage(int parameter, AccessibleObject generator) {
		if(parameter_usage.containsKey(parameter)) {
			return parameter_usage.get(parameter).hasGenerator(generator);
		}
		
		return false;
	}
	
	public AccessibleObject getNextGenerator(int parameter) {
		return parameter_usage.get(parameter).getNextGenerator();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(Integer i : parameter_usage.keySet()) {
			sb.append("  "+call);
			sb.append(" "+i+": \n");
			sb.append(parameter_usage.get(i));
			sb.append("\n");
		}
		return sb.toString();
	}
}
