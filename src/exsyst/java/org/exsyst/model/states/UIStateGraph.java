package org.exsyst.model.states;

import java.io.Serializable;
import java.util.*;

import org.exsyst.model.states.UIState.Descriptor;
import org.exsyst.run.AbstractUIEnvironment;
import org.exsyst.util.GraphVizDrawable;
import org.exsyst.util.GraphVizEnvironment;
import org.exsyst.util.YWorksDrawable;
import org.exsyst.util.YWorksEnvironment;

public class UIStateGraph implements GraphVizDrawable, YWorksDrawable, Serializable {
	private static final long serialVersionUID = 1L;

	private final Map<Descriptor, UIState> stateMap = Collections.synchronizedMap(new HashMap<Descriptor, UIState>());
	final Set<AmbigueUIState> ambigueStates = Collections.synchronizedSet(new HashSet<AmbigueUIState>());
	private int lastAssignedId = -1;
	private UIState initialState = null;	

	synchronized UIState getState(Descriptor descriptor) {		
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
	
	public synchronized void mergeIn(UIStateGraph other) {
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
	public synchronized String toGraphViz(GraphVizEnvironment env) {
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

	@Override
	public synchronized void addToYWorksEnvironment(YWorksEnvironment env) {
		for (UIState state : this.stateMap.values()) {
			state.addToYWorksEnvironment(env);
		}
	
		for (AmbigueUIState state : this.ambigueStates) {
			state.addToYWorksEnvironment(env);
		}

		for (UIState state : this.stateMap.values()) {
			state.addEdgesToYWorksEnvironment(env);
		}
	
		for (AmbigueUIState state : this.ambigueStates) {
			state.addEdgesToYWorksEnvironment(env);
		}
	}
	
	public YWorksEnvironment addToYWorksEnvironment() {
		YWorksEnvironment env = new YWorksEnvironment();
		this.addToYWorksEnvironment(env);
		return env;
	}

	@Override
	public void addEdgesToYWorksEnvironment(YWorksEnvironment env) {
	}
}
