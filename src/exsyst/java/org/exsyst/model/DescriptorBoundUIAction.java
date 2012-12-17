package org.exsyst.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.utils.HashUtil;
import org.evosuite.utils.StringUtil;
import org.uispec4j.UIComponent;

import y.view.NodeRealizer;

import org.exsyst.run.AbstractUIEnvironment;
import org.exsyst.run.BoundUIAction;
import org.exsyst.run.UIEnvironment;
import org.exsyst.util.GraphVizDrawable;
import org.exsyst.util.GraphVizEnvironment;
import org.exsyst.util.YWorksDrawable;
import org.exsyst.util.YWorksEnvironment;

public class DescriptorBoundUIAction<T extends UIComponent> implements GraphVizDrawable, YWorksDrawable, Serializable {
	private static final long serialVersionUID = 1L;

	private UIActionTargetDescriptor target;
	private UIAction<T> action;

	private static Map<DescriptorBoundUIAction<?>, Integer> timesExecutedMap = Collections.synchronizedMap(
			new HashMap<DescriptorBoundUIAction<?>, Integer>());

	public DescriptorBoundUIAction(UIAction<T> action, UIActionTargetDescriptor target) {
		this.action = action;
		this.target = target;
	}

	@SuppressWarnings("unchecked")
	public BoundUIAction<T> resolve(UIEnvironment env) {
		return new BoundUIAction<T>(this.action, (T) this.target.resolve(env));
	}
	
	public boolean canResolve(UIEnvironment env) {
		return this.resolve(env) != null;
	}
	
	public boolean canResolve(List<WindowDescriptor> windowDescriptors) {
		return this.target.canResolve(windowDescriptors);
	}
	
	@SuppressWarnings("unchecked")
	public void execute(AbstractUIEnvironment env) {
		this.action.executeOn(env, (T) this.target.resolve(env));
	}
	
	public boolean randomize() {
		return this.action.randomize();
	}
	
	public UIActionTargetDescriptor targetDescriptor() {
		return this.target;
	}
	
	@Override
	public int hashCode() {
		return HashUtil.hashCode(this.action, this.target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || !(obj instanceof DescriptorBoundUIAction<?>)) {
			return false;
		}

		DescriptorBoundUIAction<T> other = (DescriptorBoundUIAction<T>) obj;
		return this.action.equals(other.action) && this.target.equals(other.target);
	}

	@Override
	public String toString() {
		return String.format("DescriptorBoundUIAction[%s on %s]", this.action, this.target);
	}

	@Override
	public String toGraphViz(GraphVizEnvironment env) {
		return String.format("%s [style=filled,color=black,fillcolor=lightgoldenrod1,label=\"%s\"]", env.getId(this), StringUtil.escapeQuotes(this.action.graphVizString()));
	}

	public String shortString() {
		return String.format("%s on %s", this.action.toString(), this.targetDescriptor().getCriteria().toString());
	}

	@Override
	public void addEdgesToYWorksEnvironment(YWorksEnvironment env) {
		/* Nothing to do here */
	}

	@Override
	public void addToYWorksEnvironment(YWorksEnvironment env) {
		NodeRealizer realizer = env.getNodeRealizerFor(this);		
		realizer.setLabelText(String.format("%s (%d x)", this.action.graphVizString(), this.getTimesExecuted()));
	}

	public int getTimesExecuted() {
		if (!timesExecutedMap.containsKey(this)) {
			timesExecutedMap.put(this, 0);
			return 0;
		}
		
		return timesExecutedMap.get(this);
	}
	
	public void increaseTimesExecuted() {
		timesExecutedMap.put(this, this.getTimesExecuted() + 1);
	}
}
