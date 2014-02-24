/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 * 
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.instrumentation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.evosuite.setup.TestCluster;
import org.evosuite.utils.Utils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Duplicate static initializers in methods, such that we can explicitly restore
 * the initial state of classes.
 * 
 * @author Gordon Fraser
 */
public class StaticInitializationClassAdapter extends ClassVisitor {

	private final String className;

	/** Constant <code>static_classes</code> */
	public static List<String> static_classes = new ArrayList<String>();

	private static Logger logger = LoggerFactory
			.getLogger(StaticInitializationClassAdapter.class);

	private boolean isInterface = false;

	private boolean clinit_found = false;

	private final List<String> finalFields = new ArrayList<String>();

	/**
	 * <p>
	 * Constructor for StaticInitializationClassAdapter.
	 * </p>
	 * 
	 * @param visitor
	 *            a {@link org.objectweb.asm.ClassVisitor} object.
	 * @param className
	 *            a {@link java.lang.String} object.
	 */
	public StaticInitializationClassAdapter(ClassVisitor visitor,
			String className) {
		super(Opcodes.ASM4, visitor);
		this.className = className;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		isInterface = ((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE);
	}

	private static class StaticField {
		int access;
		String name;
		String desc;
		String signature;
		Object value;
	}

	private List<StaticField> static_fields = new LinkedList<StaticField>();

	/* (non-Javadoc)
	 * @see org.objectweb.asm.ClassAdapter#visitField(int, java.lang.String, java.lang.String, java.lang.String, java.lang.Object)
	 */
	/** {@inheritDoc} */
	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {

		if ((access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
			StaticField staticField = new StaticField();
			staticField.access = access;
			staticField.name = name;
			staticField.desc = desc;
			staticField.signature = signature;
			staticField.value = value;
			static_fields.add(staticField);
		}

		if ((access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL)
			finalFields.add(name);

		return super.visitField(access, name, desc, signature, value);
	}

	/** {@inheritDoc} */
	@Override
	public MethodVisitor visitMethod(int methodAccess, String name,
			String descriptor, String signature, String[] exceptions) {

		MethodVisitor mv = super.visitMethod(methodAccess, name, descriptor,
				signature, exceptions);
		if (name.equals("<clinit>") && !isInterface) {
			clinit_found = true;
			logger.info("Found static initializer in class " + className);
			MethodVisitor mv2 = new RemoveFinalMethodAdapter(className,
					super.visitMethod(methodAccess | Opcodes.ACC_PUBLIC
							| Opcodes.ACC_STATIC, "__STATIC_RESET", descriptor,
							signature, exceptions), finalFields);
			registerStaticResetMethod();
			return new MultiMethodVisitor(mv2, mv);
		}
		return mv;
	}

	public void registerStaticResetMethod() {
		static_classes.add(Utils.getClassNameFromResourcePath(className));
		TestCluster.registerStaticInitializer(className.replace("/", "."));
	}

	@Override
	public void visitEnd() {
		if (!clinit_found && !static_fields.isEmpty() && !isInterface) {
			// create brand new __STATIC_RESET
			createEmptyStaticReset();
			registerStaticResetMethod();
		}
		// add initial field updates to __STATIC_RESET
		super.visitEnd();
	}

	private void createEmptyStaticReset() {
		logger.info("Creating brand-new static initializer in class "
				+ className);
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC
				| Opcodes.ACC_STATIC, "__STATIC_RESET", "()V", null, null);
		mv.visitCode();
		for (StaticField staticField : static_fields) {
			logger.info("Adding bytecode for initializing field "
					+ staticField.name);

			Type type = Type.getType(staticField.desc);
			switch (type.getSort()) {
			case Type.BOOLEAN:
			case Type.BYTE:
			case Type.CHAR:
			case Type.SHORT:
			case Type.INT:
				mv.visitInsn(Opcodes.ICONST_0);
				break;
			case Type.FLOAT:
				mv.visitInsn(Opcodes.FCONST_0);
				break;
			case Type.LONG:
				mv.visitInsn(Opcodes.LCONST_0);
				break;
			case Type.DOUBLE:
				mv.visitInsn(Opcodes.DCONST_0);
				break;
			case Type.ARRAY:
			case Type.OBJECT:
				mv.visitInsn(Opcodes.ACONST_NULL);
				break;
			}
			mv.visitFieldInsn(Opcodes.PUTSTATIC, className, staticField.name,
					staticField.desc);
		}
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		
	}
}
