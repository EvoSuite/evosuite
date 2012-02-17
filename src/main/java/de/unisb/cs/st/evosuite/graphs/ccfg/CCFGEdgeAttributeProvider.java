package de.unisb.cs.st.evosuite.graphs.ccfg;

import java.util.Map;
import java.util.HashMap;

import org.jgrapht.ext.ComponentAttributeProvider;

public class CCFGEdgeAttributeProvider implements ComponentAttributeProvider<CCFGEdge> {

	
	@Override
	public Map<String, String> getComponentAttributes(CCFGEdge edge) {
		Map<String, String> r = new HashMap<String, String>();
		if(edge instanceof CCFGFrameEdge) {
			r.put("style", "dotted");
		} else if(edge instanceof CCFGMethodCallEdge) {
			r.put("style","bold");
		}
		return r;
	}

}
