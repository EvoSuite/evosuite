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
//
// Copyright (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA). All Rights Reserved.
//
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3. The NOSA has been approved by the Open Source
// Initiative. See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
//
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//

package org.evosuite.symbolic.nativepeer;

import java.util.logging.Logger;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.IntegerConstant;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.IntegerUnaryExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealConstant;
import org.evosuite.symbolic.expr.RealToIntegerCast;
import org.evosuite.symbolic.expr.RealUnaryExpression;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.MJIEnv;

/**
 * MJI NativePeer class for java.lang.Math library abstraction
 */

public class JPF_java_lang_Math {

	@SuppressWarnings("unused")
	private static Logger log = JPF.getLogger(JPF_java_lang_Math.class.toString());
	
	@SuppressWarnings("unchecked")
	public static double abs__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.abs(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.abs(a);
		} else {
			double ret = Math.abs(a);
			RealUnaryExpression result = 
						new RealUnaryExpression(sym_arg, Operator.ABS, ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static float abs__F__F(MJIEnv env, int clsObjRef, float a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.abs(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.abs(a);
		} else {
			float ret = Math.abs(a);
			RealUnaryExpression result = 
					new RealUnaryExpression(sym_arg, Operator.ABS, (double)ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static int abs__I__I(MJIEnv env, int clsObjRef, int a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.abs(a);
		}
		Expression<Long> sym_arg = (Expression<Long>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.abs(a);
		} else {
			int ret = Math.abs(a);
			IntegerUnaryExpression result = new IntegerUnaryExpression(sym_arg, Operator.ABS,
			        (long)ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static long abs__J__J(MJIEnv env, int clsObjRef, long a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.abs(a);
		}
		Expression<Long> sym_arg = (Expression<Long>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.abs(a);
		} else {
			long ret = Math.abs(a);
			IntegerUnaryExpression result = 
						new IntegerUnaryExpression(sym_arg, Operator.ABS, ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static double acos__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.acos(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.acos(a);
		} else {
			double ret = Math.acos(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.ACOS,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}

	}

	@SuppressWarnings("unchecked")
	public static double asin__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.asin(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.asin(a);
		} else {
			double ret = Math.asin(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.ASIN,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double atan__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.atan(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.atan(a);
		} else {
			double ret = Math.atan(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.ATAN,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double atan2__DD__D(MJIEnv env, int clsObjRef, double a, double b) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.atan2(a, b);
		}
		Expression<Double> sym_arg1 = (Expression<Double>) attrs[0];
		Expression<Double> sym_arg2 = (Expression<Double>) attrs[1];
		Expression<Double> result;
		double ret = Math.atan2(a, b);
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		} else if (sym_arg1 == null) {
			result = new RealBinaryExpression(new RealConstant(a), Operator.ATAN2,
			        sym_arg2, ret);
		} else if (sym_arg2 == null) {
			result = new RealBinaryExpression(sym_arg1, Operator.ATAN2, new RealConstant(
			        b), ret);
		} else {
			result = new RealBinaryExpression(sym_arg1, Operator.ATAN2, sym_arg2, ret);
		}

		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static double ceil__D__D(MJIEnv env, int rcls, double v0) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.ceil(v0);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.ceil(v0);
		} else {
			double ret = Math.ceil(v0);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.CEIL,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double cos__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.cos(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.cos(a);
		} else {
			double ret = Math.cos(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.COS,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double cosh__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.cosh(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.cosh(a);
		} else {
			double ret = Math.cosh(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.COSH,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double exp__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.exp(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.exp(a);
		} else {
			double ret = Math.exp(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.EXP,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double expm1__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.expm1(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.expm1(a);
		} else {
			double ret = Math.expm1(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.EXPM1,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double floor__D__D(MJIEnv env, int rcls, double v0) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.floor(v0);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.floor(v0);
		} else {
			double ret = Math.floor(v0);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.FLOOR,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static double IEEEremainder__DD__D(MJIEnv env, int clsObjRef, double a, double b) {
		Object[] attrs = env.getArgAttributes();
		double ret = Math.IEEEremainder(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Double> sym_arg1 = (Expression<Double>) attrs[0];
		Expression<Double> sym_arg2 = (Expression<Double>) attrs[1];
		Expression<Double> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new RealConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new RealConstant(b);
		}
		
		result = new RealBinaryExpression(sym_arg1, Operator.IEEEREMAINDER, sym_arg2, ret);
		env.setReturnAttribute(result);
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static double log__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.log(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.log(a);
		} else {
			double ret = Math.log(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.LOG,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double max__DD__D(MJIEnv env, int clsObjRef, double a, double b) {
		Object[] attrs = env.getArgAttributes();
		double ret = Math.max(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Double> sym_arg1 = (Expression<Double>) attrs[0];
		Expression<Double> sym_arg2 = (Expression<Double>) attrs[1];
		Expression<Double> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new RealConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new RealConstant(b);
		}
		
		result = new RealBinaryExpression(sym_arg1, 
								Operator.MAX, sym_arg2, ret);
		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static float max__FF__F(MJIEnv env, int clsObjRef, float a, float b) {
		Object[] attrs = env.getArgAttributes();
		float ret = Math.max(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Double> sym_arg1 = (Expression<Double>) attrs[0];
		Expression<Double> sym_arg2 = (Expression<Double>) attrs[1];
		Expression<Double> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new RealConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new RealConstant(b);
		}
		
		result = new RealBinaryExpression(sym_arg1, 
								Operator.MAX, sym_arg2, (double)ret);
		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static int max__II__I(MJIEnv env, int clsObjRef, int a, int b) {
		Object[] attrs = env.getArgAttributes();
		int ret = Math.max(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Long> sym_arg1 = (Expression<Long>) attrs[0];
		Expression<Long> sym_arg2 = (Expression<Long>) attrs[1];
		Expression<Long> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new IntegerConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new IntegerConstant(b);
		}
		
		result = new IntegerBinaryExpression(sym_arg1, 
								Operator.MAX, sym_arg2, (long)ret);
		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static long max__JJ__J(MJIEnv env, int clsObjRef, long a, long b) {
		Object[] attrs = env.getArgAttributes();
		long ret = Math.max(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Long> sym_arg1 = (Expression<Long>) attrs[0];
		Expression<Long> sym_arg2 = (Expression<Long>) attrs[1];
		Expression<Long> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new IntegerConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new IntegerConstant(b);
		}
		
		result = new IntegerBinaryExpression(sym_arg1, 
								Operator.MAX, sym_arg2, ret);
		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static double min__DD__D(MJIEnv env, int clsObjRef, double a, double b) {
		Object[] attrs = env.getArgAttributes();
		double ret = Math.min(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Double> sym_arg1 = (Expression<Double>) attrs[0];
		Expression<Double> sym_arg2 = (Expression<Double>) attrs[1];
		Expression<Double> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new RealConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new RealConstant(b);
		}
		
		result = new RealBinaryExpression(sym_arg1, 
								Operator.MIN, sym_arg2, ret);
		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static float min__FF__F(MJIEnv env, int clsObjRef, float a, float b) {
		Object[] attrs = env.getArgAttributes();
		float ret = Math.min(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Double> sym_arg1 = (Expression<Double>) attrs[0];
		Expression<Double> sym_arg2 = (Expression<Double>) attrs[1];
		Expression<Double> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new RealConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new RealConstant(b);
		}
		
		result = new RealBinaryExpression(sym_arg1, 
								Operator.MIN, sym_arg2, (double)ret);
		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static int min__II__I(MJIEnv env, int clsObjRef, int a, int b) {
		Object[] attrs = env.getArgAttributes();
		int ret = Math.min(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Long> sym_arg1 = (Expression<Long>) attrs[0];
		Expression<Long> sym_arg2 = (Expression<Long>) attrs[1];
		Expression<Long> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new IntegerConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new IntegerConstant(b);
		}
		
		result = new IntegerBinaryExpression(sym_arg1, 
								Operator.MIN, sym_arg2, (long)ret);
		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static long min__JJ__J(MJIEnv env, int clsObjRef, long a, long b) {
		Object[] attrs = env.getArgAttributes();
		long ret = Math.min(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Long> sym_arg1 = (Expression<Long>) attrs[0];
		Expression<Long> sym_arg2 = (Expression<Long>) attrs[1];
		Expression<Long> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new IntegerConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new IntegerConstant(b);
		}
		
		result = new IntegerBinaryExpression(sym_arg1, 
								Operator.MIN, sym_arg2, ret);
		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static double log10__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.log10(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.log10(a);
		} else {
			double ret = Math.log10(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.LOG10,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double log1p__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.log1p(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.log1p(a);
		} else {
			double ret = Math.log1p(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.LOG1P,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double pow__DD__D(MJIEnv env, int clsObjRef, double a, double b) {
		
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.atan2(a, b);
		}
		Expression<Double> sym_arg1 = (Expression<Double>) attrs[0];
		Expression<Double> sym_arg2 = (Expression<Double>) attrs[1];
		Expression<Double> result;
		double ret = Math.pow(a, b);
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		} else if (sym_arg1 == null) {
			result = new RealBinaryExpression(new RealConstant(a), Operator.POW,
			        sym_arg2, ret);
		} else if (sym_arg2 == null) {
			result = new RealBinaryExpression(sym_arg1, Operator.POW,
			        new RealConstant(b), ret);
		} else {
			result = new RealBinaryExpression(sym_arg1, Operator.POW, sym_arg2, ret);
		}

		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static double rint__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.rint(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.rint(a);
		} else {
			double ret = Math.rint(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.RINT,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static long round__D__J(MJIEnv env, int rcls, double v0) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.round(v0);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.round(v0);
		} else {
			long ret = Math.round(v0);
			IntegerExpression result = new RealToIntegerCast(sym_arg, ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static int round__F__I(MJIEnv env, int rcls, float v0) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.round(v0);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.round(v0);
		} else {
			int ret = Math.round(v0);
			IntegerExpression result = new RealToIntegerCast(sym_arg, (long) ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double sin__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.sin(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.sin(a);
		} else {
			double ret = Math.sin(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.SIN,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}

	}

	@SuppressWarnings("unchecked")
	public static double sinh__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.sinh(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.sinh(a);
		} else {
			double ret = Math.sinh(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.SINH,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}

	}

	@SuppressWarnings("unchecked")
	public static double sqrt__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.sqrt(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.sqrt(a);
		} else {
			double ret = Math.sqrt(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.SQRT,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double tan__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.tan(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.tan(a);
		} else {
			double ret = Math.tan(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.TAN,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double tanh__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.tanh(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.tanh(a);
		} else {
			double ret = Math.tanh(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.TANH,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double toDegrees__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.toDegrees(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.toDegrees(a);
		} else {
			double ret = Math.toDegrees(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.TODEGREES,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double toRadians__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.toRadians(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.toRadians(a);
		} else {
			double ret = Math.toRadians(a);
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.TORADIANS,
			        ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double cbrt__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.cbrt(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.cbrt(a);
		} else {
			double ret = Math.cbrt(a);
			RealUnaryExpression result = 
				new RealUnaryExpression(sym_arg, Operator.CBRT, ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double nextUp__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.nextUp(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.nextUp(a);
		} else {
			double ret = Math.nextUp(a);
			RealUnaryExpression result = 
				new RealUnaryExpression(sym_arg, Operator.NEXTUP, ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static float nextUp__F__F(MJIEnv env, int clsObjRef, float a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.nextUp(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.nextUp(a);
		} else {
			float ret = Math.nextUp(a);
			RealUnaryExpression result = 
				new RealUnaryExpression(sym_arg, Operator.NEXTUP, (double)ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double signum__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.signum(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.signum(a);
		} else {
			double ret = Math.signum(a);
			RealUnaryExpression result = 
				new RealUnaryExpression(sym_arg, Operator.SIGNUM, ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static float signum__F__F(MJIEnv env, int clsObjRef, float a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.signum(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.signum(a);
		} else {
			float ret = Math.signum(a);
			RealUnaryExpression result = 
				new RealUnaryExpression(sym_arg, Operator.SIGNUM, (double)ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double ulp__D__D(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.ulp(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.ulp(a);
		} else {
			double ret = Math.ulp(a);
			RealUnaryExpression result = 
				new RealUnaryExpression(sym_arg, Operator.ULP, ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static float ulp__F__F(MJIEnv env, int clsObjRef, float a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.ulp(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.ulp(a);
		} else {
			float ret = Math.ulp(a);
			RealUnaryExpression result = 
				new RealUnaryExpression(sym_arg, Operator.ULP, (double)ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static int getExponent__D__I(MJIEnv env, int clsObjRef, double a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.getExponent(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.getExponent(a);
		} else {
			int ret = Math.getExponent(a);
			RealUnaryExpression realUExpr = 
				new RealUnaryExpression(sym_arg, Operator.GETEXPONENT, (double)ret);
			RealToIntegerCast result = new RealToIntegerCast(realUExpr, (long)ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static int getExponent__F__I(MJIEnv env, int clsObjRef, float a) {
		Object[] attrs = env.getArgAttributes();
		if (attrs == null) {
			return Math.getExponent(a);
		}
		Expression<Double> sym_arg = (Expression<Double>) attrs[0];
		if (sym_arg == null) { // concrete
			return Math.getExponent(a);
		} else {
			int ret = Math.getExponent(a);
			RealUnaryExpression realUExpr = 
				new RealUnaryExpression(sym_arg, Operator.GETEXPONENT, (double)ret);
			RealToIntegerCast result = new RealToIntegerCast(realUExpr, (long)ret);
			env.setReturnAttribute(result);
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public static double copySign__DD__D(MJIEnv env, int clsObjRef, double a, double b) {
		Object[] attrs = env.getArgAttributes();
		double ret = Math.copySign(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Double> sym_arg1 = (Expression<Double>) attrs[0];
		Expression<Double> sym_arg2 = (Expression<Double>) attrs[1];
		Expression<Double> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new RealConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new RealConstant(b);
		}
		
		result = new RealBinaryExpression(sym_arg1, 
								Operator.COPYSIGN, sym_arg2, ret);
		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static float copySign__FF__F(MJIEnv env, int clsObjRef, float a, float b) {
		Object[] attrs = env.getArgAttributes();
		float ret = Math.copySign(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Double> sym_arg1 = (Expression<Double>) attrs[0];
		Expression<Double> sym_arg2 = (Expression<Double>) attrs[1];
		Expression<Double> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new RealConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new RealConstant(b);
		}
		
		result = new RealBinaryExpression(sym_arg1, 
								Operator.COPYSIGN, sym_arg2, (double)ret);
		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static double hypot__DD__D(MJIEnv env, int clsObjRef, double a, double b) {
		Object[] attrs = env.getArgAttributes();
		double ret = Math.hypot(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Double> sym_arg1 = (Expression<Double>) attrs[0];
		Expression<Double> sym_arg2 = (Expression<Double>) attrs[1];
		Expression<Double> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new RealConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new RealConstant(b);
		}
		
		result = new RealBinaryExpression(sym_arg1, 
								Operator.HYPOT, sym_arg2, ret);
		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static double nextAfter__DD__D(MJIEnv env, int clsObjRef, double a, double b) {
		Object[] attrs = env.getArgAttributes();
		double ret = Math.nextAfter(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Double> sym_arg1 = (Expression<Double>) attrs[0];
		Expression<Double> sym_arg2 = (Expression<Double>) attrs[1];
		Expression<Double> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new RealConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new RealConstant(b);
		}
		
		result = new RealBinaryExpression(sym_arg1, 
								Operator.NEXTAFTER, sym_arg2, ret);
		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static float nextAfter__FD__F(MJIEnv env, int clsObjRef, float a, double b) {
		Object[] attrs = env.getArgAttributes();
		float ret = Math.nextAfter(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Double> sym_arg1 = (Expression<Double>) attrs[0];
		Expression<Double> sym_arg2 = (Expression<Double>) attrs[1];
		Expression<Double> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new RealConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new RealConstant(b);
		}
		
		result = new RealBinaryExpression(sym_arg1, 
								Operator.NEXTAFTER, sym_arg2, (double)ret);
		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static double scalb__DI__D(MJIEnv env, int clsObjRef, double a, int b) {
		Object[] attrs = env.getArgAttributes();
		double ret = Math.scalb(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Double> sym_arg1 = (Expression<Double>) attrs[0];
		Expression<Long> sym_arg2 = (Expression<Long>) attrs[1];
		Expression<Double> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new RealConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new IntegerConstant(b);
		}
		
		result = new RealBinaryExpression(sym_arg1, 
								Operator.SCALB, sym_arg2, ret);
		env.setReturnAttribute(result);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static float scalb__FI__F(MJIEnv env, int clsObjRef, float a, int b) {
		Object[] attrs = env.getArgAttributes();
		float ret = Math.scalb(a, b);
		
		if (attrs == null) {
			return ret;
		}
		
		Expression<Double> sym_arg1 = (Expression<Double>) attrs[0];
		Expression<Long> sym_arg2 = (Expression<Long>) attrs[1];
		Expression<Double> result;
		
		if (sym_arg1 == null && sym_arg2 == null) {
			return ret;
		}
		if (sym_arg1 == null) {
			sym_arg1 = new RealConstant(a);
		}
		if (sym_arg2 == null) {
			sym_arg2 = new IntegerConstant(b);
		}
		
		result = new RealBinaryExpression(sym_arg1, 
								Operator.SCALB, sym_arg2, (double)ret);
		env.setReturnAttribute(result);
		return ret;
	}

}
