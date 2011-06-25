package de.unisb.cs.st.evosuite.ui;

import java.util.*;

import de.unisb.cs.st.evosuite.utils.StringUtil;

public class GraphVizEnvironment {
	private int counter = 0;
	private Map<Object, String> idMap = new IdentityHashMap<Object, String>();
	
	public synchronized String getId(Object forObj) {
		if (!this.idMap.containsKey(forObj)) {
			this.idMap.put(forObj, String.format("node%d", counter++));
		}
		
		return this.idMap.get(forObj);
	}
	
	public String quoteString(String str) {
		return "\"" + StringUtil.escapeQuotes(str).replace("\n", "\\n") + "\"";
	}
}
