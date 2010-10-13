/**
 * 
 */
package de.unisb.cs.st.evosuite.OUM;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.unisb.cs.st.javalanche.ga.Randomness;

/**
 * @author Gordon Fraser
 *
 */
public class MethodUsage {
	
	@SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(MethodUsage.class);
	
	private ConcreteCall call; 
	
	private class ParameterUsage {
		//List<ParameterInstance> instances;
		
		Map<ConcreteCall, Integer> usage = new HashMap<ConcreteCall, Integer>();
		
		int total = 0;
		
		public void add(ConcreteCall call) {
			if(!usage.containsKey(call))
				usage.put(call, 1);
			else
				usage.put(call, usage.get(call) + 1);
			
			total++;
		}
		
		public ConcreteCall getNextGenerator() {
			int index = Randomness.getInstance().nextInt(total);
			Iterator<Entry<ConcreteCall, Integer>> i = usage.entrySet().iterator();

	        while (i.hasNext()) {
	        	Entry<ConcreteCall, Integer> link = i.next();
	            int count = link.getValue();

	            if (index < count) {
	                return link.getKey();
	            }

	            index -= count;
	        }
	        
	        return null;
		}
		
		public boolean hasGenerator(ConcreteCall generator) {
			return usage.containsKey(generator);
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for(ConcreteCall o : usage.keySet()) {
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
	
	public MethodUsage(ConcreteCall call) {
		this.call = call;
	}
	
	public void addUsage(int parameter, ConcreteCall call) {
		if(!parameter_usage.containsKey(parameter))
			parameter_usage.put(parameter, new ParameterUsage());
		
		ParameterUsage usage = parameter_usage.get(parameter);
		usage.add(call);
	}
	
	public boolean hasUsage(int parameter) {
		return parameter_usage.containsKey(parameter);
	}
	
	public boolean hasUsage(int parameter, ConcreteCall generator) {
		if(parameter_usage.containsKey(parameter)) {
			return parameter_usage.get(parameter).hasGenerator(generator);
		}
		
		return false;
	}
	
	public ConcreteCall getNextGenerator(int parameter) {
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
