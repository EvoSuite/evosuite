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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ResourceList;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.result.TestGenerationResultBuilder;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
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
  public static boolean differentAcrossClassloaders(String classFullPath) {

    InputStream originalClassIS =
        ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT())
            .getClassAsStream(classFullPath);
    InputStream regressionClassIS = ResourceList
        .getInstance(TestGenerationContext.getInstance().getRegressionClassLoaderForSUT())
        .getClassAsStream(classFullPath);

    boolean different = false;

    Map<String, List<Integer>> methodInstructionsA =
        RegressionClassDiff.getClassInstructions(originalClassIS);
    Map<String, List<Integer>> methodInstructionsB =
        RegressionClassDiff.getClassInstructions(regressionClassIS);

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
      logger.warn("class {} was equal on both versions", classFullPath);
    } else {
      logger.warn("class {} has been modified between the two versions provided", classFullPath);
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

        String mathodID = m.name + ": " + m.desc;
        System.out.println(mathodID);
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
          // System.out.println(sw.toString());
          // logger.warn("{} -> {}", sw.toString(), op);
          if (op != -1)
            instructions.add(op);
        }
        methodInstructionsMap.put(mathodID, instructions);
      }
    } catch (IOException e) {
      // Will fail if ClassReader fails
      e.printStackTrace();
    }
    return methodInstructionsMap;
  }

  public static boolean sameCFG() {
    Collection<Branch> branchesOriginal = BranchPool
        .getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllBranches();
    Collection<Branch> branchesRegression =
        BranchPool.getInstance(TestGenerationContext.getInstance().getRegressionClassLoaderForSUT())
            .getAllBranches();

    if (branchesOriginal.size() != branchesRegression.size()) {
      logger.error("Different number of branches between two versions: {} vs {}", branchesOriginal.size(), branchesRegression.size());
      return false;
    }

    Iterator<Branch> branchesOriginalIterator = branchesOriginal.iterator();
    Iterator<Branch> branchesRegressionIterator = branchesRegression.iterator();

    boolean sameBranches = true;

    while (branchesOriginalIterator.hasNext()) {
      Branch bOrig = branchesOriginalIterator.next();
      Branch bReg = branchesRegressionIterator.next();

      int bOrigOpcode = bOrig.getInstruction().getASMNode().getOpcode();
      int bRegOpcode = bReg.getInstruction().getASMNode().getOpcode();

      // Are branches from the same family of branches?
      if (RegressionClassDiff.getBranchFamily(bOrigOpcode) != RegressionClassDiff
          .getBranchFamily(bRegOpcode)) {
        logger.error("Different family found between branches: {} vs {}", bOrigOpcode, bRegOpcode);
        sameBranches = false;
        break;
      }



    }
    return sameBranches;
  }

  public static String getBranchFamily(int opcode) {
    // The default family is the opcode itself
    // Admittedly we could've use ints/enums for performance, but strings should be interned anyway
    String family = "" + opcode;
    switch (opcode) {
      // copmpare int with zero
      case Opcodes.IFEQ:
      case Opcodes.IFNE:
      case Opcodes.IFLT:
      case Opcodes.IFGE:
      case Opcodes.IFGT:
      case Opcodes.IFLE:
        family = "int_zero";
        break;
      // copmpare int with int
      case Opcodes.IF_ICMPEQ:
      case Opcodes.IF_ICMPNE:
      case Opcodes.IF_ICMPLT:
      case Opcodes.IF_ICMPGE:
      case Opcodes.IF_ICMPGT:
      case Opcodes.IF_ICMPLE:
        family = "int_int";
        break;
      // copmpare reference with reference
      case Opcodes.IF_ACMPEQ:
      case Opcodes.IF_ACMPNE:
        family = "ref_ref";
        break;
      // compare reference with null
      case Opcodes.IFNULL:
      case Opcodes.IFNONNULL:
        family = "ref_null";
        break;

    }
    return family;
  }

}
