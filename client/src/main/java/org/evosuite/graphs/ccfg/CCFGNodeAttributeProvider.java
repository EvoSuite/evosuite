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
/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Gordon Fraser
 */
package org.evosuite.graphs.ccfg;

import java.util.Map;
import java.util.HashMap;

import org.jgrapht.ext.ComponentAttributeProvider;
public class CCFGNodeAttributeProvider implements ComponentAttributeProvider<CCFGNode> {

	
	/** {@inheritDoc} */
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
		} else if(node instanceof CCFGFieldClassCallNode) {
			String method = ((CCFGFieldClassCallNode)node).getCodeInstruction().getMethodName();
			String rgbColor = generateBColor(method);
			r.put("style", "filled");
			r.put("fillcolor", rgbColor);
			r.put("fontsize", "12");
			r.put("fontcolor", "white");
			r.put("shape", "hexagon");
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
		
		float h = Math.abs(obj.hashCode() / (float)Integer.MAX_VALUE);
//		return h+",0.75,0.65";
		return h+",0.85,0.55";
	}
	
	private String generateBColor(Object obj) {
		
		float h = Math.abs(obj.hashCode() / (float)Integer.MAX_VALUE);
//		return h+",0.75,0.65";
		return h+",0.85,0.95";
	}

}
