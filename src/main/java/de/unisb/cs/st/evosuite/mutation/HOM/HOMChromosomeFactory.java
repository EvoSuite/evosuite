package de.unisb.cs.st.evosuite.mutation.HOM;

import java.util.List;

import org.apache.log4j.Logger;

import de.unisb.cs.st.evosuite.ga.ChromosomeFactory;
import de.unisb.cs.st.evosuite.utils.Randomness;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;

public class HOMChromosomeFactory implements ChromosomeFactory<HOMChromosome> {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(HOMChromosomeFactory.class);
	
	private HOMSwitcher hom_switcher = new HOMSwitcher();
	
	private List<Mutation> mutants;
	
	public HOMChromosomeFactory() {
		mutants = hom_switcher.getMutants();
	}

	public HOMChromosomeFactory(List<Mutation> mutants) {
		this.mutants = mutants;
	}

	public HOMChromosome getChromosome() {
		HOMChromosome chromosome = new HOMChromosome(mutants);
		chromosome.set(Randomness.nextInt(mutants.size()), true);
		
		for(int i=0; i<chromosome.size(); i++) {
			chromosome.set(i, Randomness.nextBoolean());
		}
		
		logger.info("Generating random HOM of size: "+mutants.size()+" -> "+chromosome.size());
		return chromosome;
	}

}
