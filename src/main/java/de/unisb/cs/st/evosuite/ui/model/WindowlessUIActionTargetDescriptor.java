package de.unisb.cs.st.evosuite.ui.model;

import java.awt.Component;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.uispec4j.Panel;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;

import de.unisb.cs.st.evosuite.ui.FocusOrder;
import de.unisb.cs.st.evosuite.utils.ArrayUtil;
import de.unisb.cs.st.evosuite.utils.HashUtil;

class WindowlessUIActionTargetDescriptor implements Serializable {
	private static final long serialVersionUID = 1L;

	static class Criteria extends HashMap<String, String> {
		public static Criteria forComponent(UIComponent comp) {
			assert (comp != null);
			
			Component awtComp = comp.getAwtComponent();
			Criteria result = new Criteria();
			String label = comp.getLabel();

			if (comp instanceof Window) {
				Window window = (Window) comp;
				label = window.getTitle();
			}
			
			// Note: the following criteria do not matter in selecting action targets.
			// However, we might want to consider these criteria for state differentiation in the future.
			// (Actions whose outcome depends on application state we can observe in the ui... see comment in WindowDescriptor)
			
//			if (comp instanceof TextBox) {
//				TextBox textBox = (TextBox) comp;
//				label = textBox.getText();
//			}
//
//			if (comp instanceof Tree || comp instanceof ListBox || comp instanceof Table) {
//				try {
//					Method getContent = comp.getClass().getDeclaredMethod("getContent");
//					getContent.setAccessible(true);
//					label = getContent.invoke(comp);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}

			result.put("class", awtComp.getClass().getName());
			result.put("label", label);
			result.put("name", !(comp instanceof Window) ? comp.getName() : null);
			
			return result;
		}

		private static final long serialVersionUID = 1L;

		private Criteria() {
			super();
		}

		public boolean match(UIComponent component) {
			return Criteria.forComponent(component).equals(this);
		}
		
		public boolean match(UIActionTargetDescriptor desc) {
			return desc.getCriteria().equals(this);
		}
		
		@Override
		public String toString() {
			Set<String> knownKeys = ArrayUtil.asSet("class", "label", "name");
				
			if (this.keySet().equals(knownKeys)) {
				String label = this.get("label");
				String name = this.get("name");
				
				List<String> parts = new LinkedList<String>();

				if (label != null) {
					parts.add(String.format("\"%s\"", label));
				}
				
				if (name != null) {
					parts.add(String.format("name=%s", name));
				}
				
				String criteria = parts.isEmpty() ? "" : String.format("[%s]", StringUtils.join(parts, ", "));

				return String.format("%s%s", this.get("class"), criteria);
			} else {
				return super.toString();
			}
		}
	}

	public static List<WindowlessUIActionTargetDescriptor> allFor(Window window) {
		List<WindowlessUIActionTargetDescriptor> result = new LinkedList<WindowlessUIActionTargetDescriptor>();
		
		for (UIComponent comp : FocusOrder.children(window)) {
			result.add(new WindowlessUIActionTargetDescriptor(window, comp));
		}
		
		return result;
	}

	private static int matchIdxFor(Panel container, UIComponent comp, Criteria criteria) {
		Component targetComp = comp.getAwtComponent();

		int matchIdx = -1;
		
		for (UIComponent curComp : FocusOrder.children(container)) {
			if (criteria.match(curComp)) {
				matchIdx++;
				
				if (curComp.getAwtComponent() == targetComp) {
					return matchIdx;
				}
			}
		}
		
		assert (false);
		return -1;
	}

	private Criteria criteria;
	private int matchIdx;
	private Class<? extends UIComponent> type;

	public WindowlessUIActionTargetDescriptor(Window window, UIComponent comp) {
		assert (window != null);
		assert (comp != null);
		
		this.criteria = Criteria.forComponent(comp);
		this.matchIdx = matchIdxFor(window, comp, criteria);
		this.type = comp.getClass();
	}
	
	public UIComponent resolve(Panel container) {
		int curMatchIdx = -1;
		
		for (UIComponent curComp : FocusOrder.children(container)) {
			if (this.criteria.match(curComp)) {
				curMatchIdx++;
				
				if (curMatchIdx == this.matchIdx) {
					assert (type.isAssignableFrom(curComp.getClass()));
					return curComp;
				}
			}
		}
		
		return null;
	}
	
	public boolean canResolve(WindowDescriptor windowDescriptor) {
		for (UIActionTargetDescriptor desc : windowDescriptor.getActionTargetDescriptors()) {
			if (this.canResolve(desc.getTargetDescriptor())) {
				return true;
			}
		}
		
		return false;
	}

	public boolean canResolve(WindowlessUIActionTargetDescriptor targetDescriptor) {
		return this.equals(targetDescriptor);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof WindowlessUIActionTargetDescriptor)) {
			return false;
		}
		
		WindowlessUIActionTargetDescriptor other = (WindowlessUIActionTargetDescriptor) obj;
		return this.matchIdx == other.matchIdx && this.criteria.equals(other.criteria);
	}

	@Override
	public int hashCode() {
		return HashUtil.hashCode(this.matchIdx, this.criteria.hashCode());
	}

	String innerString() {
		return String.format("match #%d of %s", this.matchIdx, this.criteria);
	}
	
	@Override
	public String toString() {
		return String.format("WindowlessUIActionTargetDescriptor[%s]", this.innerString());
	}

	public Class<? extends UIComponent> getType() {
		return this.type;
	}

	public Map<String,String> getCriteria() {
		return this.criteria;
	}
}
