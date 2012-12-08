package org.exsyst.model;

import java.awt.Component;
import java.io.Serializable;
import java.util.*;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.StringUtils;
import org.evosuite.utils.ArrayUtil;
import org.evosuite.utils.HashUtil;
import org.uispec4j.ComboBox;
import org.uispec4j.Panel;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;

import org.exsyst.run.FocusOrder;

class WindowlessUIActionTargetDescriptor implements Serializable {
	private static final long serialVersionUID = 1L;

	static class Criteria extends HashMap<String, String> implements Serializable {
		public static boolean isUnset(String string) {
			return string == null || string.isEmpty();
		}

		private int comboBoxValueCount;

		public static Criteria forComponent(UIComponent comp) {
			assert (comp != null);

			Component awtComp = comp.getAwtComponent();
			Criteria result = new Criteria();
			String label = comp.getLabel();

			if (comp instanceof Window) {
				Window window = (Window) comp;
				label = window.getTitle();
			}

			if (isUnset(label) && awtComp instanceof AbstractButton) {
				Action action = ((AbstractButton) awtComp).getAction();

				if (action != null) {
					label = action.getValue(Action.NAME).toString();
				} else {
					label = ((AbstractButton) awtComp).getText();

					if (isUnset(label)) {
						Icon icon = ((AbstractButton) awtComp).getIcon();

						if (icon != null) {
							label = icon.toString();
						}
					}
				}
			}

			if (comp instanceof ComboBox) {
				ComboBox comboBox = (ComboBox) comp;

				try {
					StringBuilder newLabel = new StringBuilder();
					/*		comboBox.getRenderedValue(comboBox.getAwtComponent().getSelectedItem(), -1)
					);*/
					
					newLabel.append("[");

					int valueCount = comboBox.getAwtComponent().getModel().getSize();
					result.comboBoxValueCount = valueCount;

					for (int idx = 0; idx < valueCount; idx++) {
						String value = comboBox.getRenderedValue(idx);
						
						if (idx != 0)
							newLabel.append(", ");

						newLabel.append(value);

						if (idx > 5) {
							newLabel.append(", ");
							newLabel.append(valueCount - idx);
							newLabel.append(" more...");
							break;
						}

						idx++;
					}

					newLabel.append("]");

					label = newLabel.toString();
				} catch (Throwable t) { /* OK */ }
			}

			// Note: the following criteria do not matter in selecting action
			// targets.
			// However, we might want to consider these criteria for state
			// differentiation in the future.
			// (Actions whose outcome depends on application state we can
			// observe in the ui... see comment in WindowDescriptor)

			// if (comp instanceof TextBox) {
			// TextBox textBox = (TextBox) comp;
			// label = textBox.getText();
			// }
			//
			// if (comp instanceof Tree || comp instanceof ListBox || comp
			// instanceof Table) {
			// try {
			// Method getContent =
			// comp.getClass().getDeclaredMethod("getContent");
			// getContent.setAccessible(true);
			// label = getContent.invoke(comp);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// }

			if (isUnset(label))
				label = null;
			else {
				// XXX Hack: Maps "Undo nameOfAnAction" to plain "Undo" to avoid
				// state explosion
				if (label.contains("Rückgängig")
						|| label.contains("Widerrufen")
						|| label.contains("Undo")) {
					label = "Undo";
				} else if (label.contains("Wiederholen")
						|| label.contains("Redo")) {
					label = "Redo";
				}
			}

			result.put("class", awtComp.getClass().getName());
			result.put("label", label);
			result.put("name", !(comp instanceof Window) ? comp.getName()
					: null);

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

				String criteria = parts.isEmpty() ? "" : String.format("[%s]",
						StringUtils.join(parts, ", "));

				return String.format("%s%s", this.get("class"), criteria);
			} else {
				return super.toString();
			}
		}

		public int getComboBoxValueCount() {
			return this.comboBoxValueCount;
		}
	}

	/**
	 * Swing TextComponents are 'enabled' (according to the terms of the
	 * UIComponent method) even when they can not be edited. This method only
	 * returns true if a component can be edited.
	 * 
	 * @param comp
	 * @return
	 */
	public static boolean isComponentEnabled(UIComponent comp) {
		assert (comp != null);

		Component awtComp = comp.getAwtComponent();
		boolean enabled = awtComp.isEnabled();

		if (enabled && awtComp instanceof JTextComponent) {
			JTextComponent textComp = (JTextComponent) awtComp;
			boolean editable = textComp.isEditable();
			enabled &= editable;
		}

		return enabled;
	}

	public static List<WindowlessUIActionTargetDescriptor> allFor(Window window) {
		List<WindowlessUIActionTargetDescriptor> result = new LinkedList<WindowlessUIActionTargetDescriptor>();

		Iterable<UIComponent> children = FocusOrder.children(window);

		for (UIComponent comp : children) {
			if (comp != null && isComponentEnabled(comp)) {
				result.add(new WindowlessUIActionTargetDescriptor(children,
						comp));
			}
		}

		return result;
	}

	private static int matchIdxFor(Iterable<UIComponent> children,
			UIComponent comp, Criteria criteria) {
		Component targetComp = comp.getAwtComponent();

		int matchIdx = -1;

		for (UIComponent curComp : children) {
			if (curComp != null && criteria.match(curComp)) {
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
	private String description;

	public WindowlessUIActionTargetDescriptor(Iterable<UIComponent> children,
			UIComponent comp) {
		assert (children != null);
		assert (comp != null);

		this.criteria = Criteria.forComponent(comp);
		this.matchIdx = matchIdxFor(children, comp, criteria);
		this.type = comp.getClass();
		this.description = String.format("%s: %s",
				comp.getClass().toString(),
				comp.getAwtComponent().toString());
	}

	public UIComponent resolve(Panel container) {
		int curMatchIdx = -1;

		for (UIComponent curComp : FocusOrder.children(container)) {
			if (curComp != null && this.criteria.match(curComp)) {
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
		for (UIActionTargetDescriptor desc : windowDescriptor
				.getActionTargetDescriptors()) {
			if (this.canResolve(desc.getTargetDescriptor())) {
				return true;
			}
		}

		return false;
	}

	public boolean canResolve(
			WindowlessUIActionTargetDescriptor targetDescriptor) {
		return this.equals(targetDescriptor);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof WindowlessUIActionTargetDescriptor)) {
			return false;
		}

		WindowlessUIActionTargetDescriptor other = (WindowlessUIActionTargetDescriptor) obj;
		return this.matchIdx == other.matchIdx
				&& this.criteria.equals(other.criteria);
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
		return String.format("WindowlessUIActionTargetDescriptor[%s]",
				this.innerString());
	}

	public Class<? extends UIComponent> getType() {
		return this.type;
	}

	public Criteria getCriteria() {
		return this.criteria;
	}

	public String getDescription() {
		return this.description;
	}
}
