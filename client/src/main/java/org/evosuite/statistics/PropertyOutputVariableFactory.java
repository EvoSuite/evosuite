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
package org.evosuite.statistics;

import org.evosuite.Properties;

/**
 * Output variable that represents a value stored in the properties
 * 
 * @author gordon
 *
 */
public class PropertyOutputVariableFactory {

	private String propertyName;
	
	public PropertyOutputVariableFactory(String propertyName) {
		this.propertyName = propertyName;
	}
	
	public OutputVariable<String> getVariable() {
		try {
			return new OutputVariable<String>(propertyName, Properties.getStringValue(propertyName));
		} catch (Exception e) {
			// TODO: What would be better?
			return new OutputVariable<String>(propertyName, "error");
		}
	}

}
