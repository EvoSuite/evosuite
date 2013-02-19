package org.evosuite.symbolic.search;

import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.StringConstraint;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.str.StringConstant;
import org.evosuite.symbolic.expr.str.StringVariable;
import org.evosuite.symbolic.vm.ExpressionFactory;
import org.junit.Test;

public class StringAVMTests {

	private List<Constraint<?>> getPatternConstraint(StringVariable var, String format){
		StringConstant symb_regex = ExpressionFactory.buildNewStringConstant(format);
		StringBinaryComparison strComp = new StringBinaryComparison(symb_regex, Operator.PATTERNMATCHES, var, 0L);
		StringConstraint constraint = new StringConstraint(strComp,Comparator.NE, new IntegerConstant(0));
		List<Constraint<?>> constraints = Collections.<Constraint<?>> singletonList(constraint);
		return constraints;
	}
	
	@Test
	public void testIssueWithOptional(){
		String name = "addd";
		StringVariable var = new StringVariable(name, "");
		
		String format = "a.?c";
		List<Constraint<?>> constraints = getPatternConstraint(var,format);
				
		StringAVM avm = new StringAVM(var,constraints);
		boolean succeded = avm.applyAVM();
		Assert.assertTrue(succeded);
	}
	
	@Test
	public void testSimpleRegexThreeDigits(){
		String name = "foo";
		StringVariable var = new StringVariable(name, "");
		
		String format = "\\d\\d\\d";
		List<Constraint<?>> constraints = getPatternConstraint(var,format);
		
		
		StringAVM avm = new StringAVM(var,constraints);
		boolean succeded = avm.applyAVM();
		Assert.assertTrue(succeded);
		
		String result = var.getConcreteValue();
		Integer value = Integer.parseInt(result);
		Assert.assertTrue("Value="+result, value>=0 && value<=999);
	}
	
	@Test
	public void testInsertLeft(){
		String name = "foo";
		String start = "abc";
		StringVariable var = new StringVariable(name, start);
		
		String format = "\\d\\d\\d"+start;
		List<Constraint<?>> constraints = getPatternConstraint(var,format);
		
		
		StringAVM avm = new StringAVM(var,constraints);
		boolean succeded = avm.applyAVM();
		Assert.assertTrue(succeded);
		
		String result = var.getConcreteValue();
		Assert.assertTrue("Length="+result.length(), result.length()==6);
		Assert.assertTrue(result, result.endsWith(start));
	}
	
	@Test
	public void testInsertRight(){
		String name = "foo";
		String start = "abc";
		StringVariable var = new StringVariable(name, start);
		
		String format = start+"\\d\\d\\d";
		List<Constraint<?>> constraints = getPatternConstraint(var,format);
		
		
		StringAVM avm = new StringAVM(var,constraints);
		boolean succeded = avm.applyAVM();
		Assert.assertTrue(succeded);
		
		String result = var.getConcreteValue();
		Assert.assertTrue("Length="+result.length(), result.length()==6);
		Assert.assertTrue(result, result.startsWith(start));
	}
	
}
