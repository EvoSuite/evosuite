package de.unisb.cs.st.evosuite.symbolic.search;

import gov.nasa.jpf.JPF;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerUnaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.RealVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.StringBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringComparison;
import de.unisb.cs.st.evosuite.symbolic.expr.StringVariable;

/**
 * @author krusev
 *
 */
public class Changer {

	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.search.Changer");

	private long oldDistLong = Long.MAX_VALUE;
	
	private long intBackUp;
	
	public Changer () {

	}
	
	private boolean longDistImpr(long newDistance) {
		return newDistance < oldDistLong;
	}
	
	private boolean longDistWrsn(long newDistance) {
		return newDistance > oldDistLong;
	}
	
	private void backup(IntegerVariable intVar, long integerDist) {
		oldDistLong = integerDist;
		intBackUp = intVar.execute();		
	}
	
	private void backup(StringVariable var, long newDist) {
		var.setMaxValue(var.getMinValue());
		oldDistLong = newDist;
	}
	
	private void restore(IntegerVariable intVar) {		
		intVar.setConcreteValue(intBackUp);
	}
	
	private void restore(StringVariable var) {		
		var.setMinValue(var.getMaxValue());
	}
	
	private void set(IntegerVariable intVar, long val) {
		intVar.setConcreteValue(val);
	}
	
	private long getIntDistForVal(Constraint<?> target, 
										IntegerVariable intVar, long val) {
		long result;
		long backUp = intVar.getConcreteValue();
		intVar.setConcreteValue(val);
		result = DistanceEstimator.getIntegerDist(target, intVar);
		intVar.setConcreteValue(backUp);
		return result;		
	}
	
	//TODO fix for other expressions that land here e.g. RealExpression
	public boolean strLocalSearch(StringVariable strVar, Constraint<?> target,
			List<Constraint<?>> cnstr, HashMap<String, Object> varsToChange) {
		
		// try to remove each

		backup(strVar, DistanceEstimator.getStringDist(target));
		
		for (int i = strVar.execute().length() - 1; i >= 0 ; i--) {
			String newStr = 	strVar.execute().substring(0, i) 
							+ 	strVar.execute().substring(i + 1);
			strVar.setMinValue(newStr);
			
			long newDist = DistanceEstimator.getStringDist(target);
			boolean reachable = DistanceEstimator.areReachableStr(cnstr);

			if (longDistImpr(newDist) && reachable) {
				varsToChange.put(strVar.getName(), newStr);
				if (newDist == 0 && reachable) {
					return true;
				}
				backup(strVar, newDist);
			} else {
				restore(strVar);
			}
		}

		
		// try to replace each 
		
		backup(strVar, DistanceEstimator.getStringDist(target));

		spatialLoop:
		for (int i = 0; i < strVar.execute().length(); i++) {
			char oldChar = strVar.execute().charAt(i);
			char[] characters = strVar.execute().toCharArray();
			for (char replacement = 0; replacement < 128; replacement++) {
				if (replacement != oldChar) {
					characters[i] = replacement;
					String newStr = new String(characters);
					strVar.setMinValue(newStr);

					long newDist = DistanceEstimator.getStringDist(target);
					boolean reachable = DistanceEstimator.areReachableStr(cnstr);
					if (longDistImpr(newDist) && reachable) {
						varsToChange.put(strVar.getName(), newStr);
						if (newDist == 0 && reachable) {
							return true;
						}
						backup(strVar, newDist);
						break;
					} else {
						restore(strVar);						
					}
					if (longDistWrsn(newDist)) {
						//skip this place
						continue spatialLoop;
					}
				}
			}
		}
		
		// try to add everywhere
		
		backup(strVar, DistanceEstimator.getStringDist(target));

		for (int i = 0; i < strVar.execute().length() + 1; i++) {
			boolean add = true;
			while (add) {
				add = false;
				for (char replacement = 0; replacement < 128; replacement++) {
					String newStr = strVar.execute().substring(0, i) + replacement + strVar.execute().substring(i);
					strVar.setMinValue(newStr);

					long newDist = DistanceEstimator.getStringDist(target);
					boolean reachable = DistanceEstimator.areReachableStr(cnstr);

					if (longDistImpr(newDist) && reachable) {
						varsToChange.put(strVar.getName(), newStr);
						if (newDist <= 0 && reachable) {
							return true;
						}
						backup(strVar, newDist);
						add = true;
						break;
					} else {
						restore(strVar);
					}
				}
			}
		}
		return false;
	} 

	/* TODO
	 * Fix this
	 * 
			 krusev@asya:~/workspace/evosuite/examples$ ../EvoSuite -generateSuite -class scs.Stemmer -Ddse_rate=5
		* Generating tests for class scs.Stemmer
		* Test criterion: Branch coverage
		* Setting up search algorithm for whole suite generation
		* Total number of test goals: 344
		* Starting evolution
		[Progress:>                             2%] [Cov:=========>                         28%]Exception in thread "main" java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.Character
			at de.unisb.cs.st.evosuite.testcase.CharPrimitiveStatement.delta(CharPrimitiveStatement.java:61)
			at de.unisb.cs.st.evosuite.testcase.PrimitiveStatement.mutate(PrimitiveStatement.java:286)
			at de.unisb.cs.st.evosuite.testcase.TestChromosome.mutationChange(TestChromosome.java:406)
			at de.unisb.cs.st.evosuite.testcase.TestChromosome.mutate(TestChromosome.java:261)
			at de.unisb.cs.st.evosuite.testsuite.AbstractTestSuiteChromosome.mutate(AbstractTestSuiteChromosome.java:124)
			at de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome.mutate(TestSuiteChromosome.java:78)
			at de.unisb.cs.st.evosuite.ga.SteadyStateGA.evolve(SteadyStateGA.java:100)
			at de.unisb.cs.st.evosuite.ga.SteadyStateGA.generateSolution(SteadyStateGA.java:171)
			at de.unisb.cs.st.evosuite.TestSuiteGenerator.generateWholeSuite(TestSuiteGenerator.java:389)
			at de.unisb.cs.st.evosuite.TestSuiteGenerator.generateTests(TestSuiteGenerator.java:228)
			at de.unisb.cs.st.evosuite.TestSuiteGenerator.generateTestSuite(TestSuiteGenerator.java:181)
			at de.unisb.cs.st.evosuite.TestSuiteGenerator.main(TestSuiteGenerator.java:1208)
	 */
	
	//TODO fix reachability
	public boolean intLocalSearch(IntegerVariable intVar, Constraint<?> target,
			List<Constraint<?>> cnstr, HashMap<String, Object> varsToChange) {
		
		Expression<?> varExpr;
		if (DistanceEstimator.exprContainsVar(target.getLeftOperand(),  intVar)) {
			varExpr = target.getLeftOperand();
		} else {
			varExpr = target.getRightOperand();
		}
		
		if (varExpr instanceof StringComparison ) {
			//We are here if we have some int variable concatenated to a string
			return srch4IntApp2String(intVar, target, cnstr, varsToChange);
		} else if (varExpr instanceof StringBinaryExpression ) {
			//TODO implement int var search in StringBinaryExpression
			//We are here if we have an int var that is part of some string operator
			return false;
		} else if (	varExpr instanceof IntegerBinaryExpression
				||	varExpr instanceof IntegerUnaryExpression 
				||	varExpr instanceof IntegerVariable) {
			
			long upBnd = intVar.getMaxValue();
			long distUp = getIntDistForVal(target, intVar, upBnd);
			if (distUp == 0) {
				varsToChange.put(intVar.getName(), upBnd);
				return true;
			}		
			
			long lwBnd = intVar.getMinValue();
			long distLw = getIntDistForVal(target, intVar, lwBnd);
			if (distLw == 0) {
				varsToChange.put(intVar.getName(), lwBnd);
				return true;
			}	
			
			//backup(intVar, DistanceEstimator.getIntegerDist(target, intVar));
			
			//TODO check reachability
			while (upBnd != lwBnd) {
				long mid = (upBnd + lwBnd)/2;
				long distMid = getIntDistForVal(target, intVar, mid);
//				log.warning("lw: " + lwBnd + " LwDist: " + distLw);
//				log.warning("mid: " + mid + " MidDist: " + distMid);
//				log.warning("mid: " + upBnd + " UpDist: " + distUp + "\n");
				if (distMid == 0) {
					varsToChange.put(intVar.getName(), mid);
					return true;
				}
				
				if (distLw < distMid && distMid < 0) {
					distLw = distMid;
					lwBnd = mid;

				} else if (0 < distMid && distMid < distUp) {
					distUp = distMid;
					upBnd = mid;

				} else {
					log.warning("intLocalSearch: Something funny happend in the bin search " +
								"and since no one likes divergation we broke the loop." );
					break;
				}
			}
			return false;
		} else {
			log.warning("intLocalSearch: got an unsupported expression: " + target );
			return false;
		}
	}

	/**
	 * 
	 * @param intVar: the integer variable that should be altered
	 * @param target: the target constraint that should be reached 
	 * @param cnstr: list of constraints that should stay reachable
	 * @param varsToChange HashMap containing the variables that should be changed
	 * @return true if we have found a value that satisfies the target constraint
	 */
	private boolean srch4IntApp2String(IntegerVariable intVar,
			Constraint<?> target, List<Constraint<?>> cnstr,
			HashMap<String, Object> varsToChange) {

		// try to remove each

		backup(intVar, DistanceEstimator.getStringDist(target));
		

		for (int i = iV2S(intVar).length() - 1; i > 0 ; i--) {
			String newStr = iV2S(intVar).substring(0, i) 
							+ iV2S(intVar).substring(i + 1);
			long newVal = Long.parseLong(newStr);
			set(intVar, newVal);

			long newDist = DistanceEstimator.getStringDist(target);
			boolean reachable = DistanceEstimator.areReachableStr(cnstr);

			if (longDistImpr(newDist) && reachable) {
				varsToChange.put(intVar.getName(), newVal);
				if (newDist == 0 && reachable) {
					return true;
				}
				backup(intVar, newDist);
			} else {
				restore(intVar);
			}
		}

		
		// try to replace each 
		
		backup(intVar, DistanceEstimator.getStringDist(target));

		spatialLoop:
		for (int i = 0; i < iV2S(intVar).length(); i++) {
			char oldChar = iV2S(intVar).charAt(i);
			char[] characters = iV2S(intVar).toCharArray();
			for (int offset = 0; offset < 9; offset++) {
				char replacement = (char) ('0' + offset);
				if (replacement != oldChar) {
					characters[i] = replacement;
					String newStr = new String(characters);
					long newVal = Long.parseLong(newStr);
					set(intVar, newVal);

					long newDist = DistanceEstimator.getStringDist(target);
					boolean reachable = DistanceEstimator.areReachableStr(cnstr);
					if (longDistImpr(newDist) && reachable) {
						varsToChange.put(intVar.getName(), newVal);
						if (newDist == 0 && reachable) {
							return true;
						}
						backup(intVar, newDist);
						break;
					} else {
						restore(intVar);						
					}
					if (longDistWrsn(newDist)) {
						//skip this place
						continue spatialLoop;
					}
				}
			}
		}
		
		// try to add everywhere
		
		backup(intVar, DistanceEstimator.getStringDist(target));

		for (int i = 0; i < iV2S(intVar).length() + 1; i++) {
			boolean add = true;
			while (add) {
				add = false;
				for (int offset = 0; offset < 9; offset++) {
					char replacement = (char) ('0' + offset);
					String newStr = iV2S(intVar).substring(0, i) + replacement + iV2S(intVar).substring(i);
					long newVal = Long.parseLong(newStr);
					set(intVar, newVal);

					long newDist = DistanceEstimator.getStringDist(target);
					boolean reachable = DistanceEstimator.areReachableStr(cnstr);
					if (longDistImpr(newDist) && reachable) {
						varsToChange.put(intVar.getName(), newVal);
						if (newDist == 0 && reachable) {
							return true;
						}
						backup(intVar, newDist);
						add = true;
						break;
					} else {
						restore(intVar);
					}
				}
			}
		}
		
		return false;

	}

	/**
	 * 
	 * @param intVar
	 * @return String representation of the value of the integer variable
	 */
	private String iV2S(IntegerVariable intVar) {
		return Long.toString(intVar.execute());
	}

	public boolean realLocalSearch(RealVariable realVar, Constraint<?> target,
			List<Constraint<?>> cnstr, HashMap<String, Object> varsToChange) {
		// TODO Auto-generated method stub
		
		return false;
	} 

}
