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

import de.unisb.cs.st.evosuite.symbolic.expr.StringComparison;
import de.unisb.cs.st.evosuite.symbolic.expr.StringMultipleComparison;
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
	
	//TODO Should this be dependable on the length of the String?
	private int maxStepsForEach = 1000;
	
	
	/* The idea here is to get the expressions and build the constraint dynamically here
	 * using Java reflection. This should save us some time since we wan't do the evaluation 
	 * of the constraints in JPF 
	 * 
	 * In getModel we need to build the constraints and search for better input values for 
	 * the String variables. 
	 */
	
	public Map<String, Object> getModel(Collection<Constraint<?>> constraints){
		HashMap<String, Object> result = new HashMap<String, Object>();

		
		List<Constraint<?>> cnstr = (List<Constraint<?>>)constraints;
		Constraint<?> target = cnstr.get(cnstr.size()-1);
		
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
		log.warning("int.min: " + expr);
		if (!(expr instanceof StringComparison)) {
			return null;
		}
		StringComparison sc = (StringComparison) expr;
		
		setupTree(sc);
		
		Set<Variable<?>> vars = getVariables(target);
		
		int distance = DistanceEstimator.getDistance(sc);
		boolean reachable = true;
		
		for (int i = 0; i < maxStepsForAll ; i++ ) {
			for (Variable<?> var : vars) {
				Changer changer = new Changer();
				String strVal = var.getMinValue().toString();
				
				for (int j = 0; j < maxStepsForEach ; j++ ) {
					String newVal = changer.changeVar(strVal, distance, reachable);
					//TODO assign var the new value (in MaxValue)
					reachable = DistanceEstimator.areReachable(constraints);
					//if unreachable values will be reverted automatically by changeVar()
					if (reachable) {
						int newDist = DistanceEstimator.getDistance(sc);
						if (newDist >= 0) {
							//we are ready 
							
							//TODO save the new value for the var and return 
							
						} else {
							distance = newDist;
							strVal = newVal;
						}
					}
				}
			}
		}
		
		
		
		
		
		
	
		
		
		

		
//		for (Variable<?> var : vars) {
//			
//			if ( var.getParent() instanceof StringComparison ) {
//			
//				StringComparison parent = (StringComparison)var.getParent();
//				//Special case var.equals(constant) or constant.equals(var)
//				if ( (	parent.getOperator() == Operator.EQUALS 
//						|| parent.getOperator() == Operator.EQUALSIGNORECASE) 
//							&& (parent.getRightOperand() instanceof StringConstant
//								|| parent.getLeftOperand() instanceof StringConstant)) {
//					
//					boolean rightCnst = parent.getRightOperand() instanceof StringConstant;
//					StringConstant strCnst;
//					if ( rightCnst ) { 
//						strCnst = (StringConstant) parent.getRightOperand();
//					} else {
//						strCnst = (StringConstant) parent.getLeftOperand();
//					}
//					if (desCnstrValue && !var.getMinValue().equals(strCnst.getConcreteValue())) {
//						log.warning("test?!?");
//						result.put(var.getName() , strCnst.getConcreteValue());
//					} else {
//						//we should put something != strCnst.getConcreteValue()
//					}
//					//TODO think about a break here!
//				}
//				
//				
//				//TODO use reflection to 
//				
//				
//				
//			}
//		}
		
		return result;
	}
	
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
		} else if (expr instanceof Constraint<?>) {
			// ignore

		}
	}
	
}
