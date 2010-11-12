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

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Collections;

/**
 * This class represents the state of a test case execution
 * 
 * @author Gordon Fraser
 *
 */
public class Scope {

	Map<VariableReference, Object> pool;
	
	/**
	 * Constructor
	 */
	public Scope() {
		pool = Collections.synchronizedMap(new HashMap<VariableReference, Object>());
	}
		
	/**
	 * Set variable to new value
	 * @param reference
	 *   VariableReference
	 * @param o
	 *   Value
	 */
	public synchronized void set(VariableReference reference, Object o) {
		
		// Learn some dynamic information about this object
		if(reference.isArray()) {
			if(o != null)
				reference.array_length = Array.getLength(o);
			else
				reference.array_length = 0;
		}
		
		if(o != null && !o.getClass().equals(reference.getVariableClass())) {
			reference.setType(o.getClass());
		}
		pool.put(reference, o);
	}

	/**
	 * Debug output
	 */
	public void printPool() {
		for(Entry<VariableReference, Object> entry : pool.entrySet()) {
			System.out.println("Pool: "+entry.getKey().statement+", "+entry.getKey().getType()+" : "+entry.getValue());
		}
	}
	
	/**
	 * Get current value of variable
	 * 
	 * @param reference
	 *   VariableReference we are looking for
	 * @return
	 *   Current value of reference
	 */
	public synchronized Object get(VariableReference reference) {
		if(reference.isArrayIndex()) {
			Object array = pool.get(reference.array);
			if(array != null) {
				return Array.get(array, reference.array_index);
			} else {
				return null;
			}
		} else
			return pool.get(reference);	
	}

	/**
	 * Get all elements in scope of type
	 * @param type
	 *   Class we are looking for
	 * @return
	 *   List of VariableReferences
	 */
	public List<VariableReference> getElements(Type type) {
		List<VariableReference> refs = new ArrayList<VariableReference>();
		for(Entry<VariableReference, Object> entry : pool.entrySet()) {
			if(type.equals(entry.getKey().getType()) ||
				(entry.getValue() != null && type.equals(entry.getValue().getClass())))
				refs.add(entry.getKey());
		}
/*
 		for(VariableReference ref : pool.keySet()) {
 
			// TODO: Exact match because it is used for comparison only at the moment
			if(ref.getType().equals(type))
				refs.add(ref);
		}
		*/
		return refs;
	}
}
