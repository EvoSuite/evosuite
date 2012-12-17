package org.exsyst.util;

import java.util.IdentityHashMap;
import java.util.Map;

import org.evosuite.utils.StringUtil;

public class GraphVizEnvironment {
	private int counter = 0;
	private final Map<Object, String> idMap = new IdentityHashMap<Object, String>();

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
