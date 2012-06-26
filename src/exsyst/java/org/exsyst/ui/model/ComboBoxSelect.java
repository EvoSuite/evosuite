package org.exsyst.ui.model;

import java.util.List;

import org.evosuite.utils.Randomness;
import org.uispec4j.ComboBox;
import org.uispec4j.UIComponent;

import org.exsyst.ui.run.AbstractUIEnvironment;

public class ComboBoxSelect extends UIAction<ComboBox> {
	// When the model contains less than this many values, we will
	// give each value a physically separate action, that can result
	// in a different target state...
	private static final int DISTINGUISH_BETWEEN_VALUE_TRESHOLD = 12;
	
	private static final long serialVersionUID = 1L;
	protected String value;
	private List<String> values;

	public ComboBoxSelect(List<String> values) {
		this.values = values;
		this.randomize();
	}

	public ComboBoxSelect(List<String> values, String value) {
		this.values = values;
		this.value = value;
	}

	@Override
	public boolean randomize() {
		boolean changed = false;
		
		if (this.values != null) {
			this.value = Randomness.choice(this.values);
			changed |= true;
		}
		
		changed |= super.randomize();
		return changed;
	}

	@Override
	public void executeOn(AbstractUIEnvironment env, final ComboBox comboBox) {
		this.checkTarget(comboBox);

		this.run(env, new Runnable() {
			@Override
			public void run() {
				comboBox.select(ComboBoxSelect.this.value);
			}
		});
	}

	public static void addActions(WindowlessUIActionTargetDescriptor targetDescriptor, List<UIAction<? extends UIComponent>> result) {
		List<String> values = targetDescriptor.getCriteria().getComboBoxValues();
		
		if (values == null)
			return;
		
		if (values.size() < DISTINGUISH_BETWEEN_VALUE_TRESHOLD) {
			for (String value : values) {
				result.add(new ComboBoxSelect(values, value));
			}
		} else {
			result.add(new ComboBoxSelect(values));
		}
	}

	@Override
	public String toString() {
		return String.format("ComboBoxSelect[%s]", this.value);
	}

	@Override
	public int hashCode() {
		if (this.values.size() >= DISTINGUISH_BETWEEN_VALUE_TRESHOLD)
			return super.hashCode();
		
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this.values.size() >= DISTINGUISH_BETWEEN_VALUE_TRESHOLD)
			return super.equals(obj);

		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComboBoxSelect other = (ComboBoxSelect) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
