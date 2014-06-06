package org.evosuite.instrumentation;

import java.util.List;

import org.evosuite.instrumentation.CreateClassResetClassAdapter.StaticField;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class CreateClassResetMethodAdapter extends MethodVisitor {

	private final List<StaticField> staticFields;

	private final String className;

	private final List<String> finalFields;

	public CreateClassResetMethodAdapter(MethodVisitor mv, String className,
			List<StaticField> staticFields, List<String> finalFields) {
		super(Opcodes.ASM4, mv);
		this.className = className;
		this.staticFields = staticFields;
		this.finalFields = finalFields;
	}

	@Override
	public void visitCode() {
		super.visitCode();
		for (StaticField staticField : staticFields) {

			if (!finalFields.contains(staticField.name)) {

				if (staticField.value != null) {
					mv.visitLdcInsn(staticField.value);
				} else {
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
				}
				mv.visitFieldInsn(Opcodes.PUTSTATIC, className,
						staticField.name, staticField.desc);
			}
		}

	}

}
