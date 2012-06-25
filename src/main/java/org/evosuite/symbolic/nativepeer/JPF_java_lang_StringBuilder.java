/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.symbolic.nativepeer;

import gov.nasa.jpf.jvm.MJIEnv;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntToStringCast;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealToStringCast;
import org.evosuite.symbolic.expr.StringBuilderExpression;
import org.evosuite.symbolic.expr.StringConstant;
import org.evosuite.symbolic.expr.StringExpression;
import org.evosuite.symbolic.expr.Variable;
import org.evosuite.testsuite.TestSuiteDSE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JPF_java_lang_StringBuilder {
	static Logger log = LoggerFactory.getLogger(JPF_java_lang_StringBuilder.class);
		
	public static void $init____ (MJIEnv env, int robj) {
		log.debug("we are in the constructor");
		StringBuilderExpression result = new StringBuilderExpression(null);
		env.setReturnAttribute(result);
		
		int charArrayRef = env.newCharArray(16);
		env.getHeap().get(robj).setReferenceField("value", charArrayRef);
	}
	
	public static void $init__I (MJIEnv env, int robj, int i) {
		log.debug("we are in init__I");
		StringBuilderExpression result = new StringBuilderExpression(null);
		env.setReturnAttribute(result);
		
		int charArrayRef = env.newCharArray(i);
		env.getHeap().get(robj).setReferenceField("value", charArrayRef);
	}
	
	@SuppressWarnings("unchecked")
	public static void $init__Ljava_lang_String_2 (MJIEnv env, int robj, int rString) {
		log.debug("we are in init__Ljava_lang_String_2");
		String str = env.getStringObject(rString);
		if (str==null) str="null";
		
		Object[] attrs = env.getArgAttributes();
		Expression<String> strExpr = null;
		if (attrs != null) 
			strExpr = (Expression<String>) attrs[1];

		StringBuilderExpression result = new StringBuilderExpression(strExpr);
		env.setReturnAttribute(result);

		int len = str.length();
		char[] strB = new char[len+16];
		str.getChars(0, len, strB, 0);
		int charArrayRef = env.newCharArray(strB);
		env.setReferenceField(robj, "value", charArrayRef);
		env.setIntField(robj, "count", len);
	}
	
	public static void $init__Ljava_lang_CharSequence_2 (MJIEnv env, int robj, int rCharSeq) {
		log.debug("we are in init__Ljava_lang_CharSequence_2");
		String str = env.getStringObject(rCharSeq);
		if (str==null) str="null";
		
		StringBuilderExpression result = new StringBuilderExpression(new StringConstant(str));
		env.setReturnAttribute(result);
		
		int len = str.length();
		char[] strB = new char[len+16];
		str.getChars(0, len, strB, 0);
		int charArrayRef = env.newCharArray(strB);
		env.setReferenceField(robj, "value", charArrayRef);
		env.setIntField(robj, "count", len);
	}

	public static int append__Ljava_lang_String_2__Ljava_lang_StringBuilder_2 (MJIEnv env, int robj, int rString) {
		log.debug("we are in append");
		
		
		String str = env.getStringObject(rString);
		if (str==null) str="null";
		int len = str.length();
		
		int count = env.getIntField(robj, "count");
		int valueRef = env.getReferenceField(robj, "value");
		
		ensureCapacityInternal(env, robj, valueRef, count + len);
		
		valueRef = env.getReferenceField(robj, "value");
		for (int i = 0; i<len; i++)
			env.setCharArrayElement(valueRef, count+i, str.charAt(i));
		
		env.setIntField(robj, "count", count+len);
		
		
		Object[] attrs = env.getArgAttributes();

		if (attrs[0] != null ) {
			StringBuilderExpression sB = (StringBuilderExpression)attrs[0];
			Expression<String> strExpr;
			if (attrs[1]!=null)
				strExpr = getExprFromAttr(attrs[1]);
			else
				strExpr = new StringConstant(str);
			
			sB.append(strExpr);
			
			env.setReturnAttribute(sB);
		}
		return robj;
	}
	
	public static int append__I__Ljava_lang_StringBuilder_2 (MJIEnv env, int robj, int val) {
		log.debug("we are in append");
		
		
		String str = Integer.toString(val);
		int len = str.length();
		
		int count = env.getIntField(robj, "count");
		int valueRef = env.getReferenceField(robj, "value");
		
		ensureCapacityInternal(env, robj, valueRef, count + len);
		
		valueRef = env.getReferenceField(robj, "value");
		for (int i = 0; i<len; i++)
			env.setCharArrayElement(valueRef, count+i, str.charAt(i));
		
		env.setIntField(robj, "count", count+len);
		
		
		Object[] attrs = env.getArgAttributes();

		if (attrs[0] != null ) {
			StringBuilderExpression sB = (StringBuilderExpression)attrs[0];
			Expression<String> strExpr;
			if (attrs[1]!=null)
				strExpr = getExprFromAttr(attrs[1]);
			else
				strExpr = new StringConstant(str);
			
			sB.append(strExpr);
			
			env.setReturnAttribute(sB);
		}
		return robj;
	}
	
	public static int append__C__Ljava_lang_StringBuilder_2 (MJIEnv env, int robj, char val) {
		log.debug("we are in append");
		
		String str = Character.toString(val);
		int len = 1; //char has length 1 so save it
		
		int count = env.getIntField(robj, "count");
		int valueRef = env.getReferenceField(robj, "value");
		
		ensureCapacityInternal(env, robj, valueRef, count + len);
		
		valueRef = env.getReferenceField(robj, "value");
		for (int i = 0; i<len; i++)
			env.setCharArrayElement(valueRef, count+i, str.charAt(i));
		
		env.setIntField(robj, "count", count+len);
		
		
		Object[] attrs = env.getArgAttributes();

		if (attrs[0] != null ) {
			StringBuilderExpression sB = (StringBuilderExpression)attrs[0];
			Expression<String> strExpr;
			if (attrs[1]!=null)
				strExpr = getExprFromAttr(attrs[1]);
			else
				strExpr = new StringConstant(str);
			
			sB.append(strExpr);
			
			env.setReturnAttribute(sB);
		}
		return robj;
	}
	
	public static int append__Z__Ljava_lang_StringBuilder_2 (MJIEnv env, int robj, boolean val) {
		log.debug("we are in append");
		
		String str = Boolean.toString(val);
		int len = str.length();
		
		int count = env.getIntField(robj, "count");
		int valueRef = env.getReferenceField(robj, "value");
		
		ensureCapacityInternal(env, robj, valueRef, count + len);
		
		valueRef = env.getReferenceField(robj, "value");
		for (int i = 0; i<len; i++)
			env.setCharArrayElement(valueRef, count+i, str.charAt(i));
		
		env.setIntField(robj, "count", count+len);
		
		
		Object[] attrs = env.getArgAttributes();

		if (attrs[0] != null ) {
			StringBuilderExpression sB = (StringBuilderExpression)attrs[0];
			Expression<String> strExpr;
			if (attrs[1]!=null)
				strExpr = getExprFromAttr(attrs[1]);
			else
				strExpr = new StringConstant(str);
			
			sB.append(strExpr);
			
			env.setReturnAttribute(sB);
		}
		return robj;
	}
	
	public static int append__J__Ljava_lang_StringBuilder_2 (MJIEnv env, int robj, long val) {
		log.debug("we are in append");
		
		String str = Long.toString(val);
		int len = str.length();
		
		int count = env.getIntField(robj, "count");
		int valueRef = env.getReferenceField(robj, "value");
		
		ensureCapacityInternal(env, robj, valueRef, count + len);
		
		valueRef = env.getReferenceField(robj, "value");
		for (int i = 0; i<len; i++)
			env.setCharArrayElement(valueRef, count+i, str.charAt(i));
		
		env.setIntField(robj, "count", count+len);
		
		
		Object[] attrs = env.getArgAttributes();

		if (attrs[0] != null ) {
			StringBuilderExpression sB = (StringBuilderExpression)attrs[0];
			Expression<String> strExpr;
			if (attrs[1]!=null)
				strExpr = getExprFromAttr(attrs[1]);
			else
				strExpr = new StringConstant(str);
			
			sB.append(strExpr);
			
			env.setReturnAttribute(sB);
		}
		return robj;
	}
	
	public static int append__D__Ljava_lang_StringBuilder_2 (MJIEnv env, int robj, double val) {
		log.debug("we are in append");
		
		String str = Double.toString(val);
		int len = str.length();
		
		int count = env.getIntField(robj, "count");
		int valueRef = env.getReferenceField(robj, "value");
		
		ensureCapacityInternal(env, robj, valueRef, count + len);
		
		valueRef = env.getReferenceField(robj, "value");
		for (int i = 0; i<len; i++)
			env.setCharArrayElement(valueRef, count+i, str.charAt(i));
		
		env.setIntField(robj, "count", count+len);
		
		
		Object[] attrs = env.getArgAttributes();
		
		if (attrs[0] != null ) {
			StringBuilderExpression sB = (StringBuilderExpression)attrs[0];
			Expression<String> strExpr;
			if (attrs[1]!=null)
				strExpr = getExprFromAttr(attrs[1]);
			else
				strExpr = new StringConstant(str);
			
			sB.append(strExpr);
			
			env.setReturnAttribute(sB);
		}
		return robj;
	}
	
	public static int append__F__Ljava_lang_StringBuilder_2 (MJIEnv env, int robj, float val) {
		log.debug("we are in append");
		
		String str = Float.toString(val);
		int len = str.length();
		
		int count = env.getIntField(robj, "count");
		int valueRef = env.getReferenceField(robj, "value");
		
		ensureCapacityInternal(env, robj, valueRef, count + len);
		
		valueRef = env.getReferenceField(robj, "value");
		for (int i = 0; i<len; i++)
			env.setCharArrayElement(valueRef, count+i, str.charAt(i));
		
		env.setIntField(robj, "count", count+len);
		
		
		Object[] attrs = env.getArgAttributes();

		if (attrs[0] != null ) {
			StringBuilderExpression sB = (StringBuilderExpression)attrs[0];
			Expression<String> strExpr;
			if (attrs[1]!=null)
				strExpr = getExprFromAttr(attrs[1]);
			else
				strExpr = new StringConstant(str);
			
			sB.append(strExpr);
			
			env.setReturnAttribute(sB);
		}
		return robj;
	}
	
	public static int toString____Ljava_lang_String_2 (MJIEnv env, int robj) {
		int valueRef = env.getReferenceField(robj, "value");
		int pointer = env.newString(new String(env.getCharArrayObject(valueRef)));
		
		Object[] attrs = env.getArgAttributes();
		
		if (containsVariables((Expression<?>)attrs[0])) {
			env.setReturnAttribute(attrs[0]);
		}
		return pointer;
	}
	
	private static void ensureCapacityInternal(MJIEnv env, int robj, int valueRef, int minimumCapacity) {
		int len = env.getArrayLength(valueRef);
		if (minimumCapacity - len > 0)
			expandCapacity(env, robj, valueRef, minimumCapacity);
	}
	
	private static void expandCapacity(MJIEnv env, int robj, int valueRef, int minimumCapacity) {
		int len = env.getArrayLength(valueRef);
		int newCapacity = len * 2 + 2;
		if (newCapacity - minimumCapacity < 0)
			newCapacity = minimumCapacity;
		if (newCapacity < 0) {
			if (minimumCapacity < 0) // overflow
				throw new OutOfMemoryError();
				newCapacity = Integer.MAX_VALUE;
		}	
		
		char[] value = Arrays.copyOf(env.getCharArrayObject(valueRef), newCapacity);
		valueRef = env.newCharArray(value);
		env.setReferenceField(robj, "value", valueRef);
	}
	
	private static boolean containsVariables(Expression<?> expr) {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		TestSuiteDSE.getVariables(expr, variables);
		return variables.size() > 0;
	}
	
	private static Expression<String> getExprFromAttr(Object expr) {
		Expression<String> strExpr = null;
		if (expr instanceof StringExpression) {
			strExpr = (StringExpression) expr;
		}

		if (expr instanceof IntegerExpression) {
			strExpr = new IntToStringCast((IntegerExpression) expr);
		}

		if (expr instanceof RealExpression) {
			strExpr = new RealToStringCast((RealExpression) expr);
		}
		return strExpr;
	}
}
