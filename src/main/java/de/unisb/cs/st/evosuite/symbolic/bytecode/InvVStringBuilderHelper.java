/**
 * 
 */
package de.unisb.cs.st.evosuite.symbolic.bytecode;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.bytecode.Instruction;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import de.unisb.cs.st.evosuite.symbolic.StringBuilderException;
import de.unisb.cs.st.evosuite.symbolic.expr.Expression;
import de.unisb.cs.st.evosuite.symbolic.expr.IntToStringCast;
import de.unisb.cs.st.evosuite.symbolic.expr.IntegerExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.Operator;
import de.unisb.cs.st.evosuite.symbolic.expr.RealExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.RealToStringCast;
import de.unisb.cs.st.evosuite.symbolic.expr.StringBinaryExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringBuilderExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.StringConstant;
import de.unisb.cs.st.evosuite.symbolic.expr.StringExpression;
import de.unisb.cs.st.evosuite.symbolic.expr.Variable;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteDSE;

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
			if (!containsVariables(strB_expr))
				throw_away("Concrete Execution");

			result = (String) strB_expr.getConcreteValue();
		} else {
			if (!containsVariables(se0)) 
				throw_away("Concrete Execution");
			
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
		
		StackFrame sf = ti.getTopFrame();
		
		boolean unimpl = false;

		String pp = getPrevAppend(ins);
		
		Expression<?> expr =  (Expression<?>) sf.getOperandAttr(0);

		StringExpression se0 = null;
		
		if (expr != null) {
			if (expr instanceof StringExpression) {
				se0 = (StringExpression) expr;
			}

			if (expr instanceof IntegerExpression) {
				se0 = new IntToStringCast((IntegerExpression)expr);
			} 
	
			if (expr instanceof RealExpression) {
				se0 = new RealToStringCast((RealExpression)expr);
			}
		}

		
		
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
			
			} else if (mname.equals("append(Ljava/lang/String;)Ljava/lang/StringBuilder;")) {
				int val = sf.peek();
				se0 = new StringConstant( ks.heap.get(val).asString());
					
			} else if (mname.equals("append(D)Ljava/lang/StringBuilder;")) {
				
				double val = Double.longBitsToDouble(sf.longPeek());
				se0 = new StringConstant(Double.toString(val));
				
			} else if (mname.equals("append(F)Ljava/lang/StringBuilder;")) {

				float val = Float.intBitsToFloat(sf.peek());
				se0 = new StringConstant(Float.toString(val));
				
			} else if (mname.equals("append(I)Ljava/lang/StringBuilder;")) {

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
		
		if ( pp.equals("invokespecial java.lang.StringBuilder.<init>()V")) {
			
			strB_expr = new StringBuilderExpression(se0);
			if (unimpl) {
				strB_expr.set_undef_func();
			}
			
		} else if ( (pp.startsWith("invokevirtual java.lang.StringBuilder.append(") )) {
			if (strB_expr != null) {
				if( !(strB_expr.has_undef_func()) ) {
					String result = ((String)strB_expr.getConcreteValue())
							+ ((String)se0.getConcreteValue());
					if (unimpl) {
						strB_expr.set_undef_func();
					} else {
						strB_expr.setExpr(
								new StringBinaryExpression(strB_expr.getExpr(), 
														Operator.APPEND, se0, result));
					} 
				} else {
					throw_away("StringBuilder has an unimplemented function");
				}
			} else {
				throw_away("no StringBuilder found!");
			}
			

		} else if (pp.startsWith("aload")) {
			String[] spl_str = pp.split("_");
			if (spl_str.length != 2) {
				log.warning("Something wrong happend in StringBuilder.append");
			}
			int position = Integer.parseInt(spl_str[1]);
			
			int offset = sf.getTopPos() - position;
			Object opAttr = sf.getOperandAttr(offset);
			StringBuilderExpression se1 = null;
			if (opAttr != null) {
				if (opAttr instanceof StringBuilderExpression ) {
					se1 = (StringBuilderExpression) opAttr;
				} else {
					throw_away("no StringBuilder found!");
				}
			}
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

	private static String getPrevAppend(INVOKEVIRTUAL ins) {
		//check if we are in a supported instruction
		Instruction p = ins.getPrev();

		if (p.toString().matches("(ldc)|(aload.*)|(bipush)|(iload.*)|" +
										"(fload.*)|(lload.*)|(dload.*)")) {
			return p.getPrev().toString();
		} else if (p.toString().startsWith("getfield")) {
			return p.getPrev().getPrev().toString();
		} else if (p.toString().startsWith("invokevirtual java.lang.String.charAt(I)C") ) {
			p = p.getPrev();
			if (p.toString().matches("(ldc)|(iconst.*)|(bipush)|(iload.*)")) {
				p = p.getPrev();
			} else if (p.toString().startsWith("getfield")){
				p = p.getPrev().getPrev();
			} else throw_away("charAt() second arg: "+p.toString());
			if (p.toString().matches("(ldc)|(aload.*)")) {
				p = p.getPrev();
			} else if (p.toString().startsWith("getfield")){
				p = p.getPrev().getPrev();
			} else throw_away("charAt() first arg: "+p.toString());
			return p.toString();
		} else if (p.toString().matches("invokevirtual java.lang.String.((replace)|(replaceAll)|(replaceFirst)).*") ) {
			p = p.getPrev();
			if (p.toString().matches("(ldc)|(iconst.*)|(bipush)|(iload.*)|(aload.*)")) {
				p = p.getPrev();
			} else if (p.toString().startsWith("getfield")){
				p = p.getPrev().getPrev();
			} else throw_away("replace() third arg: "+p.toString());
			if (p.toString().matches("(ldc)|(iconst.*)|(bipush)|(iload.*)|(aload.*)")) {
				p = p.getPrev();
			} else if (p.toString().startsWith("getfield")){
				p = p.getPrev().getPrev();
			} else throw_away("replace() second arg: "+p.toString());
			if (p.toString().matches("(ldc)|(aload.*)")) {
				p = p.getPrev();
			} else if (p.toString().startsWith("getfield")){
				p = p.getPrev().getPrev();
			} else throw_away("replace() first arg: "+p.toString());
			return p.toString();	
		} else {
//			log.warning("prevInstr "+prevInstr);
			throw_away(p.toString());
			return null;
		}
	}

	public static boolean isStrB_all_impl_op(KernelState ks, ThreadInfo ti, 
														INVOKEVIRTUAL ins) {
		StackFrame sf = ti.getTopFrame();
		StringBuilderExpression se0 = 
					(StringBuilderExpression) sf.getOperandAttr(0);
		
		return (se0 == null) ? !strB_expr.has_undef_func() : 
											!se0.has_undef_func();
	}
	
	private static boolean containsVariables(Expression<?> expr) {
		Set<Variable<?>> variables = new HashSet<Variable<?>>();
		TestSuiteDSE.getVariables(expr, variables);
		log.warning("vars: "+variables);
		return variables.size() <= 0;
	}

	public static void throw_away(String cause) {
		throw new StringBuilderException("StringBuilder: unsupported operation: " + cause);
	}
	
	
}
