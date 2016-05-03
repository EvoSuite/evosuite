/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with EvoSuite. If
 * not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.regression;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ResourceList;
import org.evosuite.result.TestGenerationResultBuilder;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author sina
 * 
 */
public class RegressionClassDiff {

  protected static final Logger logger = LoggerFactory.getLogger(RegressionClassDiff.class);

  /*
   * Is the underlying code of two java classes, one on ProjectCP and one on regression_cp,
   * different?
   */
  public static boolean areDifferent(Class<?> original_class, Class<?> regression_class) {

    String path = Properties.TARGET_CLASS.replace('.', '/') + ".class";
    InputStream isO = original_class.getClassLoader().getResourceAsStream(path);
    InputStream isR = regression_class.getClassLoader().getResourceAsStream(path);

    boolean different = false;

    Map<String, List<Integer>> methodInstructionsA = RegressionClassDiff.getClassInstructions(isO);
    Map<String, List<Integer>> methodInstructionsB = RegressionClassDiff.getClassInstructions(isR);

    int sizeA = methodInstructionsA.size();
    int sizeB = methodInstructionsB.size();

    // If the size of the lists differs, they have different methods and are obviously different
    if (sizeA != sizeB)
      different = true;
    else
      for (Entry<String, List<Integer>> e : methodInstructionsA.entrySet()) {
        List<Integer> miA = e.getValue();
        List<Integer> miB = methodInstructionsB.get(e.getKey());
        // using .equals as the order of instructions matters
        if (miB == null || !miA.equals(miB)) {
          different = true;
          break;
        }

      }

    // logger.warn("Were not equal? {}", different);

    if (!different) {
      logger.debug("class {} was equal on both versions", Properties.TARGET_CLASS);
    } else {
      logger.debug("Different Classes: classA {}", methodInstructionsA);
      logger.debug("Different Classes: classB {}", methodInstructionsB);
    }

    return different;
  }

  /*
   * Get bytecode instructions for the class based on the following format: Method -> List of
   * instructions
   */
  public static Map<String, List<Integer>> getClassInstructions(InputStream classAsInputStream) {
    HashMap<String, List<Integer>> methodInstructionsMap = new HashMap<>();
    try {
      ClassReader reader = new ClassReader(classAsInputStream);
      ClassNode classNode = new ClassNode();
      reader.accept(classNode, 0);
      @SuppressWarnings("unchecked")
      final List<MethodNode> methods = classNode.methods;
      Printer printer = new Textifier();
      TraceMethodVisitor mp = new TraceMethodVisitor(printer);
      for (MethodNode m : methods) {
        List<Integer> instructions = new ArrayList<>();

        InsnList inList = m.instructions;
        // System.out.println(m.name);
        int[] methodInstructions = new int[inList.size()];
        for (int i = 0; i < inList.size(); i++) {
          int op = inList.get(i).getOpcode();
          methodInstructions[i] = op;
          AbstractInsnNode insn = inList.get(i);
          insn.accept(mp);

          // Uncomment the following comment block to print the bytecode
          // instructions
          // StringWriter sw = new StringWriter();
          // printer.print(new PrintWriter(sw));
          // printer.getText().clear();
          // logger.warn("{} -> {}", sw.toString(), op);
          if (op != -1)
            instructions.add(op);
        }
        methodInstructionsMap.put(m.name, instructions);
      }
    } catch (IOException e) {
      // Will fail if ClassReader fails
      e.printStackTrace();
    }
    return methodInstructionsMap;
  }

}
