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
/**
 * 
 */
package org.evosuite.setup;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.branch.Branch;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.dataflow.DefUsePool;
import org.evosuite.coverage.lcsaj.LCSAJPool;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.BytecodeInstructionPool;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.instrumentation.LinePool;
import org.evosuite.regression.RegressionSuiteFitness;
import org.evosuite.rmi.ClientServices;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.utils.ArrayUtil;
import org.junit.Test;
import org.junit.runners.Suite;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs static analysis before everything else initializes
 * 
 * @author Gordon Fraser
 * 
 */
public class DependencyAnalysis {

	private static Logger logger = LoggerFactory.getLogger(DependencyAnalysis.class);
	
	private static Map<String, ClassNode> classCache = new LinkedHashMap<String, ClassNode>();

	private static CallTree callTree = null;

	private static InheritanceTree inheritanceTree = null;

	/**
	 * @return the inheritanceTree
	 */
	public static InheritanceTree getInheritanceTree() {
		return inheritanceTree;
	}

	/**
	 * Start analysis from target class
	 * 
	 * @param className
	 */
	public static void analyze(String className, List<String> classPath)
	        throws RuntimeException, ClassNotFoundException {
		
		logger.debug("Calculate inheritance hierarchy");		
		inheritanceTree = InheritanceTreeGenerator.createFromClassPath(classPath);
		InheritanceTreeGenerator.gatherStatistics(inheritanceTree);
		if(!inheritanceTree.hasClass(Properties.TARGET_CLASS)) {
			throw new ClassNotFoundException("Target class not found in inheritance tree");
		}

		logger.debug("Calculate call tree");
		callTree = CallTreeGenerator.analyze(className);
		loadCallTreeClasses();
		
		// TODO: Need to make sure that all classes in calltree are instrumented

		logger.debug("Update call tree with calls to overridden methods");
		CallTreeGenerator.update(callTree, inheritanceTree);

		logger.debug("Create test cluster");
		TestClusterGenerator clusterGenerator = new TestClusterGenerator();
		clusterGenerator.generateCluster(className, inheritanceTree, callTree);
		
		gatherStatistics();
	}

	private static void loadCallTreeClasses() {
		for(String className : callTree.getClasses()) {
			if(className.startsWith(Properties.TARGET_CLASS+"$")) {
				try {
					Class.forName(className, true, TestGenerationContext.getInstance().getClassLoaderForSUT());
				} catch(ClassNotFoundException e) {
					logger.debug("Error loading "+className+ ": "+e);
				}
			}
		}
	}
	
	public static CallTree getCallTree() {
		return callTree;
	}

	/**
	 * Determine if this class contains JUnit tests
	 * 
	 * @param className
	 * @return
	 */
	private static boolean isTest(String className) {
		// TODO-JRO Identifying tests should be done differently:
		// If the class either contains methods
		// annotated with @Test (> JUnit 4.0)
		// or contains Test or Suite in it's inheritance structure
		try {
			Class<?> clazz = Class.forName(className);
			Class<?> superClazz = clazz.getSuperclass();
			while (!superClazz.equals(Object.class)) {
				if (superClazz.equals(Suite.class))
					return true;
				if (superClazz.equals(Test.class))
					return true;

				superClazz = clazz.getSuperclass();
			}
			for (Method method : clazz.getMethods()) {
				if (method.isAnnotationPresent(Test.class)) {
					return true;
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO
		}
		return false;
	}

	/**
	 * Determine if the given class is the target class
	 * 
	 * @param className
	 * @return
	 */
	public static boolean isTargetClassName(String className) {
		if (!Properties.TARGET_CLASS_PREFIX.isEmpty()
		        && className.startsWith(Properties.TARGET_CLASS_PREFIX)) {
			// exclude existing tests from the target project
			return !isTest(className);
		}
		if (className.equals(Properties.TARGET_CLASS)
		        || className.startsWith(Properties.TARGET_CLASS + "$")) {
			return true;
		}

		return false;

	}

	/**
	 * Determine if the given class should be analyzed or instrumented
	 * 
	 * @param className
	 * @return
	 */
	public static boolean shouldAnalyze(String className) {
		// Always analyze if it is a target class
		if (isTargetClassName(className))
			return true;

		// Also analyze if it is a superclass and instrument_parent = true
		if (Properties.INSTRUMENT_PARENT) {
			if (inheritanceTree.getSuperclasses(Properties.TARGET_CLASS).contains(className))
				return true;
		}

		// Also analyze if it is in the calltree and we are considering the context
		if (Properties.INSTRUMENT_CONTEXT || ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)) {
			if (callTree.isCalledClass(className)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Determine if the given method should be instrumented
	 * 
	 * @param className
	 * @param methodName
	 * @return
	 */
	public static boolean shouldInstrument(String className, String methodName) {
		// Always analyze if it is a target class
		if (isTargetClassName(className))
			return true;

		// Also analyze if it is a superclass and instrument_parent = true
		if (Properties.INSTRUMENT_PARENT) {
			if (inheritanceTree.getSuperclasses(Properties.TARGET_CLASS).contains(className))
				return true;
		}

		// Also analyze if it is in the calltree and we are considering the context
		if (Properties.INSTRUMENT_CONTEXT) {
			if (callTree.isCalledMethod(className, methodName))
				return true;
		}

		return false;
	}

	public static ClassNode getClassNode(String className) {
		if (!classCache.containsKey(className)) {
			try {
				classCache.put(className, loadClassNode(className));
			} catch (IOException e) {
				classCache.put(className, null);
			}
		}

		return classCache.get(className);

	}

	public static Collection<ClassNode> getAllClassNodes() {
		return classCache.values();
	}

	private static ClassNode loadClassNode(String className) throws IOException {
		ClassReader reader = new ClassReader(className);

		ClassNode cn = new ClassNode();
		reader.accept(cn, ClassReader.SKIP_FRAMES); // | ClassReader.SKIP_DEBUG);	
		return cn;
	}
	
	private static void gatherStatistics() {
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Predicates, BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchCounter());
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.CoveredBranchesBitString, (BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchCounter()) * 2);
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Branches, (BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchCounter()) * 2);
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Branchless_Methods, BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchlessMethods().size());
		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Total_Methods, CFGMethodAdapter.getNumMethods(TestGenerationContext.getInstance().getClassLoaderForSUT()));

		ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Lines, LinePool.getNumLines());

		for (Properties.Criterion pc : Properties.CRITERION) {
		    switch(pc) {
        		case DEFUSE:
        		case ALLDEFS:
        			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Definitions, DefUsePool.getDefCounter());
        			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Uses, DefUsePool.getUseCounter());
        			break;

        		case LCSAJ:
        			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.LCSAJs, LCSAJPool.getLCSAJsPerClass(Properties.TARGET_CLASS));
        			break;

        		case WEAKMUTATION:
        		case STRONGMUTATION:
        		case MUTATION:
        			ClientServices.getInstance().getClientNode().trackOutputVariable(RuntimeVariable.Mutants, MutationPool.getMutantCounter());
        			break;

        		default:
        			break;
    		}
		}
	}
	
	
	
	
	/*
	public static void doJdiff(String className) {
		String originalJRCfilename = "original.jrc";
		String regressionJRCfilename = "regression.jrc";

		File originalJRC = new File(originalJRCfilename);
		File regressionJRC = new File(regressionJRCfilename);

		try {
			FileUtils.writeStringToFile(originalJRC, buildJRC(className, false), false);
			FileUtils.writeStringToFile(regressionJRC, buildJRC(className, true), false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		 * System.out .println(
		 * "-------------------------\n\n\n>>> Using regression strategy of type: "
		 * + Properties.REGRESSION_ANALYSIS_BRANCHDISTANCE +
		 * " <<<\n\n\n-------------------------");
		 
		String[] argv = new String[] { "jdiff.main.DefaultJdiffDriver", "-if", "-l",
		        "--", "-q", "-u", "-m", "5", 
		        // "example/joda1.jrc", "example/joda2.jrc"
		        originalJRCfilename, regressionJRCfilename

		};
		logger.warn("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		Map<String, Map<Integer, Integer>> jdiffMethodNodePairOffsets = new HashMap<String, Map<Integer, Integer>>();
		
		
		DefaultJdiffDriver.oldSM = System.getSecurityManager();
		try{
		DefaultJdiffDriver d = new DefaultJdiffDriver(argv);
		
		
		for (Entry<String, List> x : d.npMethods.entrySet()) {
			logger.warn("M: " + x.getKey() + " | size: " + x.getValue().size());
			Map<Integer, Integer> nodePairOffsets = new TreeMap<Integer, Integer>();
			for (RegressionNodePair rnp : (List<RegressionNodePair>) x.getValue()) {
				nodePairOffsets.put(rnp.oldNode.bytecodeOffset,
				                    rnp.newNode.bytecodeOffset);
			}

			// /////////////////////////////// JDIFF
			jdiffMethodNodePairOffsets.put(x.getKey(), nodePairOffsets);
		}

		logger.warn(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Methods size:"
		        + d.npMethods.size());
		DefaultJdiffDriver.numMatchedMethods = d.npMethods.size();
		logger.warn("******* numAddedMethods: "
				+ DefaultJdiffDriver.numAddedMethods + " | numDeletedMethods: "
				+ DefaultJdiffDriver.numDeletedMethods + " | numChangedMethods: "
				+ DefaultJdiffDriver.numChangedMethods + " | numChangedmethodsAcceptable: "
				+ DefaultJdiffDriver.numChangedmethodsAcceptable + " | numSameMethods: "
				+ DefaultJdiffDriver.numSameMethods + " | numMatchedMethods: "
				+ DefaultJdiffDriver.numMatchedMethods);

		if(DefaultJdiffDriver.numAddedMethods == 0 && DefaultJdiffDriver.numDeletedMethods == 0 && DefaultJdiffDriver.numChangedmethodsAcceptable > 0 && DefaultJdiffDriver.numFailedMethods == 0)
			DefaultJdiffDriver.successful = true;
		logger.warn("Jdiff: " + (DefaultJdiffDriver.successful?"Successful":"Failed"));
		} catch(Throwable t){
			DefaultJdiffDriver.successful = false;
			System.out.println("Failed to run the Jdiff Driver: " + t);
		}
		
		RegressionSearchListener.jdiffReport += "Jdiff report: " + (DefaultJdiffDriver.successful?"Successful":"Failed")
				+ " | " + " numAddedMethods: "
				+ DefaultJdiffDriver.numAddedMethods + " | numDeletedMethods: "
				+ DefaultJdiffDriver.numDeletedMethods + " | numChangedMethods: "
				+ DefaultJdiffDriver.numChangedMethods + " | numChangedmethodsAcceptable: "
				+ DefaultJdiffDriver.numChangedmethodsAcceptable + " | numSameMethods: "
				+ DefaultJdiffDriver.numSameMethods + " | numFailedMethods: "
						+ DefaultJdiffDriver.numFailedMethods + " | numMatchedMethods:" + DefaultJdiffDriver.numMatchedMethods;


		Map<String, List<Integer>> methodBasicblockOriginal = new HashMap<String, List<Integer>>();

		Set<String> allMethods = MethodPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getMethods();

		// Method <-> list of Basic Block offsets for Original
		for (String methodName : allMethods) {
			List<BytecodeInstruction> instructions = BytecodeInstructionPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getInstructionsIn(className,
			                                                                                                                                       methodName.replace(className,
			                                                                                                                                                          "").substring(1));
			List<Integer> bbs = new ArrayList<Integer>();
			if (instructions != null)
				for (BytecodeInstruction bi : instructions) {
					if (bi.getBasicBlock() == null
					        || bi.getBasicBlock().getFirstInstruction() == null)
						continue;
					if (!bbs.contains(bi.getBasicBlock().getFirstInstruction().getBytecodeOffset()))
						bbs.add(bi.getBasicBlock().getFirstInstruction().getBytecodeOffset());
				}
			methodBasicblockOriginal.put(methodName, bbs);

		}

		Collection<Branch> branchesOriginal = BranchPool.getInstance(TestGenerationContext.getClassLoader()).getAllBranches();

		// Pool of blockoffset <-> branchids for the original version
		Map<String, Map<Integer, Integer>> blockOffsetBranchidOriginal = new HashMap<String, Map<Integer, Integer>>();

		
		 * for (Branch b : branchesOriginal) { int bId = b.getActualBranchId();
		 * int bOffset = b.getInstruction().getBytecodeOffset(); String bMethod
		 * = b.getClassName() + "." + b.getMethodName(); int branchBasicBlock =
		 * 0; List<Integer> basicBlocks = methodBasicblockOriginal.get(bMethod);
		 * if (basicBlocks != null) for (Integer blockOffset : basicBlocks) {
		 * logger.warn("offset: " + blockOffset); if (bOffset < blockOffset) {
		 * break; } else { branchBasicBlock = blockOffset; }
		 * 
		 * } logger.warn("-------" + branchBasicBlock + "------->" + bOffset);
		 * Map<Integer, Integer> offId = new HashMap<Integer, Integer>();
		 * offId.put(branchBasicBlock, bId); //
		 * blockOffsetBranchidOriginal.put(bMethod, offId);
		 * 
		 * if (blockOffsetBranchidOriginal.containsKey(bMethod)) { Map<Integer,
		 * Integer> tempOffId = (blockOffsetBranchidOriginal .get(bMethod));
		 * tempOffId.put(branchBasicBlock, bId);
		 * blockOffsetBranchidOriginal.put(bMethod, tempOffId); } else
		 * blockOffsetBranchidOriginal.put(bMethod, offId); }
		 

		for (Branch b : branchesOriginal) {
			String bMethod = b.getClassName() + "." + b.getMethodName();
			int blockOffset = b.getInstruction().getBasicBlock().getFirstInstruction().getBytecodeOffset();
			int branchID = b.getActualBranchId();
			logger.warn("original offset-id-block: "
			        + b.getInstruction().getBytecodeOffset() + " | " + branchID + " | "
			        + blockOffset + " | " + b.getInstruction().getLineNumber());
			Map<Integer, Integer> offId = new HashMap<Integer, Integer>();
			offId.put(blockOffset, branchID);
			if (blockOffsetBranchidOriginal.containsKey(bMethod)) {
				Map<Integer, Integer> tempOffId = (blockOffsetBranchidOriginal.get(bMethod));
				tempOffId.put(blockOffset, branchID);
				blockOffsetBranchidOriginal.put(bMethod, tempOffId);
			} else {
				blockOffsetBranchidOriginal.put(bMethod, offId);
			}
		}

		Collection<Branch> branchesRegression = BranchPool.getInstance(TestGenerationContext.getInstance().getRegressionClassLoaderForSUT()).getAllBranches();

		Map<String, List<Integer>> methodBasicblockRegression = new HashMap<String, List<Integer>>();

		allMethods = MethodPool.getInstance(TestGenerationContext.getInstance().getRegressionClassLoaderForSUT()).getMethods();

		// Method <-> list of Basic Block offsets for Regression
		for (String methodName : allMethods) {
			List<BytecodeInstruction> instructions = BytecodeInstructionPool.getInstance(TestGenerationContext.getRegressionClassLoader()).getInstructionsIn(className,
			                                                                                                                                                 methodName.replace(className,
			                                                                                                                                                                    "").substring(1));
			List<Integer> bbs = new ArrayList<Integer>();
			if (instructions != null)
				for (BytecodeInstruction bi : instructions) {
					if (bi.getBasicBlock() == null
					        || bi.getBasicBlock().getFirstInstruction() == null)
						continue;
					if (!bbs.contains(bi.getBasicBlock().getFirstLine()))
						bbs.add(bi.getBasicBlock().getFirstLine());
				}
			methodBasicblockRegression.put(methodName, bbs);
		}
		logger.warn("methodBasicblockRegression: " + methodBasicblockRegression);
		// Pool of blockoffset <-> branchids for the regression version
		Map<String, Map<Integer, Integer>> blockoffsetBranchidRegression = new HashMap<String, Map<Integer, Integer>>();

		
		 * for (Branch b : branchesRegression) {
		 * 
		 * int bId = b.getActualBranchId(); logger.warn("branch ID Regression: "
		 * + bId); logger.warn("branch basic block: " +
		 * b.getInstruction().getBasicBlock
		 * ().getFirstInstruction().getBytecodeOffset()); int bOffset =
		 * b.getInstruction().getBytecodeOffset(); String bMethod =
		 * b.getClassName() + "." + b.getMethodName(); int branchBasicBlock = 0;
		 * //logger.warn("branch method: " + bMethod); List<Integer> basicBlocks
		 * = methodBasicblockRegression.get(bMethod); if (basicBlocks != null){
		 * for (Integer blockOffset : basicBlocks) { logger.warn("offset: "+
		 * blockOffset); if (bOffset < blockOffset) { break; } else {
		 * branchBasicBlock = blockOffset; }
		 * 
		 * } } else { logger.warn("empty basicBlocks"); } logger.warn("-------"+
		 * branchBasicBlock +"------->" + bOffset); Map<Integer, Integer> offId
		 * = new HashMap<Integer, Integer>(); offId.put(branchBasicBlock, bId);
		 * 
		 * if(blockoffsetBranchidRegression.containsKey(bMethod)){ Map<Integer,
		 * Integer> tempOffId = (blockoffsetBranchidRegression.get(bMethod));
		 * tempOffId.put(branchBasicBlock, bId);
		 * blockoffsetBranchidRegression.put(bMethod, tempOffId); } else
		 * blockoffsetBranchidRegression.put(bMethod, offId); }
		 
		for (Branch b : branchesRegression) {
			String bMethod = b.getClassName() + "." + b.getMethodName();
			int blockOffset = b.getInstruction().getBasicBlock().getFirstInstruction().getBytecodeOffset();
			int branchID = b.getActualBranchId();
			Map<Integer, Integer> offId = new HashMap<Integer, Integer>();
			offId.put(blockOffset, branchID);
			if (blockoffsetBranchidRegression.containsKey(bMethod)) {
				Map<Integer, Integer> tempOffId = (blockoffsetBranchidRegression.get(bMethod));
				tempOffId.put(blockOffset, branchID);
				blockoffsetBranchidRegression.put(bMethod, tempOffId);
			} else {
				blockoffsetBranchidRegression.put(bMethod, offId);
			}
		}

		// ////////////////

		// //////////////////////////// Final MAP!!! (Branch <-> Branch)

		// Map<Integer, Integer> branchIdMap = new HashMap<Integer,
		// Integer>();
		for (Entry<String, Map<Integer, Integer>> mOffsetIdOrig : blockOffsetBranchidOriginal.entrySet()) {
			for (Entry<Integer, Integer> offsetIdOrig : mOffsetIdOrig.getValue().entrySet()) {

				logger.warn(" -- " + mOffsetIdOrig.getKey()
				        + jdiffMethodNodePairOffsets.get(mOffsetIdOrig.getKey()));
				logger.warn("offsetIdOrig.getKey(): " + offsetIdOrig.getKey());
				logger.warn("blockoffsetBranchidRegression.get(mOffsetIdOrig.getKey())"
				        + blockOffsetBranchidOriginal);
				try {

					int regID = -2;
					if (jdiffMethodNodePairOffsets.get(mOffsetIdOrig.getKey()) == null
					        || blockoffsetBranchidRegression == null
					        || blockoffsetBranchidRegression.get(mOffsetIdOrig.getKey()) == null)
						continue;
					// if no match
					if (!blockoffsetBranchidRegression.get(mOffsetIdOrig.getKey()).containsKey(jdiffMethodNodePairOffsets.get(mOffsetIdOrig.getKey()).get(offsetIdOrig.getKey()))) {

						logger.warn(":( -- " + offsetIdOrig.getKey());
						int regValue = -1;
						for (Entry<Integer, Integer> jDiffBlockOffsets : jdiffMethodNodePairOffsets.get(mOffsetIdOrig.getKey()).entrySet()) {
							// logger.warn("offset: " + jDiffBlockOffsets +
							// " offsetIdOrig.getKey()" +
							// offsetIdOrig.getKey());
							if (jDiffBlockOffsets.getKey() >= offsetIdOrig.getKey()
							        && regValue == -1) {

								regValue = jDiffBlockOffsets.getKey();
							} else {
								continue;
								// regValue = jDiffBlockOffsets.getKey();
							}
						}
						logger.warn("regValue: " + regValue);

						Integer jDiffValue = jdiffMethodNodePairOffsets.get(mOffsetIdOrig.getKey()).get(regValue);

						if (jDiffValue == null)
							continue;
						logger.warn("jDiffValue: " + jDiffValue);

						int regIDValue = 0;
						int regIdKeyChosen = -1;

						for (Entry<Integer, Integer> regressionBlockId : blockoffsetBranchidRegression.get(mOffsetIdOrig.getKey()).entrySet()) {
							// logger.warn("regressionBlockId: " +
							// regressionBlockId);
							if (regressionBlockId.getKey() <= jDiffValue
							        && regressionBlockId.getKey() >= regIdKeyChosen) {
								regIDValue = regressionBlockId.getValue();
								regIdKeyChosen = regressionBlockId.getKey();
							}

						}

						regID = regIDValue;

						// regID = regValue;
						logger.warn("Reg Value : " + regID);
					} else {

						regID = blockoffsetBranchidRegression.get(mOffsetIdOrig.getKey()).get(jdiffMethodNodePairOffsets.get(mOffsetIdOrig.getKey()).get(offsetIdOrig.getKey()));
					}
					logger.warn("adding offsetIdOrig.getValue(): "
					        + offsetIdOrig.getValue() + " | regID: " + regID);
					if (!branchIdMap.containsValue(regID))
						branchIdMap.put(offsetIdOrig.getValue(), regID);

				} catch (NullPointerException e) {
					e.printStackTrace();
					logger.warn(e.getMessage());
					continue;
				}
			}
		}
		logger.warn("---------------------------------------------------------------------");
		logger.warn("JDiff: " + jdiffMethodNodePairOffsets.toString());
		logger.warn("Original bbOffset - bId: " + blockOffsetBranchidOriginal.toString());
		logger.warn("Regression bbOffset - bId: "
		        + blockoffsetBranchidRegression.toString());
		logger.warn(branchIdMap.toString());
	}

	public static Map<Integer, Integer> branchIdMap = new HashMap<Integer, Integer>();
	
	public static String buildJRC(String className, boolean isRegression) {
		String jrc = "";
		String sep = (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) ? "\\\\"
		        : "/";
		String sep2 = (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) ? ";"
		        : ":";

		// find evosuite route
		int count = StringUtils.countMatches(Properties.REGRESSIONCP, "..");
		String findDaddy = "";
		if ((System.getProperty("os.name").toLowerCase().indexOf("win") >= 0))
			for (int i = 0; i <= count; i++)
				findDaddy += ".." + sep;
		else {

			String path = RegressionSuiteFitness.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			path = path.substring(0, path.lastIndexOf("/"));

			path = path.replace("/", sep);

			findDaddy = path + sep + ".." + sep;

		}
		jrc += "ProgramName = "
		        + ((className.lastIndexOf('.') == -1) ? className
		                : className.substring(className.lastIndexOf('.') + 1)) + "\n";
		jrc += "ClassPath = "
		        // + ((isRegression) ? ".." + sep + "amis2" + sep : "") +
		        // "build" + sep + "classes"
		        + ((!isRegression) ? Properties.CP.replace("/", sep)
		                : Properties.REGRESSIONCP.replace("/", sep)) + sep2 + findDaddy
		        + "lib" + sep + "rt" + sep + "rt" + sep + "1.6" + sep + "rt-1.6.jar"
		        + "\n";
		jrc += "ClassFiles = \\" + "\n";
		jrc += className + "\n";

		return jrc;
	}*/
}
