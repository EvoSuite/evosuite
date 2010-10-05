/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Type;

/**
 * Special case of VariableInstance pointing to null
 * @author Gordon Fraser
 *
 */
public class NullReference extends VariableReference {

	/**
	 * @param type
	 * @param position
	 */
	public NullReference(Type type) {
		super(type, -1);
	}

	/**
	 * Return name for source code representation
	 * @return
	 */
	public String getName() {
		return "("+type.getSimpleName()+") null";
	}
	
	/**
	 * Add delta to the position of all variables up to a position
	 * @param delta
	 *    The delta that will be added to the position of each variable
	 * @param position
	 *    The maximum position up to which variables are changed
	 */
	public void adjust(int delta, int position) {
		// Do nothing
	}

	/**
	 * Create a copy of the current variable
	 */
	public VariableReference clone() {
		return new NullReference(getType());
	}
}
