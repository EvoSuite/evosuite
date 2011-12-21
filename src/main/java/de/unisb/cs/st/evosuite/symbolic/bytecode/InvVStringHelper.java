/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.bytecode;

import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.bytecode.Instruction;

import java.util.ArrayList;

import de.unisb.cs.st.evosuite.javaagent.BooleanHelper;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.Operator;
import de.unisb.cs.st.evosuite.symbolic.expr.StringBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringComparison;
import de.unisb.cs.st.evosuite.symbolic.expr.StringExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringMultipleComparison;

/**
 * @author krusev
 *
 */
public abstract class InvVStringHelper {
	

	public static Instruction str_fnc_substring(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		Expression<?> se0 =  (Expression<?>) sf.getOperandAttr(0);
		StringExpression se1 = (StringExpression) sf.getOperandAttr(1);
	
		//get the Strings using the positions from the stack. Order is crucial here
		int offset = sf.pop();
		String firstStr = ks.heap.get(sf.pop()).asString();

		if (se0 == null) {
			se0 = new IntegerConstant(offset);
		}
		
		//TODO offset could be out of range 
		//substring throws an exception
		// ask Gordon how EvoSuite could handle this
		
		
		//compute the resulting value and push it on the real stack
		String result = firstStr.substring(offset);
		int pointer = ks.heap.newString(result, ti); 
		sf.push(pointer, true);
		
		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(
			new StringBinaryExpression(se1, Operator.SUBSTRING, se0, result));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}

	public static Instruction str_fnc_equalsIgnoreCase(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		
		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression se0 = (StringExpression) sf.getOperandAttr(0);
		StringExpression se1 = (StringExpression) sf.getOperandAttr(1);
	
		//get the Strings using the positions from the stack. Order is crucial here
		String secondStr = ks.heap.get(sf.pop()).asString();
		String firstStr = ks.heap.get(sf.pop()).asString();

		//compute the resulting value and push it on the real stack
		int result = BooleanHelper.StringEqualsIgnoreCase(firstStr, secondStr);
		sf.push(result);

		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(
			new StringComparison(se1, Operator.EQUALSIGNORECASE, se0, (long)result));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}

	public static Instruction str_fnc_equals(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		
		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression se0 = (StringExpression) sf.getOperandAttr(0);
		StringExpression se1 = (StringExpression) sf.getOperandAttr(1);
	
		//get the Strings using the positions from the stack. Order is crucial here
		String secondStr = ks.heap.get(sf.pop()).asString();
		String firstStr = ks.heap.get(sf.pop()).asString();
		
		//compute the resulting value and push it on the real stack
		int result = BooleanHelper.StringEquals(firstStr, secondStr);
		sf.push(result);

		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(
			new StringComparison(se1, Operator.EQUALS, se0, (long)result));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}
	
	@SuppressWarnings("unchecked")
	public static Instruction str_fnc_startsWith(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		
		/*
		 * Here we have two cases:
		 * 	1. with offset: on the stack we have:
		 * 
		 * 		string ..... first string
		 * 		string ..... second string
		 * 		int ........ offset
		 * 
		 * 	2. normal (no offset):
		 * 
		 * 		string ..... first string
		 * 		string ..... second string
		 */
		boolean case_one = ins.getInvokedMethodSignature().equals("(Ljava/lang/String;I)Z");
		
		//we are offsetting the last stack position if we are in case_one
		int sf_offs_int = case_one ? 1 : 0;
		
		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		Expression<Long> offs_expr;
		if (case_one) {
			offs_expr = (Expression<Long>) sf.getOperandAttr(0);
		} else {
			offs_expr = new IntegerConstant(-1);
		}
		StringExpression se0 = (StringExpression) sf.getOperandAttr(0 + sf_offs_int);
		StringExpression se1 = (StringExpression) sf.getOperandAttr(1 + sf_offs_int);

		//get the Strings using the positions from the stack. Order is crucial here
		int offset = 0;
		if (case_one) {
			offset = sf.pop();
		}		
		String secondStr = ks.heap.get(sf.pop()).asString();
		String firstStr = ks.heap.get(sf.pop()).asString();
		
		//if we don't have an expression for the offset in this function we create them 
		if (offs_expr == null) {
			offs_expr = new IntegerConstant(offset);
		}
		
		//compute the resulting value and push it on the real stack
		//TODO this could throw an exception
		int result = BooleanHelper.StringStartsWith(firstStr, secondStr, offset);
		sf.push(result);
		
		//push a StringComparation expression on the fake stack
		ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
		other.add(offs_expr);
		sf.setOperandAttr(
			new StringMultipleComparison(se1, Operator.STARTSWITH, se0, other, (long)result));

		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}

	public static Instruction str_fnc_endsWith(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		
		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression se0 = (StringExpression) sf.getOperandAttr(0);
		StringExpression se1 = (StringExpression) sf.getOperandAttr(1);

		//get the Strings using the positions from the stack. Order is crucial here
		String secondStr = ks.heap.get(sf.pop()).asString();
		String firstStr = ks.heap.get(sf.pop()).asString();
			
		//compute the resulting value and push it on the real stack
		int result = BooleanHelper.StringEndsWith(firstStr, secondStr);
		sf.push(result);
			
		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(
			new StringComparison(se1, Operator.ENDSWITH, se0, (long)result));

		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}
	
	public static Instruction str_fnc_contains(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		
		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression se0 = (StringExpression) sf.getOperandAttr(0);
		StringExpression se1 = (StringExpression) sf.getOperandAttr(1);
	
		//get the Strings using the positions from the stack. Order is crucial here
		String secondStr = ks.heap.get(sf.pop()).asString();
		String firstStr = ks.heap.get(sf.pop()).asString();		
		
		//compute the resulting value and push it on the real stack
		//TODO maybe do this with some new and fancy BooleanHelper function?!?
		boolean result = firstStr.contains(secondStr);
		sf.push((result) ? 1 : 0);

		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(
			new StringComparison(se1, Operator.CONTAINS, se0, (long)((result) ? 1 : 0)));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}
	
	@SuppressWarnings("unchecked")
	public static Instruction str_fnc_regionMatches(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {

		/*
		 * Here we have two cases:
		 * 	1. with Ignore case on the stack we have:
		 * 
		 * 		string ..... first string
		 * 		bool ....... ignore case
		 * 		int ........ offset of first
		 * 		string ..... second string
		 * 		int ........ offset of second
		 * 		int ........ length that should be matched
		 * 
		 * 
		 * 	2. normal (no ignore case):
		 * 
		 * 		string ..... first string
		 * 		int ........ offset of first
		 * 		string ..... second string
		 * 		int ........ offset of second
		 * 		int ........ length that should be matched
		 */
		boolean case_one = ins.getInvokedMethodSignature().startsWith("(ZILjava/lang/String;II)Z");
		
		StackFrame sf = ti.getTopFrame();
		
		//we are offsetting the last stack position if we are in case_one
		int sf_offs_ign = case_one ? 1 : 0;
		
		//get values from the fake stack 
		Expression<Long> len = (Expression<Long>) sf.getOperandAttr(0);
		Expression<Long> offs_two = (Expression<Long>) sf.getOperandAttr(1);
		StringExpression str_two = (StringExpression) sf.getOperandAttr(2);
		Expression<Long> offs_one = (Expression<Long>) sf.getOperandAttr(3);
		Expression<Long> ign_case; 
		if (case_one) {
			ign_case = (Expression<Long>) sf.getOperandAttr(4);
		} else {
			ign_case = new IntegerConstant(0);
		}
		StringExpression str_one = (StringExpression) sf.getOperandAttr(4 + sf_offs_ign);
		
		//get the values from the real stack
		int length = sf.pop();
		int offset2 = sf.pop();
		String secondStr = ks.heap.get(sf.pop()).asString();
		int offset1 = sf.pop();
		boolean ignore_case = false;
		if (case_one) {
			ignore_case = (sf.pop() > 0) ? true : false;
		}
		String firstStr = ks.heap.get(sf.pop()).asString();
		
		//if we don't have expressions from the ints in this function we create them 
		if (len == null) {
			len = new IntegerConstant(length);
		}
		if (offs_two == null) {
			offs_two = new IntegerConstant(offset2);
		}
		if (offs_one == null) {
			offs_one = new IntegerConstant(offset1);
		}
		if (ign_case == null) {
			ign_case = new IntegerConstant(ignore_case ? 1 : 0);
		}

		//compute the resulting value and push it on the real stack
		//TODO maybe do this with some new and fancy BooleanHelper function?!?
		boolean result = firstStr.regionMatches(ignore_case, offset1, secondStr, offset2, length);
		sf.push((result) ? 1 : 0);
		
		
		//int result = BooleanHelper.StringRegionMatches(firstStr, offset1, secondStr, offset2, length, ignore_case);
		//sf.push(result);

		//push a StringComparation expression on the fake stack
		//		initialize the arraylist of the "other" arguments
		ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
		other.add(offs_one);
		other.add(offs_two);
		other.add(len);
		other.add(ign_case);	
		//		create the new StringComparison
		StringMultipleComparison s = new StringMultipleComparison(str_one, 
							Operator.REGIONMATCHES, str_two, other, (long)((result) ? 1 : 0));
		//		add it to the fake stack
		sf.setOperandAttr(s);
		

		return ins.getNext(ti);
	}

}
