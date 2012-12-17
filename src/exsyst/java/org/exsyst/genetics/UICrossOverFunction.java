package org.exsyst.genetics;

import org.evosuite.ga.Chromosome;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.CrossOverFunction;

public class UICrossOverFunction extends CrossOverFunction {
	private static final long serialVersionUID = 1L;

	@Override
	public void crossOver(Chromosome parent1, Chromosome parent2) throws ConstructionFailedException {
		if (!(parent1 instanceof UITestChromosome) || !(parent2 instanceof UITestChromosome)) {
			throw new IllegalArgumentException("UICrossOverFunction can only be used for cross-over of UITestChromosomes");
		}
		
		UITestChromosome x1 = (UITestChromosome) parent1;
		UITestChromosome x2 = (UITestChromosome) parent2;
		
		UITestChromosome c1 = (UITestChromosome) x1.clone();
		UITestChromosome c2 = (UITestChromosome) x2.clone();
		
		x1.replaceWithCrossOverResult(c1);
		x2.replaceWithCrossOverResult(c2);
	}
}
