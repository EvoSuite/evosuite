package org.exsyst.model.states;

import java.io.Serializable;
import java.util.*;

import org.evosuite.utils.HashUtil;
import org.uispec4j.Window;

import y.view.Arrow;
import y.view.EdgeRealizer;
import y.view.NodeRealizer;

import org.exsyst.model.DescriptorBoundUIAction;
import org.exsyst.model.UIActionTargetDescriptor;
import org.exsyst.model.WindowDescriptor;
import org.exsyst.run.AbstractUIEnvironment;
import org.exsyst.util.GraphVizDrawable;
import org.exsyst.util.GraphVizEnvironment;
import org.exsyst.util.YWorksDrawable;
import org.exsyst.util.YWorksEnvironment;

public class UIState extends AbstractUIState implements GraphVizDrawable, YWorksDrawable {
	static class Descriptor extends LinkedList<WindowDescriptor> implements Serializable {
		private static final long serialVersionUID = 1L;

		private static Descriptor forWindows(List<Window> windows) {
			Descriptor result = new Descriptor();

			for (Window window : windows) {
				result.add(WindowDescriptor.forWindow(window, windows));
			}

			return result;
		}

		static Descriptor forEnvironment(AbstractUIEnvironment env) {
			/* Currently a state is only concerned with the windows it can actually interact with.
			 * It might make sense to add non-interactable windows for differentiating states to 
			 * out edges ambiguities. Also see the comment in WindowDescriptor. 
			 */
			return forWindows(env.getTargetableWindows());
		}
		
		private Descriptor() {
			super();
		}

		public String shortString() {
			StringBuilder sb = new StringBuilder("[");
			boolean isFirst = true;
			
			for (WindowDescriptor wd : this) {
				if (!isFirst) {
					sb.append(", ");
				}
				
				sb.append(wd.shortString());
				isFirst = false;
			}
			
			sb.append("]");
			return sb.toString();
		}
	}

	private static final long serialVersionUID = 1L;

	private UIStateGraph graph;
	private Descriptor descriptor;
	private Map<DescriptorBoundUIAction<?>, AbstractUIState> transitions = new HashMap<DescriptorBoundUIAction<?>, AbstractUIState>();
	private int id;

	private int timesVisited;

	UIState(UIStateGraph graph, Descriptor descriptor) {
		assert (descriptor != null);
		this.graph = graph;
		this.descriptor = descriptor;
		this.id = graph.getID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.gui.UIState#addTransition(de.unisb.cs.st.evosuite
	 * .gui.BoundUIAction, de.unisb.cs.st.evosuite.gui.RegularUIState)
	 */
	@Override
	public synchronized void addTransition(DescriptorBoundUIAction<?> action, UIState toState) {
		if (!this.transitions.containsKey(action)) {
			this.transitions.put(action, toState);
		} else if (!this.transitions.get(action).equals(toState)) {
			AbstractUIState prevState = this.transitions.get(action);
			
			if (prevState instanceof AmbigueUIState) {
				((AmbigueUIState) prevState).addPossibleState(toState);
			} else {
				AmbigueUIState newState = new AmbigueUIState(Arrays.asList(prevState, toState));
				this.graph.ambigueStates.add(newState);
				this.transitions.put(action, newState);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.unisb.cs.st.evosuite.gui.UIState#getTransition(de.unisb.cs.st.evosuite
	 * .gui.BoundUIAction)
	 */
	@Override
	public AbstractUIState getTransition(DescriptorBoundUIAction<?> action) {
		if (this.transitions.containsKey(action)) {
			return this.transitions.get(action);
		}
		
		return action.canResolve(this.getTargetableWindowDescriptors()) ? unknownUIState(this) : null;
	}

	@Override
	public List<WindowDescriptor> getTargetableWindowDescriptors() {
		return Collections.unmodifiableList(this.descriptor);
	}

	@Override
	public int hashCode() {
		return HashUtil.hashCode(this.descriptor);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || !(obj instanceof UIState)) {
			return false;
		}

		UIState other = (UIState) obj;
		return (this.descriptor == null ? other.descriptor == null : this.descriptor.equals(other.descriptor));
	}

	@Override
	public String toString() {
		return String.format("UIState[id=%d, descriptor=%s]", this.id, this.descriptor);
	}
	
	@Override
	public String shortString() {
		return String.format("UIState[id=%d]", this.id);
	}
	
	@Override
	public String graphVizId(GraphVizEnvironment env) {
		return String.format("cluster_state%d", this.id);
	}
	
	@Override
	public String toGraphViz(GraphVizEnvironment env) {
		String id = this.graphVizId(env);
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("subgraph %s {\n", id));
		sb.append(String.format("rankdir=LR\nstyle=filled\ncolor=black\nfillcolor=papayawhip\nlabel=\"UIState %s\"\n\n", this.id));

		sb.append(String.format("dummy_%s [label=\"\",style=invis,shape=none,margin=\"0,0\",width=0,height=0,fixedsize=true]\n\n", id));
		
		for (WindowDescriptor wd : this.descriptor) {
			sb.append(wd.toGraphViz(env));
			sb.append("\n");
		}
		
		sb.append("}\n");
		
		return sb.toString();
	}
	
	synchronized String graphVizEdges(GraphVizEnvironment env) {
		//String fromId = this.graphVizId(env);
		//String dummyFromId = "dummy_" + fromId;
		StringBuilder sb = new StringBuilder();
		
		for (DescriptorBoundUIAction<?> action : this.transitions.keySet()) {
			sb.append(action.toGraphViz(env));
			sb.append("\n");
		}

		sb.append("\n");
		
		for (DescriptorBoundUIAction<?> action : this.transitions.keySet()) {
			String actionId = env.getId(action);   
			String onId = env.getId(action.targetDescriptor());
			String toId = this.transitions.get(action).graphVizId(env);   
			String dummyToId = "dummy_" + toId;
			
			//sb.append(String.format("%s -> %s [ltail=\"%s\"]\n", dummyFromId, actionId, fromId));
			sb.append(String.format("%s -> %s [lhead=\"%s\"]\n", actionId, dummyToId, toId));

			sb.append(String.format("%s -> %s [arrowhead=dot,arrowtail=dot]\n", actionId, onId));
			
			sb.append("\n");			
		}
		
		return sb.toString();
	}

	@Override
	public void addToYWorksEnvironment(YWorksEnvironment env) {
		NodeRealizer nodeRealizer = env.realizerPushGroupNodeFor(this);
		nodeRealizer.setLabelText(String.format("UIState %s (%d x)", this.id, this.timesVisited));

		for (WindowDescriptor wd : this.descriptor) {
			wd.addToYWorksEnvironment(env);
		}
		
		env.popGroupNode();
	}
	
	@Override
	public synchronized void addEdgesToYWorksEnvironment(YWorksEnvironment env) {
		for (DescriptorBoundUIAction<?> action : this.transitions.keySet()) {
			action.addToYWorksEnvironment(env);
		}

		for (DescriptorBoundUIAction<?> action : this.transitions.keySet()) {
			EdgeRealizer edgeRealizer = env.getEdgeRealizerFor(action.targetDescriptor(), action);
			edgeRealizer.setSourceArrow(Arrow.CIRCLE);
			edgeRealizer.setTargetArrow(Arrow.CIRCLE);
			
			edgeRealizer = env.getEdgeRealizerFor(action, this.transitions.get(action));
			edgeRealizer.setArrow(Arrow.CONCAVE);
		}
	}

	@Override
	public Map<DescriptorBoundUIAction<?>, AbstractUIState> getTransitions() {
		return Collections.unmodifiableMap(this.transitions);
	}

	@Override
	public List<UIActionTargetDescriptor> getActionTargetDescriptors() {
		List<UIActionTargetDescriptor> result = new LinkedList<UIActionTargetDescriptor>();
		
		for (WindowDescriptor wd : this.getTargetableWindowDescriptors()) {
			result.addAll(wd.getActionTargetDescriptors());
		}
		
		return result;
	}

	private synchronized void mergeInInternal(DescriptorBoundUIAction<?> action, AbstractUIState toState) {
		if (toState instanceof AmbigueUIState) {
			this.mergeInInternal(action, (AmbigueUIState) toState);
		} else if (toState instanceof UIState) {
			this.mergeInInternal(action, (UIState) toState);			
		} else {
			throw new UnsupportedOperationException("Don't know how to merge state " + toState);
		}
	}

	private synchronized  void mergeInInternal(DescriptorBoundUIAction<?> action, AmbigueUIState ambigueUIState) {
		for (AbstractUIState possibleState : ambigueUIState.getPossibleStates()) {
			this.mergeInInternal(action, possibleState);
		}
	}

	private synchronized  void mergeInInternal(DescriptorBoundUIAction<?> action, UIState toState) {
		UIState newState = this.graph.getState(toState.descriptor);
		this.addTransition(action, newState);
	}
	
	public synchronized  void mergeIn(UIState otherState) {
		assert(otherState != null);
		assert(otherState.descriptor == this.descriptor);
		
		for (DescriptorBoundUIAction<?> action : otherState.transitions.keySet()) {
			AbstractUIState toState = otherState.transitions.get(action);
			this.mergeInInternal(action, toState);
		}
	}

	public void increaseTimesVisited() {
		this.timesVisited++;
	}
}
