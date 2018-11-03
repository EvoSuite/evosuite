/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.rmi.service;

import org.evosuite.ga.Chromosome;
import org.evosuite.statistics.RuntimeVariable;

/**
 * Client Node view in the client process.
 * @author arcuri
 *
 */
public interface ClientNodeLocal {

	public boolean init();

	public void trackOutputVariable(RuntimeVariable variable, Object value);
	
    public void publishPermissionStatistics();

	public void changeState(ClientState state);

	public void changeState(ClientState state, ClientStateInformation information);

	public void updateStatistics(Chromosome individual);

	public void flushStatisticsForClassChange();

	public void updateProperty(String propertyName, Object value);

	public void waitUntilDone();
}
