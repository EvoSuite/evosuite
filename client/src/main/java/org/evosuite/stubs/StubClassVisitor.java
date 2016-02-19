/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.stubs;

import org.evosuite.PackageInfo;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public class StubClassVisitor extends ClassVisitor {

	private String className;
	
	public StubClassVisitor(ClassVisitor parent, String className) {
		super(Opcodes.ASM5, parent);
		this.className = className;
	}
	
	/**
	 * The stub constructor does nothing
	 * 
	 * @param mg
	 * @param m
	 */
	private void createConstructor(GeneratorAdapter mg, Method m) {
		mg.loadThis();
		mg.invokeConstructor(Type.getType(Object.class), m);
		mg.returnValue();
		mg.endMethod();		
	}
	
	private String getTypeName(Type t) {
		switch(t.getSort()) {
		case Type.VOID:
			return "Void";
		case Type.BOOLEAN:
			return "Boolean";
		case Type.BYTE:
			return "Byte";
		case Type.CHAR:
			return "Char";
		case Type.DOUBLE:
			return "Double";
		case Type.FLOAT:
			return "Float";
		case Type.INT:
			return "Int";
		case Type.LONG:
			return "Long";
		case Type.SHORT:
			return "Short";
		case Type.ARRAY:
			return getTypeName(t.getElementType()) +"Array";
		case Type.OBJECT:
			String className = t.getClassName();
			if(className.equals("java.lang.String"))
				return "String";
			else
				return "Object";
		}
		
		return "";
	}
	
	private String getReturnTypeDesc(Type t) {
		if(t.getSort() == Type.OBJECT) {
			return "Ljava/lang/Object;";
		} else if(t.getSort() == Type.ARRAY) {
			return "["+getReturnTypeDesc(t.getElementType());
		} else
			return t.getDescriptor();
	}
	
	private void insertReturnCast(GeneratorAdapter mg, Method m) {
		if(m.getReturnType().getSort() == Type.OBJECT) {
			mg.checkCast(m.getReturnType());
		} else if(m.getReturnType().getSort() == Type.ARRAY) {
			if(m.getReturnType().getElementType().getSort() == Type.OBJECT) {
				// TODO: String? Other arrays?
				try {
					java.lang.reflect.Method copyMethod = System.class.getMethod("arraycopy", new Class<?>[] {Object.class, int.class, Object.class, int.class, int.class} );

					// Object 1
					mg.dup(); // O1, O1
					mg.arrayLength(); // O1, O1.length
					int arrayLengthPos = mg.newLocal(Type.INT_TYPE);
					mg.storeLocal(arrayLengthPos);

					mg.loadLocal(arrayLengthPos);
					mg.newArray(m.getReturnType().getElementType());
					int newArrayPos = mg.newLocal(m.getReturnType());
					mg.storeLocal(newArrayPos);

					
					mg.push(0); // O1, 0
					mg.loadLocal(newArrayPos); // O1, 0, O2
					mg.push(0); // O1, 0, O2, 0
					mg.loadLocal(arrayLengthPos); // O1, 0, O2, 0, O1.length					
					mg.invokeStatic(Type.getType(System.class), Method.getMethod(copyMethod));
					mg.loadLocal(newArrayPos);
					mg.checkCast(m.getReturnType());
				} catch(Exception e) {
					System.out.println("Screw that");
				}
				// mg.checkCast(m.getReturnType());
			}
		}
	}
	
	/**
	 * Stubbed methods forward the query to the central Stubs class
	 * and return whatever that class tells it to return
	 * 
	 * @param mg
	 * @param m
	 */
	private void createMethod(GeneratorAdapter mg, Method m) {
		String methodName = "getReturnValue"+getTypeName(m.getReturnType());
		String desc = "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)" + getReturnTypeDesc(m.getReturnType());
		mg.push(className);
		mg.push(m.getName() + m.getDescriptor());
		mg.loadArgArray();
		Type owner = Type.getType(PackageInfo.getNameWithSlash(Stubs.class));
		Method method = new Method(methodName, desc);
		mg.invokeStatic(owner, method);
		insertReturnCast(mg, m);
		mg.returnValue();
		mg.endMethod();		
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		
		Method m = new Method(name, desc);
		GeneratorAdapter mg = new GeneratorAdapter(access, m, signature, new Type[0], this.cv);
		mg.visitCode();
		
		if(name.equals("<init>"))
			createConstructor(mg, m);
		else
			createMethod(mg, m);
		
		
		return null;
	}
}
