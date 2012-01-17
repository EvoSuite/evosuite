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

package de.unisb.cs.st.evosuite.symbolic.nativepeer;

import java.util.logging.Logger;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.MJIEnv;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerUnaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.Operator;
import de.unisb.cs.st.evosuite.symbolic.expr.RealBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.RealConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.RealToIntegerCast;
import de.unisb.cs.st.evosuite.symbolic.expr.RealUnaryExpression;

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
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.ABS,
			        (double)ret);
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
			RealUnaryExpression result = new RealUnaryExpression(sym_arg, Operator.ROUND,
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
}
