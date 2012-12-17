package org.exsyst.model;

import java.awt.Component;
import java.awt.Container;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.evosuite.utils.HashUtil;
import org.evosuite.utils.ListUtil;
import org.evosuite.utils.StringUtil;
import org.uispec4j.Window;

import y.view.NodeRealizer;

import org.exsyst.model.WindowlessUIActionTargetDescriptor.Criteria;
import org.exsyst.run.AbstractUIEnvironment;
import org.exsyst.run.UIEnvironment;
import org.exsyst.util.GraphVizDrawable;
import org.exsyst.util.GraphVizEnvironment;
import org.exsyst.util.YWorksDrawable;
import org.exsyst.util.YWorksEnvironment;

public class WindowDescriptor implements GraphVizDrawable, YWorksDrawable, Serializable {
	private static final long serialVersionUID = 1L;

	public static WindowDescriptor forWindow(Window window, List<Window> windows) {
		return new WindowDescriptor(window, windows);
	}

	private static int matchIdxFor(Criteria windowCriteria,
	        List<WindowlessUIActionTargetDescriptor> childrenDescriptors,
	        List<Window> windows, Window targetWindow) {
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

	private final Criteria windowCriteria;
	private final List<WindowlessUIActionTargetDescriptor> windowlessActionTargetDescriptors;
	private final List<UIActionTargetDescriptor> actionTargetDescriptors;
	private final int matchIdx;
	private final String description;

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

	private static String deepToString(Component awtComponent) {
		StringBuilder result = new StringBuilder();
		deepToString(awtComponent, result, 0);
		return result.toString();
	}

	private static void deepToString(Component awtComponent, StringBuilder result,
	        int depth) {
		for (int i = 0; i < depth; i++)
			result.append(" ____");

		result.append("* ");
		result.append(StringEscapeUtils.escapeXml(awtComponent.toString()));
		result.append("\n");

		if (awtComponent instanceof Container) {
			Container awtContainer = (Container) awtComponent;

			for (Component childComp : awtContainer.getComponents()) {
				deepToString(childComp, result, depth + 1);
			}
		}
	}

	private WindowDescriptor(Window window, List<Window> windows) {
		this.windowCriteria = WindowlessUIActionTargetDescriptor.Criteria.forComponent(window);
		this.windowlessActionTargetDescriptors = WindowlessUIActionTargetDescriptor.allFor(window);
		this.matchIdx = matchIdxFor(this.windowCriteria,
		                            this.windowlessActionTargetDescriptors, windows,
		                            window);
		this.description = deepToString(window.getAwtComponent());

		this.actionTargetDescriptors = new ArrayList<UIActionTargetDescriptor>(
		        this.windowlessActionTargetDescriptors.size());

		for (WindowlessUIActionTargetDescriptor windowlessDescriptor : this.windowlessActionTargetDescriptors) {
			this.actionTargetDescriptors.add(new UIActionTargetDescriptor(this,
			        windowlessDescriptor));
		}
	}

	private Window resolveWindowList(List<Window> windows) {
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
		return this.resolveWindowList(env.getTargetableWindows());
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
	private static boolean doesMatch(Criteria matchCriteria,
	        List<WindowlessUIActionTargetDescriptor> matchChildren, Window window) {
		Criteria otherCriteria = WindowlessUIActionTargetDescriptor.Criteria.forComponent(window);
		List<WindowlessUIActionTargetDescriptor> otherChildren = WindowlessUIActionTargetDescriptor.allFor(window);

		return matchCriteria.equals(otherCriteria) && matchChildren.equals(otherChildren);
	}

	public boolean actionTargetEquals(WindowDescriptor other) {
		return this.matchIdx == other.matchIdx
		        && this.windowCriteria.equals(other.windowCriteria)
		        && this.windowlessActionTargetDescriptors.equals(other.windowlessActionTargetDescriptors);
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
		return HashUtil.hashCode(this.matchIdx, this.windowCriteria,
		                         this.windowlessActionTargetDescriptors);
	}

	private String innerString() {
		return String.format("match #%d of criteria = %s", this.matchIdx,
		                     this.windowCriteria);
	}

	@Override
	public String toString() {
		return String.format("WindowDescriptor[%s, children = %s]", this.innerString(),
		                     this.windowlessActionTargetDescriptors);
	}

	public Object shortString() {
		return String.format("WindowDescriptor[%s, children = ...]", this.innerString());
	}

	@Override
	public String toGraphViz(GraphVizEnvironment env) {
		String id = env.getId(this);
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("subgraph cluster_%s {\n", id));
		sb.append(String.format("rankdir=LR\nstyle=filled\ncolor=black\nfillcolor=slategray1\nlabel=\"%s\"\n\n",
		                        StringUtil.escapeQuotes("Window: " + this.innerString())));

		for (UIActionTargetDescriptor td : this.actionTargetDescriptors) {
			sb.append(td.toGraphViz(env));
			sb.append("\n");
		}

		if (this.actionTargetDescriptors.isEmpty()) {
			sb.append(String.format("dummy_%s [label=\"\",style=invis]\n", id));
		}

		sb.append("}\n");

		return sb.toString();
	}

	public List<UIActionTargetDescriptor> getActionTargetDescriptors() {
		return Collections.unmodifiableList(this.actionTargetDescriptors);
	}

	@Override
	public void addToYWorksEnvironment(YWorksEnvironment env) {
		NodeRealizer realizer = env.realizerPushGroupNodeFor(this);
		realizer.setLabelText("Window: " + this.innerString());

		env.setDescription(env.getGroupNodeFor(this), this.description);

		for (UIActionTargetDescriptor td : this.actionTargetDescriptors) {
			td.addToYWorksEnvironment(env);
		}

		env.popGroupNode();
	}

	@Override
	public void addEdgesToYWorksEnvironment(YWorksEnvironment env) {
	}
}
