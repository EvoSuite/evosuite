/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package de.unisb.cs.st.evosuite.symbolic.nativepeer;


import java.util.logging.Logger;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.MJIEnv;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.symbolic.expr.IntegerVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.RealVariable;
import de.unisb.cs.st.evosuite.symbolic.expr.StringVariable;

public class JPF_de_unisb_cs_st_evosuite_symbolic_nativepeer_ConcolicMarker {
	private static int counter = 0;

	static Logger log = JPF.getLogger(JPF_de_unisb_cs_st_evosuite_symbolic_nativepeer_ConcolicMarker.class.toString());
//	private static Logger logger = LoggerFactory.getLogger(JPF_de_unisb_cs_st_evosuite_symbolic_nativepeer_ConcolicMarker.class);
	
	public static boolean mark__Z__Z(MJIEnv env, int rcls, boolean v0) {
		env.setReturnAttribute(new IntegerVariable("mark" + counter++ + "__SYM",
				v0 ? 1 : 0, Integer.MIN_VALUE, Integer.MAX_VALUE));
		return v0;
	}

	public static char mark__C__C(MJIEnv env, int rcls, char v0) {
		env.setReturnAttribute(new IntegerVariable("mark" + counter++ + "__SYM",
		        v0, Character.MIN_VALUE, Character.MAX_VALUE));
		return v0;
	}

	public static short mark__S__S(MJIEnv env, int rcls, short v0) {
		env.setReturnAttribute(new IntegerVariable("mark" + counter++ + "__SYM",
				v0, Short.MIN_VALUE, Short.MAX_VALUE));
		return v0;
	}

	public static byte mark__B__B(MJIEnv env, int rcls, byte v0) {
		env.setReturnAttribute(new IntegerVariable("mark" + counter++ + "__SYM",
				v0, Byte.MIN_VALUE, Byte.MAX_VALUE));
		return v0;
	}

	public static int mark__I__I(MJIEnv env, int rcls, int v0) {
		env.setReturnAttribute(new IntegerVariable("mark" + counter++ + "__SYM",
				v0, Integer.MIN_VALUE, Integer.MAX_VALUE));
//		logger.info("Marked integer!");
		return v0;
	}

	public static long mark__J__J(MJIEnv env, int rcls, long v0) {
		env.setReturnAttribute(new IntegerVariable("mark" + counter++ + "__SYM",
				v0, Long.MIN_VALUE, Long.MAX_VALUE));
		return v0;
	}

	public static float mark__F__F(MJIEnv env, int rcls, float v0) {
		env.setReturnAttribute(new RealVariable("mark" + counter++ + "__SYM",
				v0, -Float.MAX_VALUE, Float.MAX_VALUE));
		return v0;
	}

	public static double mark__D__D(MJIEnv env, int rcls, double v0) {
		env.setReturnAttribute(new RealVariable("mark" + counter++ + "__SYM",
				v0, -Double.MAX_VALUE, Double.MAX_VALUE));
		return v0;
	}
	
	//here are the two new ones ===============================================
	
	public static int mark__Ljava_lang_String_2__Ljava_lang_String_2(MJIEnv env, 
			int rcls, int rString) {
		String str = env.getStringObject(rString);
		
		env.setReturnAttribute(new StringVariable("mark" + counter++ + "__SYM",
		        str, str, str));

		return rString;
	}

	public static int mark__Ljava_lang_String_2Ljava_lang_String_2__Ljava_lang_String_2(
			MJIEnv env, int rcls, int rString, int rString1) {
		String str = env.getStringObject(rString);

		env.setReturnAttribute(new StringVariable(env.getStringObject(rString1) 
				+ "__SYM", str, str, str));

		return rString;
	}

	
	//=========================================================================
	
	
	
	public static boolean mark__ZLjava_lang_String_2__Z(MJIEnv env, int rcls, boolean v0,
	        int rString1) {
		env.setReturnAttribute(new IntegerVariable(env.getStringObject(rString1)
		        + "__SYM", v0 ? 1:0, Integer.MIN_VALUE, Integer.MAX_VALUE));
		return v0;
	}

	public static char mark__CLjava_lang_String_2__C(MJIEnv env, int rcls, char v0,
	        int rString1) {
		env.setReturnAttribute(new IntegerVariable(env.getStringObject(rString1)
		        + "__SYM", v0, Character.MIN_VALUE, Character.MAX_VALUE));
		return v0;
	}

	public static short mark__SLjava_lang_String_2__S(MJIEnv env, int rcls, short v0,
	        int rString1) {
		env.setReturnAttribute(new IntegerVariable(env.getStringObject(rString1)
		        + "__SYM", v0, Short.MIN_VALUE, Short.MAX_VALUE));
		return v0;
	}

	public static byte mark__BLjava_lang_String_2__B(MJIEnv env, int rcls, byte v0,
	        int rString1) {
		env.setReturnAttribute(new IntegerVariable(env.getStringObject(rString1)
		        + "__SYM", v0, Byte.MIN_VALUE, Byte.MAX_VALUE));
		return v0;
	}

	public static int mark__ILjava_lang_String_2__I(MJIEnv env, int rcls, int v0,
	        int rString1) {
		env.setReturnAttribute(new IntegerVariable(env.getStringObject(rString1)
		        + "__SYM", v0, Integer.MIN_VALUE, Integer.MAX_VALUE));
		return v0;
	}

	public static long mark__JLjava_lang_String_2__J(MJIEnv env, int rcls, long v0,
	        int rString1) {
		env.setReturnAttribute(new IntegerVariable(env.getStringObject(rString1)
		        + "__SYM", v0, Long.MIN_VALUE, Long.MAX_VALUE));
		return v0;
	}

	public static float mark__FLjava_lang_String_2__F(MJIEnv env, int rcls, float v0,
	        int rString1) {
		env.setReturnAttribute(new RealVariable(env.getStringObject(rString1) + "__SYM",
		        v0, -Float.MAX_VALUE, Float.MAX_VALUE));
		return v0;
	}

	public static double mark__DLjava_lang_String_2__D(MJIEnv env, int rcls, double v0,
	        int rString1) {
		env.setReturnAttribute(new RealVariable(env.getStringObject(rString1) + "__SYM",
				v0, -Double.MAX_VALUE, Double.MAX_VALUE));
		return v0;
	}
}