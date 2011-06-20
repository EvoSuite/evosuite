package de.unisb.cs.st.evosuite.mutation;

import java.util.List;

public interface Mutateable {

	public boolean isMutation();

	public List<Long> getMutationIds();

	public boolean hasMutation(long mutationId);

	public int getDistance(long mutationId);

	public void setDistance(long mutationId, int distance);
	
	public String getName();
}
