/*
 * Copyright (C) 2009 Saarland University
 * 
 * This file is part of Javalanche.
 * 
 * Javalanche is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Javalanche is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with Javalanche.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.cfg;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


import de.unisb.cs.st.javalanche.mutation.javaagent.MutationForRun;
import de.unisb.cs.st.javalanche.mutation.javaagent.classFileTransfomer.mutationDecision.Excludes;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

public class CFGClassAdapter extends ClassAdapter {
	
	private static Logger logger = Logger.getLogger(CFGClassAdapter.class);
	
	private String className;
	private Excludes e = Excludes.getInstance();
	
	private boolean exclude;

	
	//private List<String> instrument;
	
//	public CFGClassAdapter(ClassVisitor visitor, String className, List<String> instrument_methods) {
	public CFGClassAdapter(ClassVisitor visitor, String className) {
		super(visitor);
//		instrument = instrument_methods;
		this.className = className;
		String classNameWithDots = className.replace('/', '.');
		if (e.shouldExclude(classNameWithDots)) {
			exclude = true;
		} else {
			exclude = false;
		}
		
	}


	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
//		if((access & Opcodes.ACC_PRIVATE) == 0 && (access & Opcodes.ACC_PROTECTED) == 0 && (access & Opcodes.ACC_PUBLIC) == 0) {
		if((access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
			access = access | Opcodes.ACC_PUBLIC;
			access = access & ~Opcodes.ACC_PROTECTED;
			logger.info("Setting class to public: "+className);
		}		
		super.visit(version, access, name, signature, superName, interfaces);
	}
	
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if((access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
			access = access | Opcodes.ACC_PUBLIC;
			access = access & ~Opcodes.ACC_PROTECTED;
			//System.out.println("Setting field to public: "+className);
		}
		
		return super.visitField(access, name, desc, signature, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.ClassAdapter#visitMethod(int, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String[])
	 */
	public MethodVisitor visitMethod(int methodAccess, String name,
			String descriptor, String signature, String[] exceptions) {

		if (!exclude) {
			if((methodAccess & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
				methodAccess = methodAccess | Opcodes.ACC_PUBLIC;
				methodAccess = methodAccess & ~Opcodes.ACC_PROTECTED;
			}
		}
		
		MethodVisitor mv = super.visitMethod(methodAccess, name, descriptor,
				signature, exceptions);
//		if (!exclude && instrument.contains(name+descriptor)) {
		if( (methodAccess & Opcodes.ACC_SYNTHETIC) > 0 || (methodAccess & Opcodes.ACC_BRIDGE) > 0) {
			return mv;
		}

		if (!exclude) {
			//mv = new BranchMethodAdapter(className, methodAccess, name, descriptor, signature, exceptions, mv);
			logger.info("Instrumenting: "+className+"."+name);
			if(false) {
				MutationForRun mm = MutationForRun.getFromDefaultLocation();
				mv = new CFGMethodAdapter(className, methodAccess, name, descriptor, signature, exceptions, mv, mm.getMutations());
			}
			else {
				mv = new CFGMethodAdapter(className, methodAccess, name, descriptor, signature, exceptions, mv, new ArrayList<Mutation>());
			}
				
		}
		return mv;
	}
}
