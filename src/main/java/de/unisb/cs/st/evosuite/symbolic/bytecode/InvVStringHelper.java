/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.bytecode;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.bytecode.Instruction;

import java.util.ArrayList;
import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.Operator;
import de.unisb.cs.st.evosuite.symbolic.expr.StringBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringComparison;
import de.unisb.cs.st.evosuite.symbolic.expr.StringExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringMultipleComparison;
import de.unisb.cs.st.evosuite.symbolic.expr.StringMultipleExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringUnaryExpression;

/**
 * @author krusev
 *
 */
public abstract class InvVStringHelper {
	
	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.bytecode.InvVStringHelper");
	
	private enum Fcase {ONE, TWO, THREE, FOUR}
	
	/* TODO all if (equals(...)) are actually replaced with (equals(...) == 0) or != 0 )
	 * this if we change the compare functions to the ones in BooleanHelper this is no longer correct 
	 * since BooleanHelper.StringEquals("aa", "bb") == 0 evaluates to -254 == 0 which is obviously not true.
	 * The condition is then marked as unsolved. This is not such a big problem but and can be 
	 * fixed but is doesn't really make of sense to do this things here. 
	 * See de.unisb.cs.st.evosuite.symbolic.search.Seeker for more details.
	 */
	
	

	//String Comparisons

	public static Instruction strFncEqualsIgnoreCase(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		
		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression se0 = (StringExpression) sf.getOperandAttr(0);
		StringExpression se1 = (StringExpression) sf.getOperandAttr(1);
	
		//get the Strings using the positions from the stack. Order is crucial here
		String secondStr = ks.heap.get(sf.pop()).asString();
		String firstStr = ks.heap.get(sf.pop()).asString();

		//compute the resulting value and push it on the real stack
		//int result = BooleanHelper.StringEqualsIgnoreCase(firstStr, secondStr);
		int result = firstStr.equalsIgnoreCase(secondStr) ? 1 : 0;
		sf.push(result);

		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(
			new StringComparison(se1, Operator.EQUALSIGNORECASE, se0, (long)result));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}

	public static Instruction strFncEquals(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		
		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression se0 = (StringExpression) sf.getOperandAttr(0);
		StringExpression se1 = (StringExpression) sf.getOperandAttr(1);
	
		//get the Strings using the positions from the stack. Order is crucial here
		String secondStr = ks.heap.get(sf.pop()).asString();
		String firstStr = ks.heap.get(sf.pop()).asString();
		
		//compute the resulting value and push it on the real stack
		//int result = BooleanHelper.StringEquals(firstStr, secondStr);
		int result = firstStr.equals(secondStr) ? 1 : 0;
		sf.push(result);

		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(
			new StringComparison(se1, Operator.EQUALS, se0, (long)result));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}
	
	@SuppressWarnings("unchecked")
	public static Instruction strFncStartsWith(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		
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
			offs_expr = new IntegerConstant(0);
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
		//int result = BooleanHelper.StringStartsWith(firstStr, secondStr, offset);
		int result = firstStr.startsWith(secondStr, offset) ? 1 : 0;
		sf.push(result);
		
		//push a StringComparation expression on the fake stack
		ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
		other.add(offs_expr);
		sf.setOperandAttr(
			new StringMultipleComparison(se1, Operator.STARTSWITH, se0, other, (long)result));

		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}

	public static Instruction strFncEndsWith(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		
		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression se0 = (StringExpression) sf.getOperandAttr(0);
		StringExpression se1 = (StringExpression) sf.getOperandAttr(1);

		//get the Strings using the positions from the stack. Order is crucial here
		String secondStr = ks.heap.get(sf.pop()).asString();
		String firstStr = ks.heap.get(sf.pop()).asString();
			
		//compute the resulting value and push it on the real stack
		//int result = BooleanHelper.StringEndsWith(firstStr, secondStr);
		int result = firstStr.endsWith(secondStr) ? 1 : 0;
		sf.push(result);
			
		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(
			new StringComparison(se1, Operator.ENDSWITH, se0, (long)result));

		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}
	
	//TODO this actually takes CharSequanse as input. Think about implementing it this way is correct.
	public static Instruction strFncContains(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		
		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression se0 = (StringExpression) sf.getOperandAttr(0);
		StringExpression se1 = (StringExpression) sf.getOperandAttr(1);
	
		//get the Strings using the positions from the stack. Order is crucial here
		String secondStr = ks.heap.get(sf.pop()).asString();
		String firstStr = ks.heap.get(sf.pop()).asString();		
		
		
		//compute the resulting value and push it on the real stack
		//int result = BooleanHelper.StringContains(firstStr, secondStr);
		int result = firstStr.contains(secondStr) ? 1 : 0;
		sf.push(result);

		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(new StringComparison(se1, Operator.CONTAINS, se0, (long)result));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}
	
	@SuppressWarnings("unchecked")
	public static Instruction strFncRegionMatches(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {

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
		//int result = BooleanHelper.StringRegionMatches(firstStr, offset1, secondStr, offset2, length, ignore_case);
		int result = firstStr.regionMatches(ignore_case, offset1, secondStr, offset2, length) ? 1 : 0;
		sf.push(result);

		//push a StringComparation expression on the fake stack
		//		initialize the arraylist of the "other" arguments
		ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
		other.add(offs_one);
		other.add(offs_two);
		other.add(len);
		other.add(ign_case);	
		//		create the new StringComparison
		StringMultipleComparison s = new StringMultipleComparison(str_one, 
							Operator.REGIONMATCHES, str_two, other, (long)result);
		//		add it to the fake stack
		sf.setOperandAttr(s);
		

		return ins.getNext(ti);
	}

	public static Instruction strFncCompareTo(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins){

		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression se0 = (StringExpression) sf.getOperandAttr(0);
		StringExpression se1 = (StringExpression) sf.getOperandAttr(1);
	
		//get the Strings using the positions from the stack. Order is crucial here
		String secondStr = ks.heap.get(sf.pop()).asString();
		String firstStr = ks.heap.get(sf.pop()).asString();
		
		//compute the resulting value and push it on the real stack
		int result = firstStr.compareTo(secondStr);
		sf.push(result);

		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(
			new StringComparison(se1, Operator.COMPARETO, se0, (long)result));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}

	public static Instruction strFncCompareToIgnoreCase(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins){

		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression se0 = (StringExpression) sf.getOperandAttr(0);
		StringExpression se1 = (StringExpression) sf.getOperandAttr(1);
	
		//get the Strings using the positions from the stack. Order is crucial here
		String secondStr = ks.heap.get(sf.pop()).asString();
		String firstStr = ks.heap.get(sf.pop()).asString();
		
		//compute the resulting value and push it on the real stack
		int result = firstStr.compareToIgnoreCase(secondStr);
		sf.push(result);

		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(
			new StringComparison(se1, Operator.COMPARETOIGNORECASE, se0, (long)result));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}

	
	// String Operations
	
	public static Instruction strFncSubstring(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		boolean case_one = ins.getInvokedMethodSignature().equals("(I)Ljava/lang/String;");
		/*
		 * case one: val.substring(int start);
		 * case two: val.substring(int start, int end);
		 */
		
		int offset = ((case_one) ? 0 : 1);
		
		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		Expression<?> se_end = null;
		if (!case_one){ 
			se_end = (Expression<?>) sf.getOperandAttr(0);
		}
		Expression<?> se0 = (Expression<?>) sf.getOperandAttr(0 + offset);
		StringExpression se1 = (StringExpression) sf.getOperandAttr(1 + offset);

		//get the Strings using the positions from the stack. Order is crucial here
		int end = -1;
		if (!case_one){ 
			end = sf.pop();
		}
		int start = sf.pop();
		String firstStr = ks.heap.get(sf.pop()).asString();

		if (case_one) {
			end = firstStr.length();
		}
		
		if (!case_one && se_end == null)
			se_end = new IntegerConstant(end);
			
		if (se0 == null) {
			se0 = new IntegerConstant(start);
		}
		
		//TODO offset could be out of range 
		//substring throws an exception
		// ask Gordon how EvoSuite could handle this

		
		//compute the resulting value and push it on the real stack
		String result = null;
		if (case_one) {
			result = firstStr.substring(start);
		} else {
			result = firstStr.substring(start, end);
		}
		
		int pointer = ks.heap.newString(result, ti); 
		sf.push(pointer, true);
		
		ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
		other.add(se_end);
		
		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(
			new StringMultipleExpression(se1, Operator.SUBSTRING, se0, other, result));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}
	
	public static Instruction strFncReplace(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins){
		/* 
		 * case one:
		 * java/lang/String.replace:(CC)Ljava/lang/String;
		 * 
		 * case two:
		 * java/lang/String.replace:(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
		 */
		boolean case_one = ins.getInvokedMethodSignature().equals("(CC)Ljava/lang/String;");
		
		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression str_expr_new = null;
		StringExpression str_expr_old = null;
		Expression<?> char_expr_new = null;
		Expression<?> char_expr_old = null;
		if (case_one) {
			char_expr_new = (Expression<?>) sf.getOperandAttr(0);
			char_expr_old = (Expression<?>) sf.getOperandAttr(1);
		} else {
			str_expr_new = (StringExpression) sf.getOperandAttr(0);
			str_expr_old = (StringExpression) sf.getOperandAttr(1);
		}
		StringExpression str_expr = (StringExpression) sf.getOperandAttr(2);
		
		
		//get the values from the real stack !!!Don't mix the order of this and the previous one
		int seq_new = sf.pop();
		int seq_old = sf.pop();
		//log.warning("char_new: " +  (char)seq_new + " char_old: " + (char)seq_old);
		String str_new = null;
		String str_old = null;
		if (!case_one) {
			str_new = ks.heap.get(seq_new).asString();
			str_old = ks.heap.get(seq_old).asString();
		}
		String str = ks.heap.get(sf.pop()).asString();
		
		//compute the resulting value and push it on the real stack
		String result = null;
		if (case_one) {
			result = str.replace((char)seq_old, (char)seq_new);
		} else {
			result = str.replace(str_new, str_old);
		}
		
		int pointer = ks.heap.newString(result, ti); 
		sf.push(pointer, true);
		
		ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
		if (case_one) {
			if ( char_expr_new == null ) {
				char_expr_new = new IntegerConstant(seq_new);
			}
			if ( char_expr_old == null ) {
				char_expr_old = new IntegerConstant(seq_old);
			}
			other.add(char_expr_new);
		} else {
			other.add(str_expr_new);
		}
		
		
		//push a StringComparation expression on the fake stack
		if (case_one) {
			sf.setOperandAttr(
					new StringMultipleExpression(str_expr, Operator.REPLACEC, char_expr_old, other, result));
		} else {
			sf.setOperandAttr(
					new StringMultipleExpression(str_expr, Operator.REPLACECS, str_expr_old, other, result));
		}
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}

	public static Instruction strFncReplaceAll(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins){

		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression str_expr_new = (StringExpression) sf.getOperandAttr(0);
		StringExpression str_expr_old = (StringExpression) sf.getOperandAttr(1);
		
		StringExpression str_expr = (StringExpression) sf.getOperandAttr(2);
		
		
		//get the values from the real stack !!!Don't mix the order of this and the previous one
		String str_new = ks.heap.get(sf.pop()).asString();
		String str_old = ks.heap.get(sf.pop()).asString();
		
		String str = ks.heap.get(sf.pop()).asString();
		
		//compute the resulting value and push it on the real stack
		String result = str.replaceAll(str_new, str_old);
		
		int pointer = ks.heap.newString(result, ti); 
		sf.push(pointer, true);
		
		ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
		other.add(str_expr_new);

		sf.setOperandAttr(
			new StringMultipleExpression(str_expr, Operator.REPLACEALL, str_expr_old, other, result));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}

	public static Instruction strFncReplaceFirst(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins){

		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression str_expr_new = (StringExpression) sf.getOperandAttr(0);
		StringExpression str_expr_old = (StringExpression) sf.getOperandAttr(1);
		
		StringExpression str_expr = (StringExpression) sf.getOperandAttr(2);
		
		
		//get the values from the real stack !!!Don't mix the order of this and the previous one
		String str_new = ks.heap.get(sf.pop()).asString();
		String str_old = ks.heap.get(sf.pop()).asString();
		
		String str = ks.heap.get(sf.pop()).asString();
		
		//compute the resulting value and push it on the real stack
		String result = str.replaceFirst(str_new, str_old);
		
		int pointer = ks.heap.newString(result, ti); 
		sf.push(pointer, true);
		
		ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
		other.add(str_expr_new);

		sf.setOperandAttr(
			new StringMultipleExpression(str_expr, Operator.REPLACEFIRST, str_expr_old, other, result));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}
	
	public static Instruction strFncToLowerCase(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins){
		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression se1 = (StringExpression) sf.getOperandAttr(0);
	
		//get the Strings using the positions from the stack. Order is crucial here
		String value = ks.heap.get(sf.pop()).asString();

		
		//compute the resulting value and push it on the real stack
		String result = value.toLowerCase();

		int pointer = ks.heap.newString(result, ti); 
		sf.push(pointer, true);


		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(
			new StringUnaryExpression(se1, Operator.TOLOWERCASE, result));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}

	public static Instruction strFncTrim(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins){
		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression se1 = (StringExpression) sf.getOperandAttr(0);
	
		//get the Strings using the positions from the stack. Order is crucial here
		String value = ks.heap.get(sf.pop()).asString();

		//compute the resulting value and push it on the real stack
		String result = value.trim();

		int pointer = ks.heap.newString(result, ti); 
		sf.push(pointer, true);

		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(
			new StringUnaryExpression(se1, Operator.TRIM, result));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}
	
	public static Instruction strFncConcat(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins){

		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression se0 = (StringExpression) sf.getOperandAttr(0);
		StringExpression se1 = (StringExpression) sf.getOperandAttr(1);
	
		//get the Strings using the positions from the stack. Order is crucial here
		String secondStr = ks.heap.get(sf.pop()).asString();
		String firstStr = ks.heap.get(sf.pop()).asString();
		
		//compute the resulting value and push it on the real stack
		String result = firstStr.concat(secondStr);
		int pointer = ks.heap.newString(result, ti); 
		sf.push(pointer, true);

		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(
			new StringBinaryExpression(se1, Operator.CONCAT, se0, result));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}
	
	public static Instruction strFncLength(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins){

		StackFrame sf = ti.getTopFrame();
		
		//get values from the fake stack 
		StringExpression se1 = (StringExpression) sf.getOperandAttr(0);
	
		//get the Strings using the positions from the stack. Order is crucial here
		String firstStr = ks.heap.get(sf.pop()).asString();
		
		//compute the resulting value and push it on the real stack
		int result = firstStr.length();
		sf.push(result);

		//push a StringComparation expression on the fake stack
		sf.setOperandAttr(
			new StringUnaryExpression(se1, Operator.LENGTH, Integer.toString(result)));
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}

	public static Instruction strFncIndexOf(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins){
		StackFrame sf = ti.getTopFrame();
		
		//declare local expression variables
		Expression<?> char_expr = null;
		Expression<?> indx_expr = null;
		StringExpression se0 = null;
		StringExpression se1 = null;
		ArrayList<Expression<?>> other = new ArrayList<Expression<?>>();
		
		//declare local "really" used variables
		int chr = -1;
		int indx = -1;
		String srch = null;
		String value = null;
		int result = -1;
		
		/* four cases:
		 * case 1: indexOf(int character)
		 * case 2: indexOf(String what_to_search)
		 * case 3: indexOf(int charicter, int begin_idx)
		 * case 4: indexOf(String what_to_search, int begin_idx)
		 */
		Fcase case_x = null;
		if (ins.getInvokedMethodSignature().equals("(I)I")) {
			case_x = Fcase.ONE;
		} else if (ins.getInvokedMethodSignature().equals("(Ljava/lang/String;)I")) {
			case_x = Fcase.TWO;
		} else if (ins.getInvokedMethodSignature().equals("(II)I")) {
			case_x = Fcase.THREE;
		} else if (ins.getInvokedMethodSignature().equals("(Ljava/lang/String;I)I")) {
			case_x = Fcase.FOUR;
		}

		/* In each case we
		 * 		- get values from the fake stack
		 * 		- get the variables using the positions from the stack. Order is crucial here
		 * 		- compute the resulting value and push it on the real stack
		 * 		- push the result on the "real" stack
		 * 		- push a newly created expression on the "fake" stack
		 */
		switch (case_x) {
		case ONE:
			char_expr = (Expression<?>) sf.getOperandAttr(0);
			se1 = (StringExpression) sf.getOperandAttr(1);
			
			chr = sf.pop();
			value = ks.heap.get(sf.pop()).asString();
			if (char_expr == null) {
				char_expr = new IntegerConstant(chr);
			}
			
			result = value.indexOf(chr);
			
			sf.push(result);
			
			sf.setOperandAttr(
					new StringBinaryExpression(se1, Operator.INDEXOFC, 
									char_expr, Integer.toString(result)));
			break;
		case TWO:
			se0 = (StringExpression) sf.getOperandAttr(0);
			se1 = (StringExpression) sf.getOperandAttr(1);
			
			srch = ks.heap.get(sf.pop()).asString();
			value = ks.heap.get(sf.pop()).asString();
			
			result = value.indexOf(srch);
			
			sf.push(result);
			
			sf.setOperandAttr(
					new StringBinaryExpression(se1, Operator.INDEXOFS, 
									se0, Integer.toString(result)));
			break;
		case THREE:
			indx_expr = (Expression<?>) sf.getOperandAttr(0);
			char_expr = (Expression<?>) sf.getOperandAttr(1);
			se1 = (StringExpression) sf.getOperandAttr(2);
			
			indx = sf.pop();
			chr = sf.pop();
			value = ks.heap.get(sf.pop()).asString();
			if (indx_expr == null) {
				indx_expr = new IntegerConstant(indx);
			}
			if (char_expr == null) {
				char_expr = new IntegerConstant(chr);
			}
			
			result = value.indexOf(chr, indx);
			
			sf.push(result);
			
			other.add(indx_expr);
			sf.setOperandAttr(
					new StringMultipleExpression(se1, Operator.INDEXOFCI, 
									char_expr, other, Integer.toString(result)));
			break;
		case FOUR:
			indx_expr = (Expression<?>) sf.getOperandAttr(0);
			se0 = (StringExpression) sf.getOperandAttr(1);
			se1 = (StringExpression) sf.getOperandAttr(2);
			
			indx = sf.pop();
			srch = ks.heap.get(sf.pop()).asString();
			value = ks.heap.get(sf.pop()).asString();
			if (indx_expr == null) {
				indx_expr = new IntegerConstant(indx);
			}
			
			result = value.indexOf(srch, indx);
			
			sf.push(result);
			
			other.add(indx_expr);
			sf.setOperandAttr(
					new StringMultipleExpression(se1, Operator.INDEXOFSI, 
									se0, other, Integer.toString(result)));
			break;
		default:
			log.warning("strFncIndexOf: We are in an unknown case.");
			break;
		}
		
		//return the next instruction that followed the function call
		return ins.getNext(ti);
	}
	
	
	/**	To implement?!?
	 
	lastIndexOf()
	toUpperCase()
	matches()
	 
	getChars(II[CI)V
	charAt(I)C
	
	//this thing here is funny it would not work with 
	intern()Ljava/lang/String;
	
	
	this here is some joda time function. It does not
	compare(Ljava/lang/String;Ljava/lang/String;)I
	
	
	compareTo(Ljava/lang/Object;)I
	
	 */
	

}