/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.graphs.cfg;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.Properties.Criterion;
import de.unisb.cs.st.evosuite.cfg.instrumentation.BranchInstrumentation;
import de.unisb.cs.st.evosuite.cfg.instrumentation.DefUseInstrumentation;
import de.unisb.cs.st.evosuite.cfg.instrumentation.LCSAJsInstrumentation;
import de.unisb.cs.st.evosuite.cfg.instrumentation.MethodInstrumentation;
import de.unisb.cs.st.evosuite.cfg.instrumentation.MutationInstrumentation;
import de.unisb.cs.st.evosuite.cfg.instrumentation.PrimePathInstrumentation;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.testcase.StaticTestCluster;

/**
 * Create a minimized control flow graph for the method and store it. In
 * addition, this adapter also adds instrumentation for branch distance
 * measurement
 * 
 * defUse, concurrency and LCSAJs instrumentation is also added (if the
 * properties are set).
 * 
 * @author Gordon Fraser
 * 
 */
public class CFGMethodAdapter extends MethodVisitor {

	private static Logger logger = LoggerFactory.getLogger(CFGMethodAdapter.class);

	/**
	 * A list of Strings representing method signatures. Methods matching those
	 * signatures are not instrumented and no CFG is generated for them. Except
	 * if some MethodInstrumentation requests it.
	 */
	public static final List<String> EXCLUDE = Arrays.asList("<clinit>",
	                                                         "__STATIC_RESET()V",
	                                                         "__STATIC_RESET");
	/**
	 * The set of all methods which can be used during test case generation This
	 * excludes e.g. synthetic, initializers, private and deprecated methods
	 */
	public static Map<String, Set<String>> methods = new HashMap<String, Set<String>>();

	/**
	 * This is the name + the description of the method. It is more like the
	 * signature and less like the name. The name of the method can be found in
	 * this.plain_name
	 */
	private final String methodName;

	private final MethodVisitor next;
	private final String plain_name;
	private final int access;
	private final String className;

	public CFGMethodAdapter(String className, int access, String name, String desc,
	        String signature, String[] exceptions, MethodVisitor mv) {

		// super(new MethodNode(access, name, desc, signature, exceptions),
		// className,
		// name.replace('/', '.'), null, desc);

		super(Opcodes.ASM4, new MethodNode(access, name, desc, signature, exceptions));

		this.next = mv;
		this.className = className; // .replace('/', '.');
		this.access = access;
		this.methodName = name + desc;
		this.plain_name = name;
	}

	@Override
	public void visitEnd() {

		boolean isExcludedMethod = EXCLUDE.contains(methodName);
		boolean isMainMethod = plain_name.equals("main") && Modifier.isStatic(access);

		List<MethodInstrumentation> instrumentations = new ArrayList<MethodInstrumentation>();
		if (StaticTestCluster.isTargetClassName(className)) {
			if (Properties.CRITERION == Criterion.LCSAJ) {
				instrumentations.add(new LCSAJsInstrumentation());
				instrumentations.add(new BranchInstrumentation());
			} else if (Properties.CRITERION == Criterion.DEFUSE
			        || Properties.CRITERION == Criterion.ALLDEFS) {
				instrumentations.add(new BranchInstrumentation());
				instrumentations.add(new DefUseInstrumentation());
			} else if (Properties.CRITERION == Criterion.ANALYZE) {
				instrumentations.add(new BranchInstrumentation());
				instrumentations.add(new DefUseInstrumentation());
			} else if (Properties.CRITERION == Criterion.PATH) {
				instrumentations.add(new PrimePathInstrumentation());
				instrumentations.add(new BranchInstrumentation());
			} else if (Properties.CRITERION == Criterion.MUTATION
			        || Properties.CRITERION == Criterion.WEAKMUTATION
			        || Properties.CRITERION == Criterion.STRONGMUTATION) {
				instrumentations.add(new BranchInstrumentation());
				instrumentations.add(new MutationInstrumentation());
			} else if (Properties.CRITERION == Criterion.COMP_LCSAJ_BRANCH) {
				instrumentations.add(new LCSAJsInstrumentation());
				instrumentations.add(new BranchInstrumentation());
			} else {
				instrumentations.add(new BranchInstrumentation());
			}
		} else {
			//instrumentations.add(new BranchInstrumentation());
		}

		boolean executeOnMain = false;
		boolean executeOnExcluded = false;

		for (MethodInstrumentation instrumentation : instrumentations) {
			executeOnMain = executeOnMain || instrumentation.executeOnMainMethod();
			executeOnExcluded = executeOnExcluded
			        || instrumentation.executeOnExcludedMethods();
		}

		// super.visitEnd();
		// Generate CFG of method
		MethodNode mn = (MethodNode) mv;

		// Only instrument if the method is (not main and not excluded) or (the
		// MethodInstrumentation wants it anyway)
		if ((!isMainMethod || executeOnMain) && (!isExcludedMethod || executeOnExcluded)
		        && (access & Opcodes.ACC_ABSTRACT) == 0
		        && (access & Opcodes.ACC_NATIVE) == 0) {

			logger.info("Analyzing method " + methodName + " in class " + className);
			if (!methods.containsKey(className))
				methods.put(className, new HashSet<String>());

			// MethodNode mn = new CFGMethodNode((MethodNode)mv);
			// System.out.println("Generating CFG for "+ className+"."+mn.name +
			// " ("+mn.desc +")");

			BytecodeAnalyzer bytecodeAnalyzer = new BytecodeAnalyzer();
			logger.info("Generating CFG for method " + methodName);

			try {

				bytecodeAnalyzer.analyze(className, methodName, mn);
				logger.trace("Method graph for "
				        + className
				        + "."
				        + methodName
				        + " contains "
				        + bytecodeAnalyzer.retrieveCFGGenerator().getRawGraph().vertexSet().size()
				        + " nodes for " + bytecodeAnalyzer.getFrames().length
				        + " instructions");
			} catch (AnalyzerException e) {
				logger.error("Analyzer exception while analyzing " + className + "."
				        + methodName + ": " + e);
				e.printStackTrace();
			}

			// compute Raw and ActualCFG and put both into GraphPool
			bytecodeAnalyzer.retrieveCFGGenerator().registerCFGs();
			logger.info("Created CFG for method " + methodName);

			// add the actual instrumentation
			logger.info("Instrumenting method " + methodName + " in class " + className);
			for (MethodInstrumentation instrumentation : instrumentations)
				instrumentation.analyze(mn, className, methodName, access);

			handleBranchlessMethods();

			String id = className + "." + methodName;
			if (isUsable()) {
				methods.get(className).add(id);
				logger.debug("Counting: " + id);
			}
		}
		mn.accept(next);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.commons.LocalVariablesSorter#visitMaxs(int, int)
	 */
	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		int maxNum = 7;
		super.visitMaxs(Math.max(maxNum, maxStack), maxLocals);
	}

	private void handleBranchlessMethods() {
		String id = className + "." + methodName;
		if (BranchPool.getBranchCountForMethod(className, methodName) == 0) {
			if (isUsable()) {
				logger.debug("Method has no branches: " + id);
				BranchPool.addBranchlessMethod(className, id);
			}
		}
	}

	/**
	 * See description of CFGMethodAdapter.EXCLUDE
	 * 
	 * @return
	 */
	private boolean isUsable() {
		return !((this.access & Opcodes.ACC_SYNTHETIC) > 0
		        || (this.access & Opcodes.ACC_BRIDGE) > 0 || (this.access & Opcodes.ACC_NATIVE) > 0)
		        && !methodName.contains("<clinit>")
		        && !(methodName.contains("<init>") && (access & Opcodes.ACC_PRIVATE) == Opcodes.ACC_PRIVATE)
		        && (Properties.USE_DEPRECATED || (access & Opcodes.ACC_DEPRECATED) != Opcodes.ACC_DEPRECATED);
	}

	/**
	 * Returns a set with all unique methodNames of methods.
	 * 
	 * @return A set with all unique methodNames of methods.
	 */
	public static Set<String> getMethods(String className) {
		if (!methods.containsKey(className))
			return new HashSet<String>();

		return methods.get(className);
	}

	/**
	 * Returns a set with all unique methodNames of methods.
	 * 
	 * @return A set with all unique methodNames of methods.
	 */
	public static Set<String> getMethodsPrefix(String className) {
		Set<String> matchingMethods = new HashSet<String>();

		for (String name : methods.keySet()) {
			if (name.startsWith(className)) {
				matchingMethods.addAll(methods.get(name));
			}
		}

		return matchingMethods;
	}

	/**
	 * Returns a set with all unique methodNames of methods.
	 * 
	 * @return A set with all unique methodNames of methods.
	 */
	public static int getNumMethodsPrefix(String className) {
		int num = 0;

		for (String name : methods.keySet()) {
			if (name.startsWith(className)) {
				num += methods.get(name).size();
			}
		}

		return num;
	}

	/**
	 * Returns a set with all unique methodNames of methods.
	 * 
	 * @return A set with all unique methodNames of methods.
	 */
	public static int getNumMethodsMemberClasses(String className) {
		int num = 0;

		for (String name : methods.keySet()) {
			if (name.equals(className) || name.startsWith(className + "$")) {
				num += methods.get(name).size();
			}
		}

		return num;
	}
}
