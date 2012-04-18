package de.unisb.cs.st.evosuite.graphs.ccfg;

import java.util.Map;
import java.util.HashMap;

import org.jgrapht.ext.ComponentAttributeProvider;

public class CCFGNodeAttributeProvider implements ComponentAttributeProvider<CCFGNode> {

	
	@Override
	public Map<String, String> getComponentAttributes(CCFGNode node) {
		Map<String, String> r = new HashMap<String, String>();
		if(node instanceof CCFGFrameNode) {
			r.put("shape", "diamond");
			r.put("style", "filled");
			r.put("fillcolor", "grey");
			r.put("fontcolor", "white");
			r.put("fontsize", "20");
		} else if(node instanceof CCFGMethodEntryNode) {
			r.put("style", "filled");
			r.put("shape", "triangle");
		} else if(node instanceof CCFGMethodExitNode) {
			r.put("style", "filled");
			r.put("shape", "invtriangle");
		} else if(node instanceof CCFGCodeNode) {
			String method = ((CCFGCodeNode)node).getCodeInstruction().getMethodName();
			String rgbColor = generateSaturatedColor(method);
			r.put("style", "filled");
			r.put("fillcolor", rgbColor);
			r.put("fontsize", "12");
			r.put("fontcolor", "white");
		} else if(node instanceof CCFGMethodCallNode) {
			r.put("shape", "box");
			r.put("style", "filled");
			r.put("fillcolor", "green");
		} else if(node instanceof CCFGMethodReturnNode) {
			r.put("shape", "box");
			r.put("style", "filled");
			r.put("fillcolor", "red");
		}
		return r;
	}

	private String generateSaturatedColor(Object obj) {
		
		float h = Math.abs(obj.hashCode()) / (float)Integer.MAX_VALUE;
//		return h+",0.75,0.65";
		return h+",0.85,0.55";
	}

}
