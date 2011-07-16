/*
 * Copyright (C) 2011 Saarland University
 * 
 * This file is part of Javalanche.
 * 
 * Javalanche is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Javalanche is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * Javalanche. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.callgraph;

import java.util.HashSet;
import java.util.Set;

import de.unisb.cs.st.ds.util.io.XmlIo;
import de.unisb.cs.st.evosuite.Properties;

public class ConnectionData {

	private final Set<Tuple> connections = new HashSet<Tuple>();

	public void addConnection(String className1, String methodName1, String desc1,
	        String className2, String methodName2, String desc2) {
		MethodDescription m1 = new MethodDescription(className1, methodName1, desc1);
		MethodDescription m2 = new MethodDescription(className2, methodName2, desc2);
		connections.add(new Tuple(m1, m2));
	}

	public Set<MethodDescription> getAllMethods() {
		Set<MethodDescription> result = new HashSet<MethodDescription>();
		for (Tuple t : connections) {
			result.add(t.getStart());
			result.add(t.getEnd());
		}
		return result;
	}

	public boolean hasConnection(MethodDescription m1, MethodDescription m2) {
		return connections.contains(new Tuple(m1, m2));
	}

	public void save() {
		XmlIo.toXML(this, Properties.OUTPUT_DIR + "/" + Properties.CONNECTION_DATA);
	}

	public static ConnectionData read() {
		return XmlIo.get(Properties.OUTPUT_DIR + "/" + Properties.CONNECTION_DATA);
	}

	public Set<Tuple> getConnections() {
		return connections;
	}

}
