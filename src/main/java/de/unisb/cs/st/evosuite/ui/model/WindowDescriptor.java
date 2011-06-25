package de.unisb.cs.st.evosuite.ui.model;

import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.uispec4j.Window;

import de.unisb.cs.st.evosuite.ui.AbstractUIEnvironment;
import de.unisb.cs.st.evosuite.ui.GraphVizDrawable;
import de.unisb.cs.st.evosuite.ui.GraphVizEnvironment;
import de.unisb.cs.st.evosuite.ui.UIEnvironment;
import de.unisb.cs.st.evosuite.ui.model.WindowlessUIActionTargetDescriptor.Criteria;
import de.unisb.cs.st.evosuite.utils.HashUtil;
import de.unisb.cs.st.evosuite.utils.ListUtil;
import de.unisb.cs.st.evosuite.utils.StringUtil;

public class WindowDescriptor implements GraphVizDrawable, Serializable {
	private static final long serialVersionUID = 1L;

	public static WindowDescriptor forWindow(Window window, List<Window> windows) {
		return new WindowDescriptor(window, windows);
	}

	private static int matchIdxFor(Criteria windowCriteria, List<WindowlessUIActionTargetDescriptor> childrenDescriptors, List<Window> windows, Window targetWindow) {
		Component targetComp = targetWindow.getAwtComponent();
		int matchIdx = -1;
		
		for (Window w : windows) {
			if (doesMatch(windowCriteria, childrenDescriptors, w)) {
				matchIdx++;
			}

			if (w.getAwtComponent() == targetComp) {
				return matchIdx;
			}
		}
		
		assert (false);
		return -1;
	}

	private Criteria windowCriteria;
	private List<WindowlessUIActionTargetDescriptor> windowlessActionTargetDescriptors;
	private List<UIActionTargetDescriptor> actionTargetDescriptors;
	private int matchIdx;
		
	/* TODO: Add state descriptors used for differentiating windows in UIStates.
	 * 
	 * These would allow us to differentiate between two calculators with the same
	 * action targets, but with different displayed numbers. While this does not
	 * matter at all for action selection, it could be that the outcome (read: target
	 * state!) of an action depends on such things as the displayed number...
	 * 
	 * It's not possible to fully capture an application's state by just looking at
	 * the user interface. So while this would get rid of some nondeterminism in
	 * state transitions, some nondeterminism would still remain based on application
	 * state that can not be observed from the user interface.
	 */
	
	private WindowDescriptor(Window window, List<Window> windows) {
		this.windowCriteria = WindowlessUIActionTargetDescriptor.Criteria.forComponent(window);
		this.windowlessActionTargetDescriptors = WindowlessUIActionTargetDescriptor.allFor(window);
		this.matchIdx = matchIdxFor(this.windowCriteria, this.windowlessActionTargetDescriptors, windows, window);
		
		this.actionTargetDescriptors = new ArrayList<UIActionTargetDescriptor>(this.windowlessActionTargetDescriptors.size());
		
		for (WindowlessUIActionTargetDescriptor windowlessDescriptor : this.windowlessActionTargetDescriptors) {
			this.actionTargetDescriptors.add(new UIActionTargetDescriptor(this, windowlessDescriptor));
		}
	}

	private Window resolve(List<Window> windows) {
		int curMatchIdx = -1;
		
		for (Window w : windows) {
			if (doesMatch(this.windowCriteria, this.windowlessActionTargetDescriptors, w)) {
				curMatchIdx++;
				
				if (curMatchIdx == this.matchIdx) {
					return w;
				}
			}
		}
		
		return null;
	}
	
	public Window resolve(AbstractUIEnvironment env) {
		return this.resolve(env.getTargetableWindows());
	}

	public WindowDescriptor resolve(List<WindowDescriptor> windowDescriptors) {
		return ListUtil.anyEquals(windowDescriptors, this) ? this : null;
	}

	public WindowDescriptor resolve(WindowDescriptor windowDescriptor) {
		return windowDescriptor.equals(this) ? this : null;
	}

	public boolean canResolve(UIEnvironment env) {
		return this.resolve(env) != null;
	}

	public boolean canResolve(List<WindowDescriptor> windowDescriptors) {
		return this.resolve(windowDescriptors) != null;
	}

	public boolean canResolve(WindowDescriptor windowDescriptor) {
		return this.resolve(windowDescriptor) != null;
	}

	
	/**
	 * @param window
	 * @return true if the given criteria match the given window
	 */
	private static boolean doesMatch(Criteria matchCriteria, List<WindowlessUIActionTargetDescriptor> matchChildren, Window window) {
		Criteria otherCriteria = WindowlessUIActionTargetDescriptor.Criteria.forComponent(window);
		List<WindowlessUIActionTargetDescriptor> otherChildren = WindowlessUIActionTargetDescriptor.allFor(window);

		return matchCriteria.equals(otherCriteria) && matchChildren.equals(otherChildren);
	}

	public boolean actionTargetEquals(WindowDescriptor other) {
		return this.matchIdx == other.matchIdx && this.windowCriteria.equals(other.windowCriteria) && this.windowlessActionTargetDescriptors.equals(other.windowlessActionTargetDescriptors);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof WindowDescriptor)) {
			return false;
		}
		
		WindowDescriptor other = (WindowDescriptor) obj;
		return this.actionTargetEquals(other);
	}

	@Override
	public int hashCode() {
		return HashUtil.hashCode(this.matchIdx, this.windowCriteria, this.windowlessActionTargetDescriptors);
	}

	private String innerString() {
		return String.format("match #%d of criteria = %s", this.matchIdx, this.windowCriteria);
	}
	
	@Override
	public String toString() {
		return String.format("WindowDescriptor[%s, children = %s]", this.innerString(), this.windowlessActionTargetDescriptors);
	}

	public Object shortString() {
		return String.format("WindowDescriptor[%s, children = ...]", this.innerString());
	}

	@Override
	public String toGraphViz(GraphVizEnvironment env) {
		String id = env.getId(this);
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("subgraph cluster_%s {\n", id));
		sb.append(String.format("rankdir=LR\nstyle=filled\ncolor=black\nfillcolor=slategray1\nlabel=\"%s\"\n\n", StringUtil.escapeQuotes("Window: " + this.innerString())));
		
		for (UIActionTargetDescriptor td : this.actionTargetDescriptors) {
			sb.append(td.toGraphViz(env));
			sb.append("\n");
		}
		
		sb.append("}\n");
		
		return sb.toString();
	}
	
	public List<UIActionTargetDescriptor> getActionTargetDescriptors() {
		return Collections.unmodifiableList(this.actionTargetDescriptors);
	}
}
