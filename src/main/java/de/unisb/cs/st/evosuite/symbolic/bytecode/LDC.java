/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.bytecode;

import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.bytecode.Instruction;

import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.expr.StringConstant;
import gov.nasa.jpf.JPF;

/**
 * @author krusev
 *
 */
public class LDC extends gov.nasa.jpf.jvm.bytecode.LDC {
		
	
	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.invokevitrual");
	

	public LDC (String s, boolean isClass){
		super(s, isClass);
	}

	public Instruction execute (SystemState ss, KernelState ks, ThreadInfo ti) {
		Instruction ret = super.execute(ss, ks, ti);

		/*
		 * If we have a Sting constant make an Expression out of it and add it to the 
		 * "other" stack  
		 */
		if (super.getType() == Type.STRING) {			
			StackFrame sf = ti.getTopFrame();
			
			StringConstant s = new StringConstant(super.getStringValue());
			sf.setOperandAttr(s);
		}

	    return ret;
	}

	
}
