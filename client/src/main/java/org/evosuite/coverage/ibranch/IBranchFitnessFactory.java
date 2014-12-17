/**
 * 
 */
package org.evosuite.coverage.ibranch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.coverage.branch.BranchCoverageFactory;
import org.evosuite.coverage.branch.BranchCoverageTestFitness;
import org.evosuite.setup.CallContext;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.setup.callgraph.CallGraph;
import org.evosuite.testsuite.AbstractFitnessFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Create the IBranchTestFitness goals for the class under test.
 * @author mattia, Gordon Fraser
 * 
 */
public class IBranchFitnessFactory extends AbstractFitnessFactory<IBranchTestFitness> {

	private static Logger logger = LoggerFactory.getLogger(IBranchFitnessFactory.class);

	/* (non-Javadoc)
	 * @see org.evosuite.coverage.TestFitnessFactory#getCoverageGoals()
	 */
	@Override
	public List<IBranchTestFitness> getCoverageGoals() {
		//TODO this creates duplicate goals. Momentary fixed using a Set.
		Set<IBranchTestFitness> goals = new HashSet<IBranchTestFitness>();

		// retrieve set of branches
		BranchCoverageFactory branchFactory = new BranchCoverageFactory();
		List<BranchCoverageTestFitness> branchGoals = branchFactory.getCoverageGoalsForAllKnownClasses();

		CallGraph callGraph = DependencyAnalysis.getCallGraph();


		// try to find all occurrences of this branch in the call tree
		for (BranchCoverageTestFitness branchGoal : branchGoals) {
			logger.info("Adding context branches for " + branchGoal.toString());
			for (CallContext context : callGraph.getAllContextsFromTargetClass(branchGoal.getClassName(),
				                          branchGoal.getMethod())) {
				//if is not possible to reach this branch from the target class, continue.
				if(context.isEmpty()) continue; 				
				goals.add(new IBranchTestFitness(branchGoal.getBranchGoal(), context));				
			}
		}
		logger.info("Created " + goals.size() + " goals");
		List<String> l = new ArrayList<>();
		for (IBranchTestFitness callGraphEntry : goals) {
			l.add(callGraphEntry.toStringContext());
		}
		File f = new File("/Users/mattia/workspaces/evosuiteSheffield/evosuite/master/evosuite-report/ibranchgoals.txt");
		f.delete();
		try {
			Files.write(f.toPath(), l, Charset.defaultCharset(), StandardOpenOption.CREATE);
		} catch (IOException e) { 
			e.printStackTrace();
		}
		return new ArrayList<IBranchTestFitness>(goals);
	}
}



//	private boolean isGradient(BranchCoverageTestFitness branchGoal){
//		if (branchGoal.getBranchGoal().getBranch() == null)
//			return false;
//		int branchOpCode = branchGoal.getBranchGoal().getBranch().getInstruction().getASMNode()
//				.getOpcode();
//		switch (branchOpCode) {
//		// copmpare int with zero
//		case Opcodes.IFEQ:
//		case Opcodes.IFNE:
//		case Opcodes.IFLT:
//		case Opcodes.IFGE:
//		case Opcodes.IFGT:
//		case Opcodes.IFLE:
//			return false; 
//			// copmpare int with int
//		case Opcodes.IF_ICMPEQ:
//		case Opcodes.IF_ICMPNE:
//		case Opcodes.IF_ICMPLT:
//		case Opcodes.IF_ICMPGE:
//		case Opcodes.IF_ICMPGT:
//		case Opcodes.IF_ICMPLE:
//			return true; 
//			// copmpare reference with reference
//		case Opcodes.IF_ACMPEQ:
//		case Opcodes.IF_ACMPNE:
//			return false; 
//			// compare reference with null
//		case Opcodes.IFNULL:
//		case Opcodes.IFNONNULL:
//			return false;
//		default:
//			return false; 
//		}
//	}
	
	
//	//---------- 
//	List<String> l = new ArrayList<>();
//	for (IBranchTestFitness callGraphEntry : goals) {
//		l.add(callGraphEntry.toStringContext());
//	}
//	File f = new File("/Users/mattia/workspaces/evosuiteSheffield/evosuite/master/evosuite-report/ibranchgoals.txt");
//	f.delete();
//	try {
//		Files.write(f.toPath(), l, Charset.defaultCharset(), StandardOpenOption.CREATE);
//	} catch (IOException e) { 
//		e.printStackTrace();
//	}
//	//---------- 

