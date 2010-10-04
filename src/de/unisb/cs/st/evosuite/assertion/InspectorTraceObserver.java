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


package de.unisb.cs.st.evosuite.assertion;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.testcase.ExecutionObserver;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.VariableReference;

public class InspectorTraceObserver extends ExecutionObserver {


	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(InspectorTraceObserver.class);
	
	private InspectorManager manager = InspectorManager.getInstance();
	
	private InspectorTrace trace = new InspectorTrace();
	
	@Override
	public void clear() {
		trace.clear();
	}

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void statement(int position, Scope scope, VariableReference retval) {
		if(retval == null)
			return;
		
		List<Inspector> inspectors = manager.getInspectors(retval.getVariableClass());
		if(inspectors.isEmpty()) {
			return;
		}
		if(scope.get(retval) == null)
			return;
		
		List<Object> result = new ArrayList<Object>();
		for(Inspector i : inspectors) {
			result.add(i.getValue(scope.get(retval)));
			//logger.info("New inspector result for variable of type "+retval.getClassName()+"/" + retval.getVariableClass().getName()+": "+i.getClassName()+"."+i.getMethodCall()+" -> "+i.getValue(scope.get(retval)));
		}
		
		trace.inspector_results.put(position, result);
		trace.return_values.put(position, retval);
	}

	public InspectorTrace getTrace() {
		return trace.clone();
	}
}
