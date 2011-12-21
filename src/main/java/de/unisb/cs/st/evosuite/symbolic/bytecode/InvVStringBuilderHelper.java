/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.bytecode;

import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.bytecode.INVOKEVIRTUAL;
import de.unisb.cs.st.evosuite.symbolic.expr.Operator;
import de.unisb.cs.st.evosuite.symbolic.expr.StringBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringBuilderExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.StringExpression;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.bytecode.Instruction;

/**
 * @author krusev
 *
 */
public abstract class InvVStringBuilderHelper {
	
	static Logger log = JPF.getLogger("de.unisb.cs.st.evosuite.symbolic.bytecode.InvVStringBuilderHelper");
	
	static StringBuilderExpression strB_expr = null;
	

	
	
	public static Instruction strB_fnc_toString(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		StackFrame sf = ti.getTopFrame();
		StringBuilderExpression se0 = (StringBuilderExpression) sf.getOperandAttr(0);
		
		String result;
		if (se0 == null) {
			result = (String) strB_expr.getConcreteValue();
		} else {
			result = (String) se0.getConcreteValue();
		}
		
		
		sf.pop();
		
		int pointer = ks.heap.newString(result, ti); 
		sf.push(pointer, true);
		
		sf.setOperandAttr((se0 == null) ? strB_expr.getExpr() : se0.getExpr());
		
		return ins.getNext();
	}

	/**
	 * Adds the appropriate StringExpression to the fake stack.
	 * @param sf the current stack frame
	 * @param pp the instruction that was executed 2 instructions ago
	 * 		if it is init or append than we have the plus operator 
	 * 		else it can only be aload_? and thus a normal string builder
	 */
	public static void strB_fnc_append(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		boolean unimpl = false;
		
		StackFrame sf = ti.getTopFrame();
		String pp = ins.getPrev().getPrev().toString();
		StringExpression se0 =  (StringExpression) sf.getOperandAttr(0);




		
		
		/* Possibilities for the append type 
		 * have to check if all can be done
		boolean 		invokevirtual	#14; //Method java/lang/StringBuilder.append:(Z)Ljava/lang/StringBuilder;
		char 			invokevirtual	#15; //Method java/lang/StringBuilder.append:(C)Ljava/lang/StringBuilder;
		char[]			invokevirtual	#16; //Method java/lang/StringBuilder.append:(Ljava/lang/Object;)Ljava/lang/StringBuilder;
		charSequence	invokevirtual	#16; //Method java/lang/StringBuilder.append:(Ljava/lang/Object;)Ljava/lang/StringBuilder;
		double			invokevirtual	#17; //Method java/lang/StringBuilder.append:(D)Ljava/lang/StringBuilder;
		float 			invokevirtual	#18; //Method java/lang/StringBuilder.append:(F)Ljava/lang/StringBuilder;
		int 			invokevirtual	#19; //Method java/lang/StringBuilder.append:(I)Ljava/lang/StringBuilder;
		long			invokevirtual	#20; //Method java/lang/StringBuilder.append:(J)Ljava/lang/StringBuilder;
		Object			
		String			
		StringBuffer	invokevirtual	#16; //Method java/lang/StringBuilder.append:(Ljava/lang/Object;)Ljava/lang/StringBuilder;
		*/
		String mname = ins.getInvokedMethodName();
		if (se0 == null) {
			
			
			if (mname.equals("append(Z)Ljava/lang/StringBuilder;")) {
				int val = sf.peek();
				se0 = new StringConstant((val > 0) ? "true" : "false");

			} else if (mname.equals("append(C)Ljava/lang/StringBuilder;")) {
				int val = sf.peek();
				se0 = new StringConstant( Character.toString((char)val) );
				
			} else if (mname.equals("append(D)Ljava/lang/StringBuilder;")) {
				
				//TODO fill in 
				se0 = new StringConstant(" ");
				unimpl = true;  
				
			} else if (mname.equals("append(F)Ljava/lang/StringBuilder;")) {

				//TODO fill in 
				se0 = new StringConstant(" ");
				unimpl = true;  
				
			} else if (mname.equals("append(I)Ljava/lang/StringBuilder;")) {

				//TODO fix this for int variables
				int val = sf.peek();
				se0 = new StringConstant(Integer.toString(val));
				
			} else if (mname.equals("append(J)Ljava/lang/StringBuilder;")) {

				long val = sf.longPeek();
				se0 = new StringConstant(Long.toString(val));
				
			} else {

				se0 = new StringConstant(" ");
				unimpl = true;  
			}
		}
		


		
		if ( pp.equals("invokespecial java.lang.StringBuilder.<init>()V") ) {
			
			strB_expr = new StringBuilderExpression(se0);
			if (unimpl) {
				strB_expr.set_undef_func();
			}
			
		} else if ( pp.startsWith("invokevirtual java.lang.StringBuilder.append(") && !(strB_expr.has_undef_func())) {

			
			//StringExpression se1 = (StringExpression) sf.getOperandAttr(2);
			String result = ((String)strB_expr.getConcreteValue()) + ((String)se0.getConcreteValue());
			if (unimpl) {
				strB_expr.set_undef_func();
			} else {
				strB_expr.setExpr(new StringBinaryExpression(strB_expr.getExpr(), Operator.APPEND, se0, result));
			}
		} else if (pp.startsWith("aload")) {
			String[] spl_str = pp.split("_");
			if (spl_str.length != 2) {
				log.warning("Something wrong happend in StringBuilder.append");
			}
			int position = Integer.parseInt(spl_str[1]);
			
			int offset = sf.getTopPos() - position;
			
			StringBuilderExpression se1 = (StringBuilderExpression) sf.getOperandAttr(offset);	
			
			
			//If se1 == null we have a new String Builder 
			if (se1 != null && !(se1.has_undef_func())) {		
				String result = ((String)se1.getConcreteValue()) + ((String)se0.getConcreteValue());
				if (unimpl) {
					se1.set_undef_func();
				} else {
					se1.setExpr(new StringBinaryExpression(se1.getExpr(), Operator.APPEND, se0, result));
				}
			} else {
				se1 = new StringBuilderExpression(se0);
				if (unimpl) {
					se1.set_undef_func();
				}
				sf.setOperandAttr(offset, se1);
			}
		}
	}

	public static boolean isStrB_all_impl_op(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		StackFrame sf = ti.getTopFrame();
		StringBuilderExpression se0 = (StringBuilderExpression) sf.getOperandAttr(0);
		
		//TODO talk with Gordon about this
		//If we had an undefined append we lose everything this should be signalized somehow
		
		return (se0 == null) ? !strB_expr.has_undef_func() : !se0.has_undef_func() ;
	}

	public static void throw_away(KernelState ks, ThreadInfo ti, INVOKEVIRTUAL ins) {
		//TODO implement
	}
	
	
}
