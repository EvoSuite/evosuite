package org.evosuite.symbolic.instrument;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;

/*
    This class is taken and adapted from the DSC tool developed by Christoph Csallner.
    Link at :
    http://ranger.uta.edu/~csallner/dsc/index.html
 */


/**
 * The main instrumentation class is {@link ConcolicMethodAdapter}
 * 
 * @author csallner@uta.edu (Christoph Csallner)
 */
final class ConcolicClassAdapter extends ClassVisitor {

  private final String className;
  
  ConcolicClassAdapter(ClassVisitor cv, String className) {
    super(Opcodes.ASM4, cv);
    this.className = className;
  }
  
  @Override
  public MethodVisitor visitMethod(
      int access, 
      String methName, 
      String methDesc,
      String methSignGeneric, 
      String[] exceptions) 
  {
    MethodVisitor mv;
    mv = cv.visitMethod(access, methName, methDesc, methSignGeneric, exceptions);
    // Added to handle Java 7
    mv = new JSRInlinerAdapter(mv, access, methName, methDesc, methSignGeneric, exceptions);
    if (mv != null) {
      mv = new ConcolicMethodAdapter(mv, access, className, methName, methDesc);
    }
    return mv;
  }
  
}
