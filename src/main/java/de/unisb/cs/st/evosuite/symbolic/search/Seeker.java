/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.search;

import gov.nasa.jpf.JPF;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.Solver;
import de.unisb.cs.st.evosuite.symbolic.expr.BinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.Constraint;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;

import de.unisb.cs.st.evosuite.symbolic.expr.Cast;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.RealVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.StringComparison;
import de.unisb.cs.st.evosuite.symbolic.expr.StringMultipleComparison;
import de.unisb.cs.st.evosuite.symbolic.expr.StringVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.UnaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.Variable;

/**
 * @author krusev
 *
 */
public class Seeker implements Solver {
	
	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.search.Seeker");
	
	//TODO figure out what values should be given to these fields
	private int maxStepsForAll = 3;
	
	
	/* The idea here is to get the expressions and build the constraint 
	 * dynamically here using Java reflection. This should save us some time
	 * since we wan't do the evaluation of the constraints in JPF 
	 * 
	 * In getModel we need to build the constraints and search for better input 
	 * values for the String variables. 
	 */
	public Map<String, Object> getModel(Collection<Constraint<?>> constraints){
		HashMap<String, Object> result = new HashMap<String, Object>();
	
		//This actually does get a list every time the super class Solver is 
		// implemented using a collection so if we are going to throw away 
		// the other Solvers we might as well change this
		List<Constraint<?>> cnstr = null;
		if (constraints instanceof List<?>) {
			cnstr = (List<Constraint<?>>)constraints;
		} else {
			log.warning("Seeker got other type of collections!");
			return null;
		}
		
		//Get the target cnstr and the variables in it
		Constraint<?> target = cnstr.get(cnstr.size()-1);
		Set<Variable<?>> vars = getVariables(target);
		//remove the target from the constraints
		cnstr.remove(target);
		
		outerloop:
		for (int i = 0; i < maxStepsForAll ; i++ ) {
			for (Variable<?> var : vars) {
				
				Changer changer = new Changer();
				
				if (var instanceof StringVariable) {
					StringVariable strVar = (StringVariable) var;
					if (changer.strLocalSearch(strVar, target, cnstr, result)) {
						break outerloop;
					}
				}
				// These two are not yet implemented
				if (var instanceof IntegerVariable) {
					
					IntegerVariable intVar = (IntegerVariable) var;
					if (changer.intLocalSearch(intVar, target, cnstr, result)) {
						break outerloop;
					}
				}
				if (var instanceof RealVariable) {
					RealVariable realVar = (RealVariable) var;
					if (changer.realLocalSearch(realVar, target, cnstr, result)) {
						break outerloop;
					}
				}				
			}
		}
		
		
		return result;
	}
	
	
	

	
	/*
	//Old getModel. Please remove me!
	public Map<String, Object> getModel(Collection<Constraint<?>> constraints){
		HashMap<String, Object> result = new HashMap<String, Object>();

		
		List<Constraint<?>> cnstr = (List<Constraint<?>>)constraints;
		Constraint<?> target = cnstr.get(cnstr.size()-1);
		
		//remove the target from the constraints
		constraints.remove(target);
		
		boolean desCnstrValue; 
		switch (target.getComparator()) {
		case EQ:	//TODO what to do if we don't want to satisfy the condition? Just exit?
			desCnstrValue = false;
			break;
		case NE:	//this means we want to satisfy the condition
			desCnstrValue = true;
			break;
		case GE:	// We should't get any of this for Strings 
		case GT:	//TODO also handle integers
		case LE:
		case LT:
		default:
			throw new RuntimeException("Unsupported comparison: " + target.getComparator());
		}
		
		Expression<?> expr = target.getLeftOperand();
		
		if (!(expr instanceof StringComparison)) {
			return null;
		}
		StringComparison sc = (StringComparison) expr;
		
		// We don't need this functionality for now
		// If we don't need it at all delete "parent" stuff in Expression

		//setupTree(sc);
		
		Set<StringVariable> vars = getStringVariables(target);
		
		int fitness;// = DistanceEstimator.getFitness(sc);
		boolean reachable = true;
		
		for (int i = 0; i < maxStepsForAll ; i++ ) {
			for (Variable<?> var : vars) {
				fitness = DistanceEstimator.getFitness(sc);
				Changer changer = new Changer();
				String strVal = var.getMaxValue();
				
				for (int j = 0; j < maxStepsForEach ; j++ ) {
					String newVal = changer.changeVar(strVal, fitness, reachable);
					//assign working value in var.MinValue)
					var.setMinValue(newVal);
					reachable = DistanceEstimator.areReachable(constraints);
					//if unreachable values will be reverted automatically by changeVar()
					if (reachable) {
						int newFit = DistanceEstimator.getFitness(sc);
						if (newFit > fitness) {
							var.setMaxValue(newVal);
							result.put(var.getName() , newVal);
						}	
						if (newFit >= 0) {
							//we are ready 
							log.warning("we got: " + result);
							return result;
						} else {							
							fitness = newFit;
							strVal = newVal;
						}
					}
				}
			}
		}
	 	*/
	
	
	@SuppressWarnings("unused")
	private void setupTree(Expression<?> expr) {
		if (expr instanceof Variable<?>) {
			//done
		} else if (expr instanceof StringMultipleComparison){
			StringMultipleComparison smc = (StringMultipleComparison) expr;
			smc.getLeftOperand().setParent(expr);
			setupTree(smc.getLeftOperand());
			smc.getRightOperand().setParent(expr);
			setupTree(smc.getRightOperand());
			
			ArrayList<Expression<?>> ar_l_ex = smc.getOther();
			Iterator<Expression<?>> itr = ar_l_ex.iterator();
		    while (itr.hasNext()) {
		    	Expression<?> element = itr.next();
		    	element.setParent(expr);
		    	setupTree(element);
		    }
		} else if (expr instanceof StringComparison){
			StringComparison sc = (StringComparison) expr;
			sc.getLeftOperand().setParent(expr);
			setupTree(sc.getLeftOperand());
			sc.getRightOperand().setParent(expr);
			setupTree(sc.getRightOperand());
			
		} else if (expr instanceof BinaryExpression<?>) {
			BinaryExpression<?> bin = (BinaryExpression<?>) expr;
			bin.getLeftOperand().setParent(expr);
			setupTree(bin.getLeftOperand());
			bin.getRightOperand().setParent(expr);
			setupTree(bin.getRightOperand());

		} else if (expr instanceof UnaryExpression<?>) {
			UnaryExpression<?> un = (UnaryExpression<?>) expr;
			un.getOperand().setParent(expr);
			setupTree(un.getOperand());
			
		} else if (expr instanceof Constraint<?>) {
			// ignore

		}		
	}

	public boolean solve(Collection<Constraint<?>> constraints){
		return false;
	}

	/**
	 * Determine the set of variable referenced by this constraint
	 * 
	 * @param constraint
	 * @return
	 */
	private Set<Variable<?>> getVariables(Constraint<?> constraint) {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		getVariables(constraint.getLeftOperand(), variables);
		getVariables(constraint.getRightOperand(), variables);
		return variables;
	}

	/**
	 * Recursively determine constraints in expression
	 * 
	 * @param expr
	 * @param variables
	 */
	private void getVariables(Expression<?> expr, Set<Variable<?>> variables) {
		if (expr instanceof Variable<?>) {
			variables.add((Variable<?>) expr);
		} else if (expr instanceof StringMultipleComparison){
			StringMultipleComparison smc = (StringMultipleComparison) expr;
			getVariables(smc.getLeftOperand(), variables);
			getVariables(smc.getRightOperand(), variables);
			ArrayList<Expression<?>> ar_l_ex = smc.getOther();
			Iterator<Expression<?>> itr = ar_l_ex.iterator();
		    while (itr.hasNext()) {
		    	Expression<?> element = itr.next();
		    	getVariables(element, variables);
		    }
		} else if (expr instanceof StringComparison){
			StringComparison sc = (StringComparison) expr;
			getVariables(sc.getLeftOperand(), variables);
			getVariables(sc.getRightOperand(), variables);
		} else if (expr instanceof BinaryExpression<?>) {
			BinaryExpression<?> bin = (BinaryExpression<?>) expr;
			getVariables(bin.getLeftOperand(), variables);
			getVariables(bin.getRightOperand(), variables);
		} else if (expr instanceof UnaryExpression<?>) {
			UnaryExpression<?> un = (UnaryExpression<?>) expr;
			getVariables(un.getOperand(), variables);
		} else if (expr instanceof Cast<?>) {
			Cast<?> cst = (Cast<?>) expr;
			getVariables(cst.getConcreteObject(), variables);	
		} else if (expr instanceof Constraint<?>) {
			// ignore

		}
	}
	
}
