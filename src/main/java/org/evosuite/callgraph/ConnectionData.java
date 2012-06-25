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
 */
package org.evosuite.callgraph;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.utils.Utils;


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
		Utils.writeXML(this, Properties.OUTPUT_DIR + "/" + Properties.CONNECTION_DATA);
	}

	public void save(String fileName) {
		Utils.writeXML(this, fileName);
	}

	public static ConnectionData read() {
		return Utils.readXML(Properties.OUTPUT_DIR + "/" + Properties.CONNECTION_DATA);
	}

	public Set<Tuple> getConnections() {
		return connections;
	}

}
