/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.bytecode;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.bytecode.Instruction;

import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.expr.StringExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.StringVariable;

/**
 * Store reference into local variable
 * ..., objectref => ...
 */
public class ASTORE extends gov.nasa.jpf.jvm.bytecode.ASTORE {
	
	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.invokevitrual");
	
	public ASTORE(int index){
		super(index);
	}
	
	public Instruction execute (SystemState ss, KernelState ks, ThreadInfo th) {
		//** warning: an ASTORE should store an object reference. However **//
		//**          it is used for subroutines program counters as well.  **//
		
		
		//boolean ref = th.isOperandRef();
	    //th.setLocalVariable(index, th.pop(), ref);
		
		StackFrame sf = th.getTopFrame();
		int offset = sf.getTopPos()-index;
		
		StringExpression new_expr = (StringExpression) sf.getOperandAttr();
		StringExpression curr_expr = (StringExpression) sf.getOperandAttr(offset);
		
		
		if (new_expr != null) {
			//if this here is true we are initializing a variable with a constant 
			if ( (new_expr instanceof StringConstant) ){
				if (curr_expr != null) {
					if (curr_expr instanceof StringVariable) {
						sf.setOperandAttr(offset,
								new StringVariable(
										((StringVariable) curr_expr).getName(), 
										((StringConstant)new_expr).getConcreteValue(), 
										null) 
						);
					}
				}

			} else {
		 		sf.setOperandAttr(offset, new_expr);
			}
		}
		
		//order is crucial this should come last
		Instruction ret = super.execute(ss, ks, th);
		
		return ret;
	}
}
