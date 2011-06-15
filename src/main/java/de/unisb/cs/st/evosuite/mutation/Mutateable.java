package de.unisb.cs.st.evosuite.mutation;

import java.util.List;

public interface Mutateable {

	public int getDistance(long mutationId);

	public List<Long> getMutationIds();

	public String getName();

	public boolean hasMutation(long mutationId);

	public boolean isMutation();

	public void setDistance(long mutationId, int distance);
}
