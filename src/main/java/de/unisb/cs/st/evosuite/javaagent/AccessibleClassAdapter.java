/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.javaagent;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import de.unisb.cs.st.evosuite.Properties;

/**
 * Turn protected / default access rights to public access rights.
 * This is necessary because EvoSuite is not in the same package as the UUT,
 * but the tests will reside in the same package.
 * 
 * @author Gordon Fraser
 *
 */
public class AccessibleClassAdapter extends ClassAdapter {

	private boolean exclude = false;
	
	/**
	 * @param Parent class visitor
	 */
	public AccessibleClassAdapter(ClassVisitor cv, String className) {
		super(cv);
		className = className.replace('/', '.');
		String packageName = className.substring(0, className.lastIndexOf('.'));
		if(!packageName.equals(Properties.CLASS_PREFIX)) {
			exclude = true;
		}
	}
	
	/**
	 * Change subclasses to public
	 */
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		if(!exclude && (access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
			access = access | Opcodes.ACC_PUBLIC;
			access = access & ~Opcodes.ACC_PROTECTED;
		}	
		super.visit(version, access, name, signature, superName, interfaces);
	}

	/**
	 * Change fields to public
	 */
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if(!exclude && (access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
			access = access | Opcodes.ACC_PUBLIC;
			access = access & ~Opcodes.ACC_PROTECTED;
			//System.out.println("Setting field to public: "+name);
		}
		
		return super.visitField(access, name, desc, signature, value);
	}
	
	/**
	 * Change methods to public
	 */
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, final String[] exceptions) {
		
		if(!exclude && (access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE) {
			access = access | Opcodes.ACC_PUBLIC;
			access = access & ~Opcodes.ACC_PROTECTED;
		}
		
		MethodVisitor mv = super.visitMethod(access, name, desc, signature,
				exceptions);
		return mv;
	}
}
