package de.unisb.cs.st.evosuite.ui.model;

import java.io.Serializable;
import java.util.List;

import org.uispec4j.UIComponent;

import y.view.NodeRealizer;
import de.unisb.cs.st.evosuite.ui.GraphVizDrawable;
import de.unisb.cs.st.evosuite.ui.GraphVizEnvironment;
import de.unisb.cs.st.evosuite.ui.YWorksEnvironment;
import de.unisb.cs.st.evosuite.ui.run.AbstractUIEnvironment;
import de.unisb.cs.st.evosuite.ui.run.BoundUIAction;
import de.unisb.cs.st.evosuite.ui.run.UIEnvironment;
import de.unisb.cs.st.evosuite.utils.HashUtil;
import de.unisb.cs.st.evosuite.utils.StringUtil;

public class DescriptorBoundUIAction<T extends UIComponent> implements GraphVizDrawable, Serializable {
	private static final long serialVersionUID = 1L;

	private UIActionTargetDescriptor target;
	private UIAction<T> action;

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
	
	public void randomize() {
		this.action.randomize();
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

	public void addToYWorksEnvironment(YWorksEnvironment env) {
		NodeRealizer realizer = env.getNodeRealizerFor(this);		
		realizer.setLabelText(this.action.graphVizString());
	}
}
