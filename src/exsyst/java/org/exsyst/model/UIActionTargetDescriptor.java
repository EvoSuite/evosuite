package org.exsyst.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.evosuite.utils.HashUtil;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;

import y.view.NodeRealizer;

import org.exsyst.run.AbstractUIEnvironment;
import org.exsyst.util.GraphVizDrawable;
import org.exsyst.util.GraphVizEnvironment;
import org.exsyst.util.YWorksDrawable;
import org.exsyst.util.YWorksEnvironment;

public class UIActionTargetDescriptor implements GraphVizDrawable, YWorksDrawable, Serializable {
	private static final long serialVersionUID = 1L;

	private WindowDescriptor windowDescriptor;
	private WindowlessUIActionTargetDescriptor targetDescriptor;

	public UIActionTargetDescriptor(WindowDescriptor windowDescriptor,
			WindowlessUIActionTargetDescriptor targetDescriptor) {
		this.windowDescriptor = windowDescriptor;
		this.targetDescriptor = targetDescriptor;
	}

	public List<DescriptorBoundUIAction<? extends UIComponent>> getActions() {
		List<DescriptorBoundUIAction<? extends UIComponent>> result = new LinkedList<DescriptorBoundUIAction<? extends UIComponent>>();
		
		for (UIAction<? extends UIComponent> action : UIAction.actionsForDescriptor(this.targetDescriptor)) {
			result.add(action.bind(this));
		}
		
		return result;
	}

	public UIComponent resolve(AbstractUIEnvironment env) {
		Window window = this.windowDescriptor.resolve(env);
		return window == null ? null : this.targetDescriptor.resolve(window);
	}

	public boolean canResolve(List<WindowDescriptor> windowDescriptors) {
		for (WindowDescriptor wd : windowDescriptors) {
			if (this.windowDescriptor.canResolve(wd)) {
				if (this.targetDescriptor.canResolve(wd)) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof UIActionTargetDescriptor)) {
			return false;
		}

		UIActionTargetDescriptor other = (UIActionTargetDescriptor) obj;
		return this.targetDescriptor.equals(other.targetDescriptor) && this.windowDescriptor.actionTargetEquals(other.windowDescriptor);
	}

	@Override
	public int hashCode() {
		return HashUtil.hashCode(this.targetDescriptor, this.windowDescriptor);
	}

	@Override
	public String toString() {
		return String.format("UIActionTargetDescriptor[%s in %s]", this.targetDescriptor.innerString(), this.windowDescriptor.shortString());
	}

	@Override
	public String toGraphViz(GraphVizEnvironment env) {
		String id = env.getId(this);
		return String.format("%s [style=filled,shape=rect,color=black,fillcolor=lightcyan,label=%s]", id, env.quoteString(this.targetDescriptor.innerString()));
	}

	public Map<String,String> getCriteria() {
		return this.targetDescriptor.getCriteria();
	}
	
	public WindowlessUIActionTargetDescriptor getTargetDescriptor() {
		return targetDescriptor;
	}

	@Override
	public void addToYWorksEnvironment(YWorksEnvironment env) {
		NodeRealizer realizer = env.getNodeRealizerFor(this);
		realizer.setLabelText(this.targetDescriptor.innerString());
		env.setDescription(env.getNodeFor(this), this.targetDescriptor.getDescription());
	}

	@Override
	public void addEdgesToYWorksEnvironment(YWorksEnvironment env) {
	}
}
