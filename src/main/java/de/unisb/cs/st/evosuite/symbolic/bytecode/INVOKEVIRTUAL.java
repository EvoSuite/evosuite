/*
 * Copyright (C) 2005,2006 United States Government as represented by the
 * Administrator of the National Aeronautics and Space Administration (NASA).
 * All Rights Reserved.
 * 
 * Copyright (C) 2011 Saarland University
 * 
 * This file is part of EvoSuite, but based on the SymbC extension of JPF
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
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.symbolic.bytecode;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.bytecode.Instruction;

import java.util.logging.Logger;

/**
 * @author krusev
 * 
 */
public class INVOKEVIRTUAL extends gov.nasa.jpf.jvm.bytecode.INVOKEVIRTUAL {

	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.bytecode.invokevitrual");
	
	protected INVOKEVIRTUAL (String clsDescriptor, String methodName, String signature){
	    super(clsDescriptor, methodName, signature);
	  }
	
	public INVOKEVIRTUAL () {
		super();
	}
	
	@Override
	public Instruction execute(SystemState ss, KernelState ks, ThreadInfo ti) {

		int objRef = ti.getCalleeThis(getArgSize());

		if (objRef == -1) {
			lastObj = -1;
			return ti.createAndThrowException("java.lang.NullPointerException",
			                                  "Calling '" + mname + "' on null object");
		}

		//Check if we are in some String function
		if (cname.equals("java.lang.String")) {

			//Check in which function we are and handle appropriately 
			//TODO this ugly thing here can also be done with java reflection
			//	new methods in the helper classes will not have to be added her too
			try {
				if (mname.startsWith("equalsIgnoreCase")) {
					
					return InvVStringHelper.strFncEqualsIgnoreCase(ks, ti, this);
					
				} else if (mname.startsWith("equals")) {

					return InvVStringHelper.strFncEquals(ks, ti, this);
					
				} else if (mname.startsWith("startsWith")) {

					return InvVStringHelper.strFncStartsWith(ks, ti, this);
					
				} else if (mname.startsWith("endsWith")) {
					
					return InvVStringHelper.strFncEndsWith(ks, ti, this);
					
				} else if (mname.startsWith("contains")) {
					
					return InvVStringHelper.strFncContains(ks, ti, this);
					
				} else if (mname.startsWith("regionMatches")) {

					return InvVStringHelper.strFncRegionMatches(ks, ti, this);
					
				} else if (mname.startsWith("substring")) {
					
					return InvVStringHelper.strFncSubstring(ks, ti, this);
				
				} else if (mname.startsWith("trim")) {
					
					return InvVStringHelper.strFncTrim(ks, ti, this);

				} else if (mname.startsWith("toLowerCase")) {
					
					return InvVStringHelper.strFncToLowerCase(ks, ti, this);
								
				} else if (mname.startsWith("toUpperCase")) {
					
					return InvVStringHelper.strFncToUpperCase(ks, ti, this);
								
				} else if (mname.startsWith("compareTo(")) {
					
					return InvVStringHelper.strFncCompareTo(ks, ti, this);
								
				} else if (mname.startsWith("compareToIgnoreCase")) {
					
					return InvVStringHelper.strFncCompareToIgnoreCase(ks, ti, this);
								
				} else if (mname.startsWith("replace(")) {
					
					return InvVStringHelper.strFncReplace(ks, ti, this);
							
				} else if (mname.startsWith("replaceAll(")) {
					
					return InvVStringHelper.strFncReplaceAll(ks, ti, this);
						
				} else if (mname.startsWith("replaceFirst(")) {
					
					return InvVStringHelper.strFncReplaceFirst(ks, ti, this);
						
				} else if (mname.startsWith("concat(")) {
					
					return InvVStringHelper.strFncConcat(ks, ti, this);
				
				} else if (mname.startsWith("length(")) {
					
					return InvVStringHelper.strFncLength(ks, ti, this);
						
				} else if (mname.startsWith("indexOf(")) {
					
					return InvVStringHelper.strFncIndexOf(ks, ti, this);
							
				} else if (mname.startsWith("charAt(")) {
					
					return InvVStringHelper.strFncCharAt(ks, ti, this);
							
				} else {
					InvVFunctionLogger.LogStringFnc("StringFunctions.txt", this);
				}

			} catch (Exception e) {

				log.warning("Exception: " + e.toString());
			}

		} else if (cname.equals("java.lang.StringBuilder")) {

			try {
				//Check in which function we are and handle appropriately 
				if (mname.startsWith("append(")) {

					InvVStringBuilderHelper.strB_fnc_append(ks, ti, this);

				} else if (mname.startsWith("toString()")) {

					//This works but only if we have just strings appended 
					if (InvVStringBuilderHelper.isStrB_all_impl_op(ks, ti, this)) {
						return InvVStringBuilderHelper.strB_fnc_toString(ks, ti, this);
					}
				} else {
					//TODO if we have some other function here we can throw out the whole thing
					InvVStringBuilderHelper.throw_away(ks, ti, this);

					InvVFunctionLogger.LogStringFnc("StringBuilderFunctions.txt", this);
				}
			} catch (Exception e) {

				log.warning("INVOKEVIRTUAL: " + e.toString());
			}
		}

		//this doesn't really execute the method it just gives the new instruction that the method should be entered 
		//after this instruction the method is not over. It's just beginning.
		Instruction ret = super.execute(ss, ks, ti);

		//if you want to check what is the method returning take a look at the next instruction and see the value on top of the stack 

		return ret;
	}

}
