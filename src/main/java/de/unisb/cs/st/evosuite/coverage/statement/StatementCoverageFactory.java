package de.unisb.cs.st.evosuite.coverage.statement;

import java.util.ArrayList;
import java.util.List;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.cfg.BytecodeInstructionPool;
import de.unisb.cs.st.evosuite.testcase.TestFitnessFunction;
import de.unisb.cs.st.evosuite.testsuite.AbstractFitnessFactory;

public class StatementCoverageFactory extends AbstractFitnessFactory {

	private static boolean called = false;
	private static List<TestFitnessFunction> goals = new ArrayList<TestFitnessFunction>();
	
	@Override
	public List<TestFitnessFunction> getCoverageGoals() {
		
		if(!called)
			computeGoals();

		return goals;
	}
	
	private static void computeGoals() {
		
		if(called)
			return;
		
		String targetMethod = Properties.TARGET_METHOD;
		String targetClass = Properties.TARGET_CLASS;

		for (String className : BytecodeInstructionPool.knownClasses()) {

			if (!(targetClass.equals("") || className.endsWith(targetClass)))
				continue;

			for (String methodName : BytecodeInstructionPool
					.knownMethods(className)) {

				if (!targetMethod.equals("")
						&& !methodName.equals(targetMethod))
					continue;

				for (BytecodeInstruction ins : BytecodeInstructionPool.getInstructionsIn(className, methodName))
					if(isUsable(ins))
						goals.add(new StatementCoverageTestFitness(ins));
			}
		}
		
		called = true;		
	}


	private static boolean isUsable(BytecodeInstruction ins) {
		
		return !ins.isLabel() && !ins.isLineNumber();
	}

	public static List<TestFitnessFunction> retrieveCoverageGoals() {
		if(!called)
			computeGoals();
		
		return goals;
	}
}
