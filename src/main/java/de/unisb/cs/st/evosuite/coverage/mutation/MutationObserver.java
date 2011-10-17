/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.mutation;

/**
 * @author Gordon Fraser
 * 
 */
public class MutationObserver {

	public static int activeMutation = -1;

	public static void mutationTouched(int mutationID) {

	}

	public static void activateMutation(Mutation mutation) {
		if (mutation != null)
			activeMutation = mutation.getId();
	}

	public static void activateMutation(int id) {
		activeMutation = id;
	}

	public static void deactivateMutation() {
		activeMutation = -1;
	}

	public static void deactivateMutation(Mutation mutation) {
		activeMutation = -1;
	}

}
