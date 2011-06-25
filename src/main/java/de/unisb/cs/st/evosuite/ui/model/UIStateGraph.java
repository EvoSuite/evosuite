package de.unisb.cs.st.evosuite.ui.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.evosuite.ui.AbstractUIEnvironment;
import de.unisb.cs.st.evosuite.ui.GraphVizDrawable;
import de.unisb.cs.st.evosuite.ui.GraphVizEnvironment;
import de.unisb.cs.st.evosuite.ui.model.UIState.Descriptor;

public class UIStateGraph implements GraphVizDrawable, Serializable {
	private static final long serialVersionUID = 1L;

	private final Map<Descriptor, UIState> stateMap = new HashMap<Descriptor, UIState>();
	final Set<AmbigueUIState> ambigueStates = new HashSet<AmbigueUIState>();
	private int lastAssignedId = -1;
	private UIState initialState = null;	

	UIState getState(Descriptor descriptor) {		
		if (!this.stateMap.containsKey(descriptor)) {
			UIState uiState = new UIState(this, descriptor);
			this.stateMap.put(descriptor, uiState);

			if (this.initialState == null) {
				this.initialState = uiState;
			}
		}
	
		return this.stateMap.get(descriptor);
	}
	
	public UIState getInitialState() {
		return initialState;
	}

	public UIState stateForEnvironment(AbstractUIEnvironment env) {
		return getState(Descriptor.forEnvironment(env));
	}
	
	public void mergeIn(UIStateGraph other) {
		for (Descriptor otherDescriptor : other.stateMap.keySet()) {
			UIState otherState = other.stateMap.get(otherDescriptor);
			getState(otherDescriptor).mergeIn(otherState);
		}
	}

	synchronized int getID() {
		return ++this.lastAssignedId;
	}
	
	public String toGraphViz() {
		return this.toGraphViz(new GraphVizEnvironment());
	}
	
	@Override
	public String toGraphViz(GraphVizEnvironment env) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("digraph G {\n");
		sb.append("compound=true\n");
		sb.append("fontname=\"Sans\"\n");	
		sb.append("rankdir=LR\n");	
		
		sb.append("\n");
		
		sb.append("edge [minlen=2.0, fontname=\"Sans\"]\n");
		sb.append("node [fontname=\"Sans\"]\n");	
		
		for (UIState state : this.stateMap.values()) {
			sb.append(state.toGraphViz(env));
		}
	
		for (AmbigueUIState state : this.ambigueStates) {
			sb.append(state.toGraphViz(env));
		}
		
		sb.append("\n");
		
		for (UIState state : this.stateMap.values()) {
			sb.append(state.graphVizEdges(env));
		}
	
		for (AmbigueUIState state : this.ambigueStates) {
			sb.append(state.graphVizEdges(env));
		}
	
		sb.append("}\n");
		
		return sb.toString();
	}

}
