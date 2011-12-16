/**
 * 
 */
package de.unisb.cs.st.evosuite.testcase;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.objectweb.asm.Type;

import de.unisb.cs.st.evosuite.Properties;
import de.unisb.cs.st.evosuite.coverage.branch.BranchCoverageTestFitness;
import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.SecondaryObjective;

/**
 * @author fraser
 * 
 */
public class DirectCallSecondaryObjective extends SecondaryObjective {

	private final BranchCoverageTestFitness goal;

	private final String targetName;

	public DirectCallSecondaryObjective(BranchCoverageTestFitness goal) {
		this.goal = goal;
		this.targetName = goal.getMethod();
	}

	private int hasDirectCall(ExecutionResult result) {

		for (StatementInterface statement : result.test) {
			if (statement instanceof MethodStatement) {
				MethodStatement methodStatement = (MethodStatement) statement;
				Method method = methodStatement.getMethod();
				if (method.getDeclaringClass().equals(Properties.getTargetClass())
				        && Modifier.isPublic(method.getModifiers())) {
					String name = method.getName() + Type.getMethodDescriptor(method);
					if (name.equals(targetName))
						return 1;
				}
			}
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SecondaryObjective#compareChromosomes(de.unisb.cs.st.evosuite.ga.Chromosome, de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public int compareChromosomes(Chromosome chromosome1, Chromosome chromosome2) {
		TestChromosome test1 = (TestChromosome) chromosome1;
		TestChromosome test2 = (TestChromosome) chromosome2;

		ExecutionResult result1 = test1.getLastExecutionResult();
		ExecutionResult result2 = test2.getLastExecutionResult();

		if (result1 == null || result2 == null)
			return 0;

		int call1 = hasDirectCall(result1);
		int call2 = hasDirectCall(result2);

		return call2 - call1;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.SecondaryObjective#compareGenerations(de.unisb.cs.st.evosuite.ga.Chromosome, de.unisb.cs.st.evosuite.ga.Chromosome, de.unisb.cs.st.evosuite.ga.Chromosome, de.unisb.cs.st.evosuite.ga.Chromosome)
	 */
	@Override
	public int compareGenerations(Chromosome parent1, Chromosome parent2,
	        Chromosome child1, Chromosome child2) {
		// TODO Auto-generated method stub
		return 0;
	}

}
