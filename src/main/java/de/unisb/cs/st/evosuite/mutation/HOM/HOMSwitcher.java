package de.unisb.cs.st.evosuite.mutation.HOM;

import java.util.List;

import org.apache.log4j.Logger;

import de.unisb.cs.st.javalanche.mutation.results.Mutation;
import de.unisb.cs.st.javalanche.mutation.runtime.MutationObserver;

public class HOMSwitcher {

	private static final String CURRENT_MUTATION_KEY = "javalanche.current.mutation";

	private final Logger logger = Logger.getLogger(HOMSwitcher.class);

	static int num_mutants;

	public final static List<Mutation> mutants = HOMFileTransformer.mm.getMutations(); //MutationForRun.getFromDefaultLocation().getMutations();

	public HOMSwitcher() {
		// TODO: All mutants?
		//num_mutants = (int)QueryManager.getNumberOfMutationsWithPrefix(MutationProperties.PROJECT_PREFIX);
		//			if(mutants == null) {
		//				mutants = MutationForRun.getFromDefaultLocation().getMutations();
		num_mutants = mutants.size();
		//				logger.info("Got "+num_mutants+" mutations");
		//				assert(num_mutants == mutants.size());
		//			}
		//mutants = QueryManager.getMutationIdListFromDb(num_mutants);
	}

	public void switchOn(HOMChromosome hom) {
		//assert(hom.size() == num_mutants);
		for (int i = 0; i < mutants.size(); i++) {
			if (hom.get(i)) {
				logger.debug("Activating " + mutants.get(i).getMutationVariable());
				System.setProperty(mutants.get(i).getMutationVariable(), "1");
				//assert(System.getProperty(mutants.get(i).getMutationVariable()).equals("1"));
				System.setProperty(CURRENT_MUTATION_KEY, mutants.get(i).getId() + "");
			} else {
				System.clearProperty(mutants.get(i).getMutationVariable());
				System.clearProperty(CURRENT_MUTATION_KEY);
			}
		}
	}

	public void switchOn(Mutation m) {
		logger.debug("Activating " + m.getMutationVariable());
		MutationObserver.expectedID.set(m.getId());
		System.setProperty(m.getMutationVariable(), "1");
		System.setProperty(CURRENT_MUTATION_KEY, m.getId() + "");
	}

	public void switchOff(Mutation m) {

		if (m == null)
			switchOff();
		else {
			System.clearProperty(m.getMutationVariable());
			System.clearProperty(CURRENT_MUTATION_KEY);
		}
	}

	public void switchOff() {
		for (Mutation m : mutants) {
			System.clearProperty(m.getMutationVariable());
			System.clearProperty(CURRENT_MUTATION_KEY);
		}
	}

	public int getNumMutants() {
		return num_mutants;
	}

	public List<Mutation> getMutants() {
		return mutants;
	}
	//	public void activate(Long id) {
	//		System.setProperty(currentMutation.getMutationVariable(), "1");
	//		System.setProperty(MutationProperties.CURRENT_MUTATION_KEY,
	//		
	//	}
}
