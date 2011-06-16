package de.unisb.cs.st.evosuite.mutation.HOM;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.ConstructionFailedException;
import de.unisb.cs.st.evosuite.ga.LocalSearchObjective;
import de.unisb.cs.st.evosuite.utils.Randomness;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;
import de.unisb.cs.st.javalanche.mutation.results.MutationTestResult;

public class HOMChromosome extends Chromosome {

	private final BitSet activated;

	private List<Mutation> mutants;

	protected int size = 0;

	double last_fitness = 0.0;
	MutationTestResult last_result;

	double mutation_rate = 0.0;

	public HOMChromosome(List<Mutation> mutants) {
		activated = new BitSet(mutants.size());
		size = mutants.size();
		this.mutants = mutants;
		mutation_rate = 1.0 / size;
	}

	public HOMChromosome(HOMChromosome chromosome) {
		activated = (BitSet) chromosome.activated.clone();
		mutation_rate = chromosome.mutation_rate;
		size = chromosome.size;
	}

	@Override
	public int size() {
		return size;
	}

	public boolean get(int position) {
		return activated.get(position);
	}

	public void set(int position, boolean value) {
		activated.set(position, value);
	}

	public List<Mutation> getActiveMutants() {
		List<Mutation> active = new ArrayList<Mutation>();
		for (int i = 0; i < size; i++) {
			if (activated.get(i))
				active.add(mutants.get(i));
		}
		return active;
	}

	public int getNumberOfMutations() {
		return activated.cardinality();
		/*
		int num = 0;
		
		for(int i=0; i<activated.size(); i++) {
			if(activated.get(i))
				num++;
		}
		
		return num;
		*/
	}

	public void randomize() {
		for (int i = 0; i < activated.size(); i++) {
			activated.set(i, Randomness.nextBoolean());
		}
	}

	public void flip(int position) {
		activated.flip(position);
	}

	@Override
	public void mutate() {
		for (int i = 0; i < activated.size(); i++) {
			if (Randomness.nextDouble() <= mutation_rate)
				activated.flip(i);
		}
		//activated.flip(Randomness.nextInt(activated.size()));
	}

	@Override
	public Chromosome clone() {
		return new HOMChromosome(this);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.Chromosome#localSearch()
	 */
	@Override
	public void localSearch(LocalSearchObjective objective) {
		// TODO Auto-generated method stub

	}

	@Override
	public void crossOver(Chromosome other, int position1, int position2)
	        throws ConstructionFailedException {
		for (int i = position2; i < other.size(); i++) {
			activated.set(i, ((HOMChromosome) other).activated.get(i));
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		HOMChromosome other = (HOMChromosome) obj;
		if (other.activated.size() != activated.size())
			return false;

		return activated.equals(other.activated);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.ga.Chromosome#applyDSE()
	 */
	@Override
	public void applyDSE() {
		// TODO Auto-generated method stub

	}

}
