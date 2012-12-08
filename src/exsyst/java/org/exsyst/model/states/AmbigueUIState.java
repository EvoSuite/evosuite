package org.exsyst.model.states;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.NodeRealizer;

import org.exsyst.model.DescriptorBoundUIAction;
import org.exsyst.model.UIActionTargetDescriptor;
import org.exsyst.model.WindowDescriptor;
import org.exsyst.util.GraphVizDrawable;
import org.exsyst.util.GraphVizEnvironment;
import org.exsyst.util.YWorksDrawable;
import org.exsyst.util.YWorksEnvironment;

public class AmbigueUIState extends AbstractUIState implements GraphVizDrawable,
        YWorksDrawable {
	private static final long serialVersionUID = 1L;

	private final Map<AbstractUIState, Integer> possibleStates = new HashMap<AbstractUIState, Integer>();
	private final Map<DescriptorBoundUIAction<?>, UIState> ownTransitions = new HashMap<DescriptorBoundUIAction<?>, UIState>();

	public AmbigueUIState() {
	}

	public AmbigueUIState(Iterable<AbstractUIState> possibleStates) {
		this();

		for (AbstractUIState state : possibleStates) {
			this.addPossibleState(state);
		}
	}

	private void addPossibleStateInternal(AbstractUIState state, int count) {
		if (state == null) {
			throw new IllegalArgumentException("state");
		}

		if (!state.isKnown()) {
			throw new IllegalArgumentException(
			        "Cannot add an unknown state as possible state of an AmbigueUIState");
		} else {
			this.possibleStates.put(state,
			                        count
			                                + (!this.possibleStates.containsKey(state) ? 0
			                                        : this.possibleStates.get(state)));
		}
	}

	public void addPossibleState(AbstractUIState state) {
		if (state == null) {
			throw new IllegalArgumentException("state");
		}

		if (state instanceof AmbigueUIState) {
			Set<Map.Entry<AbstractUIState, Integer>> entries = ((AmbigueUIState) state).possibleStates.entrySet();

			for (Map.Entry<AbstractUIState, Integer> entry : entries) {
				addPossibleStateInternal(entry.getKey(), entry.getValue());
			}
		} else {
			addPossibleStateInternal(state, 1);
		}
	}

	@Override
	public void addTransition(DescriptorBoundUIAction<?> action, UIState toState) {
		throw new UnsupportedOperationException(
		        "Why are you trying to add a transition to an AmbigueUIState?");
		// this.ownTransitions.put(action, toState);
	}

	@Override
	public List<UIActionTargetDescriptor> getActionTargetDescriptors() {
		List<UIActionTargetDescriptor> result = new LinkedList<UIActionTargetDescriptor>();

		for (AbstractUIState possibleState : possibleStates.keySet()) {
			result.addAll(possibleState.getActionTargetDescriptors());
		}

		return result;
	}

	@Override
	public List<WindowDescriptor> getTargetableWindowDescriptors() {
		List<WindowDescriptor> result = new LinkedList<WindowDescriptor>();

		for (AbstractUIState possibleState : possibleStates.keySet()) {
			result.addAll(possibleState.getTargetableWindowDescriptors());
		}

		return result;
	}

	@Override
	public AbstractUIState getTransition(DescriptorBoundUIAction<?> action) {
		boolean oneCanResolve = false;
		LinkedHashSet<AbstractUIState> knownStates = new LinkedHashSet<AbstractUIState>();

		for (AbstractUIState possibleState : possibleStates.keySet()) {
			AbstractUIState state = possibleState.getTransition(action);
			boolean canResolve = state != null;
			oneCanResolve |= canResolve;

			if (canResolve && (state.isKnown() || state.isAmbigue())) {
				knownStates.add(state);
			}
		}

		if (ownTransitions.containsKey(action)) {
			AbstractUIState state = ownTransitions.get(action);
			boolean canResolve = state != null;
			oneCanResolve |= canResolve;

			if (canResolve && (state.isKnown() || state.isAmbigue())) {
				knownStates.add(state);
			}
		}

		if (knownStates.size() > 1) {
			return new AmbigueUIState(knownStates);
		}

		if (knownStates.size() == 1) {
			return knownStates.toArray(new AbstractUIState[1])[0];
		}

		return oneCanResolve ? new UnknownUIState(this) : null;
	}

	@Override
	public Map<DescriptorBoundUIAction<?>, AbstractUIState> getTransitions() {
		Map<DescriptorBoundUIAction<?>, AbstractUIState> result = new HashMap<DescriptorBoundUIAction<?>, AbstractUIState>(
		        this.ownTransitions);

		for (AbstractUIState possibleState : possibleStates.keySet()) {
			Map<DescriptorBoundUIAction<?>, ? extends AbstractUIState> transitions = possibleState.getTransitions();

			for (Map.Entry<DescriptorBoundUIAction<?>, ? extends AbstractUIState> entry : transitions.entrySet()) {
				DescriptorBoundUIAction<?> key = entry.getKey();
				AbstractUIState newState = entry.getValue();

				if (result.containsKey(key)) {
					AbstractUIState oldState = result.get(key);

					result.put(key,
					           new AmbigueUIState(
					                   Arrays.<AbstractUIState> asList(newState, oldState)));
				}
			}
		}

		return result;
	}

	@Override
	public boolean isUnknown() {
		return true;
	}

	@Override
	public boolean isAmbigue() {
		return true;
	}

	@Override
	public String toGraphViz(GraphVizEnvironment env) {
		String id = this.graphVizId(env);
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("subgraph %s {\n", id));
		sb.append(String.format("rankdir=LR\nstyle=filled\ncolor=black\nfillcolor=papayawhip\nlabel=\"Ambigue UIState %s\"\n\n",
		                        id));

		sb.append(String.format("dummy_%s [label=\"\",style=invis,shape=none,margin=\"0,0\",width=0,height=0,fixedsize=true]\n\n",
		                        id));

		sb.append("}\n");

		return sb.toString();
	}

	String graphVizEdges(GraphVizEnvironment env) {
		String fromId = this.graphVizId(env);
		String dummyFromId = "dummy_" + fromId;
		StringBuilder sb = new StringBuilder();

		for (DescriptorBoundUIAction<?> action : this.ownTransitions.keySet()) {
			sb.append(action.toGraphViz(env));
			sb.append("\n");
		}

		sb.append("\n");

		for (DescriptorBoundUIAction<?> action : this.ownTransitions.keySet()) {
			String actionId = env.getId(action);
			String onId = env.getId(action.targetDescriptor());
			String toId = this.ownTransitions.get(action).graphVizId(env);
			String dummyToId = "dummy_" + toId;

			sb.append(String.format("%s -> %s [ltail=\"%s\"]\n", dummyFromId, actionId,
			                        fromId));
			sb.append(String.format("%s -> %s [lhead=\"%s\"]\n", actionId, dummyToId,
			                        toId));

			sb.append(String.format("%s -> %s [arrowhead=dot,arrowtail=dot]\n", actionId,
			                        onId));

			sb.append("\n");
		}

		sb.append("\n");

		for (Map.Entry<AbstractUIState, Integer> entry : possibleStates.entrySet()) {
			AbstractUIState possibleState = entry.getKey();
			Integer count = entry.getValue();

			String toId = possibleState.graphVizId(env);
			String dummyToId = "dummy_" + toId;

			sb.append(String.format("%s -> %s [ltail=\"%s\", lhead=\"%s\", arrowtail=dot, label=%d]\n",
			                        dummyFromId, dummyToId, fromId, toId, count));
		}

		return sb.toString();
	}

	@Override
	public void addToYWorksEnvironment(YWorksEnvironment env) {
		NodeRealizer realizer = env.getNodeRealizerFor(this);
		realizer.setLabelText("Ambigue UIState");

		for (DescriptorBoundUIAction<?> action : this.ownTransitions.keySet()) {
			action.addToYWorksEnvironment(env);
		}
	}

	@Override
	public void addEdgesToYWorksEnvironment(YWorksEnvironment env) {
		for (DescriptorBoundUIAction<?> action : this.ownTransitions.keySet()) {
			EdgeRealizer edgeRealizer = env.getEdgeRealizerFor(action.targetDescriptor(),
			                                                   action);
			edgeRealizer.setSourceArrow(Arrow.CIRCLE);
			edgeRealizer.setTargetArrow(Arrow.CIRCLE);

			edgeRealizer = env.getEdgeRealizerFor(action, this.ownTransitions.get(action));
			edgeRealizer.setArrow(Arrow.CONCAVE);
		}

		for (Map.Entry<AbstractUIState, Integer> entry : possibleStates.entrySet()) {
			AbstractUIState possibleState = entry.getKey();
			Integer count = entry.getValue();

			EdgeRealizer edgeRealizer = env.getEdgeRealizerFor(this, possibleState);
			edgeRealizer.setSourceArrow(Arrow.CIRCLE);
			edgeRealizer.setArrow(Arrow.CONCAVE);
			edgeRealizer.setLabelText(count + "x");
		}
	};

	public Set<AbstractUIState> getPossibleStates() {
		return this.possibleStates.keySet();
	}

	@Override
	public String toString() {
		return "AmbigueUIState (" + StringUtils.join(possibleStates.entrySet(), ", ")
		        + ")";
	}

	@Override
	public String shortString() {
		StringBuilder result = new StringBuilder("AmbigueUIState (");
		boolean isFirst = true;

		for (Map.Entry<AbstractUIState, Integer> entry : possibleStates.entrySet()) {
			if (!isFirst) {
				result.append(", ");
			}

			result.append(String.format("%s=%s", entry.getKey().shortString(),
			                            entry.getValue()));
			isFirst = false;
		}

		result.append(")");
		return result.toString();
	}

}
