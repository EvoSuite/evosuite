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
package org.evosuite.statistics.backend;

import java.util.Map;

import org.evosuite.ga.Chromosome;
import org.evosuite.statistics.OutputVariable;

/**
 * Simple dummy backend that just outputs all output variables to the console
 *  
 * @author gordon
 *
 */
public class ConsoleStatisticsBackend implements StatisticsBackend {

	@Override
	public void writeData(Chromosome result, Map<String, OutputVariable<?>> data) {
		for(OutputVariable<?> var : data.values()) {
			if (System.out!=null) {
				System.out.println(var.getName()+": "+var.getValue());
			}
		}

	}

}
