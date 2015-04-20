package org.evosuite.regression;

import java.util.ArrayList;
import java.util.Collection;
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

	List<List<Object>> objectsPool = new ArrayList<List<Object>>();
	List<List<Object>> regressionObjectsPool = new ArrayList<List<Object>>();
	
	
	
	boolean isRegression = false;

	public List<List<Object>> getObjectsPool() {
		return objectsPool;
	}


	public List<List<Object>> getRegressionObjectsPool() {
		return regressionObjectsPool;
	}


	public boolean off = true;

	public void regressionFlag(boolean isRegression) {
		this.isRegression = isRegression;
	}

	@Override
	public void output(int position, String output) {
		// TODO Auto-generated method stub
		// logger.warn("output");
	}

	@Override
	public void beforeStatement(Statement statement, Scope scope) {
		// TODO Auto-generated method stub
		// logger.warn("before Statement");

	}

	public List<List<Map<Integer,Map<String, Map<String, Object>>>>> objectMapPool = new ArrayList<List<Map<Integer,Map<String, Map<String, Object>>>>>();
	public List<List<Map<Integer,Map<String, Map<String, Object>>>>> regressionObjectMapPool = new ArrayList<List<Map<Integer,Map<String, Map<String, Object>>>>>();
	
	public List<Map<Integer,Map<String, Map<String, Object>>>> currentObjectMapPool = new ArrayList<Map<Integer,Map<String, Map<String, Object>>>>();
	public List<Map<Integer,Map<String, Map<String, Object>>>> currentRegressionObjectMapPool = new ArrayList<Map<Integer,Map<String, Map<String, Object>>>>();
	
	
	@Override
	public void afterStatement(Statement statement, Scope scope,
			Throwable exception) {
		long startTime = System.nanoTime(); 
		
		// TODO Auto-generated method stub
		// logger.warn("after Statement");
		//Collection<Object> obs = scope.getObjects();
		//scope.getVariables().
		//List<Object> objectVars = new ArrayList<Object>();
		
		//List<Object> objectVars = new ArrayList<Object>();
		
		//Collection<VariableReference> variableReferences = scope.getVariables();
		
		ObjectFields ovars = new ObjectFields(scope);
		
		//logger.warn("overs: " + ovars.getObjectVariables());
		
		//for(VariableReference vr: statement.getVariableReferences())
		//logger.warn("isRegression: " + isRegression + " | vr:" + vr + " -- classloader: " + vr.getClass().getClassLoader());
		
		//for(Object o: obs)
		//	objectVars.add(objectVariables.getObjectVariables(o, o.getClass()));
		

		/*
		 * for (Object x : obs) { try {
		 * 
		 * if (x.getClass() != null) logger.warn(x.getClass().getName()); }
		 * catch (NullPointerException x1) {
		 * 
		 * } }
		 */
		if(!off) {
		if (isRegression)
			currentRegressionObjectMapPool.add(ovars.getObjectVariables());
		else
			currentObjectMapPool.add(ovars.getObjectVariables());
		}
		
		if(!off) {
		if (isRegression)
			a++;
		else
			b++;
		}
		RegressionSearchListener.odCollectionTime += System.nanoTime() - startTime;
	}
	
	public void requestNewPools(){
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
	
	public int a = 0;
	public int b = 0;

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		// logger.warn("cleared");
		
	}
	
	public void clearPools(){
		//objectsPool = new ArrayList<List<Object>>();
		//regressionObjectsPool = new ArrayList<List<Object>>();
		
		objectMapPool = new ArrayList<List<Map<Integer,Map<String, Map<String, Object>>>>>();
		regressionObjectMapPool = new ArrayList<List<Map<Integer,Map<String, Map<String, Object>>>>>();
		
	}


	@Override
	public void testExecutionFinished(ExecutionResult r) {
		// TODO Auto-generated method stub
		
	}

}
