package org.evosuite.instrumentation;

import org.evosuite.idNaming.VariableNameCollector;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gordon on 22/12/2015.
 */
public class MethodSignatureCollector extends MethodVisitor {

    private String className;

    private String methodName;

    private int numParams;

    private int startIndex;

    private int endIndex;

    private boolean isStatic;

    private static final Logger logger = LoggerFactory.getLogger(MethodSignatureCollector.class);

    public MethodSignatureCollector(MethodVisitor mv, String className, String methodName, String desc, boolean isStatic) {
        super(Opcodes.ASM5, mv);
        this.className = className;
        this.methodName = methodName;
        numParams = Type.getArgumentTypes(desc).length;
        this.isStatic = isStatic;
        if(!isStatic) {
            startIndex = 1;
        }
        endIndex = startIndex + numParams;
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.commons.LocalVariablesSorter#visitLocalVariable(java.lang.String, java.lang.String, java.lang.String, org.objectweb.asm.Label, org.object
     */
    @Override
    public void visitLocalVariable(String name, String desc, String signature,
                                   Label start, Label end, int index) {
        if(index >= startIndex && index < endIndex) {
            logger.debug("Collecting name for parameter {} of method {}: {}", index, methodName, name);
            VariableNameCollector.getInstance().addParameterName(className.replace('/', '.'), methodName+desc, index, name);
        }

        super.visitLocalVariable(name, desc, signature, start, end, index);
    }
}
