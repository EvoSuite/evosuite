/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.bytecode;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.bytecode.Instruction;

import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.expr.StringConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.StringExpression;

/**
 * @author krusev
 *
 */
public class ALOAD extends gov.nasa.jpf.jvm.bytecode.ALOAD {

	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.invokevitrual");

	public ALOAD(int index){
		super(index);
	}
	  
	public Instruction execute (SystemState ss, KernelState ks, ThreadInfo th) {
		//Order is crucial this should come first
		Instruction ret = super.execute(ss, ks, th);
	    
		//Get the current stack frame
		StackFrame sf = th.getTopFrame();
		
		//get the type of the variable that we are loading
		String type = "";
		ElementInfo el_inf = null;
		
		try {
			int pos = sf.getTopPos();
			//Only try go get the type if this is a reference
		    if (sf.isReferenceSlot(pos)) {
		    	el_inf = ks.heap.get(sf.peek(pos-index));
		    	
		    	//For some reason (maybe when reserving only space) the Element Info is sometimes empty
		    	//So only try to get the type when it is not empty
		    	if (el_inf != null) {
		    		type = el_inf.getType();
		    	}
		    } else {
		    	log.info("Aload: Trying to load a not ref variable");
		    }
		    
	    } catch (Exception e) {
			log.warning("Aload: " + e);
		}

		if ( type.equals("Ljava/lang/String;") ) { 
			String value =  el_inf.asString();
			
			//compute the offset of the variable from the top position
			int offset = sf.getTopPos()-index;
			
			//get the expression on that offset
			StringExpression ex = (StringExpression) sf.getOperandAttr(offset);
			
			//if the expression does not exist make a new one and put it both 
			//		an the variable stack and the operand stack
			//else load the existing expression
			if (ex == null) {
				StringConstant s = new StringConstant(value);
				sf.setOperandAttr(s);
			} else {
				sf.setOperandAttr(ex);
			}
		}
		return ret;
	}
}