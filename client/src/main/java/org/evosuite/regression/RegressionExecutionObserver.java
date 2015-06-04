package org.evosuite.regression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegressionExecutionObserver extends ExecutionObserver {
	private static final Logger logger = LoggerFactory
			.getLogger(RegressionExecutionObserver.class);

	
	boolean isRegression = false;
	
	public boolean off = true;

	public void regressionFlag(boolean isRegression) {
		this.isRegression = isRegression;
	}

	
	public List<List<Map<Integer,Map<String, Map<String, Object>>>>> objectMapPool = new ArrayList<List<Map<Integer,Map<String, Map<String, Object>>>>>();
	public List<List<Map<Integer,Map<String, Map<String, Object>>>>> regressionObjectMapPool = new ArrayList<List<Map<Integer,Map<String, Map<String, Object>>>>>();
	
	public List<Map<Integer,Map<String, Map<String, Object>>>> currentObjectMapPool = new ArrayList<Map<Integer,Map<String, Map<String, Object>>>>();
	public List<Map<Integer,Map<String, Map<String, Object>>>> currentRegressionObjectMapPool = new ArrayList<Map<Integer,Map<String, Map<String, Object>>>>();
	
	
	@Override
	public void afterStatement(Statement statement, Scope scope,
			Throwable exception) {
		
	}

	public void requestNewPools() {
		currentObjectMapPool = new ArrayList<Map<Integer,Map<String, Map<String, Object>>>>();
		currentRegressionObjectMapPool = new ArrayList<Map<Integer,Map<String, Map<String, Object>>>>();
	}

	public void addToPools(){
		objectMapPool.add(currentObjectMapPool);
		regressionObjectMapPool.add(currentRegressionObjectMapPool);
	}
	
	public void addToPools(List<Map<Integer,Map<String, Map<String, Object>>>> currentObjectMapPool,List<Map<Integer,Map<String, Map<String, Object>>>> currentRegressionObjectMapPool){
		objectMapPool.add(currentObjectMapPool);
		regressionObjectMapPool.add(currentRegressionObjectMapPool);
	}

	public void clearPools() {
		objectMapPool = new ArrayList<List<Map<Integer,Map<String, Map<String, Object>>>>>();
		regressionObjectMapPool = new ArrayList<List<Map<Integer,Map<String, Map<String, Object>>>>>();
	}

	@Override
	public void testExecutionFinished(ExecutionResult r, Scope s) {
		long startTime = System.nanoTime();

		ObjectFields ovars = new ObjectFields(s);

		if (!off) {
			if (isRegression)
				currentRegressionObjectMapPool.add(ovars.getObjectVariables());
			else
				currentObjectMapPool.add(ovars.getObjectVariables());
		}

		RegressionSearchListener.odCollectionTime += System.nanoTime()
				- startTime;
	}

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeStatement(Statement statement, Scope scope) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {

	}

}
