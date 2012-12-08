package org.exsyst.model;

import java.util.List;

import javax.swing.JComboBox;

import org.evosuite.utils.Randomness;
import org.uispec4j.ComboBox;
import org.uispec4j.UIComponent;

import org.exsyst.run.AbstractUIEnvironment;

public class ComboBoxSelect extends UIAction<ComboBox> {
	// When the model contains less than this many values, we will
	// give each value a physically separate action, that can result
	// in a different target state...
	private static final int DISTINGUISH_BETWEEN_VALUE_TRESHOLD = 12;
	
	private static final long serialVersionUID = 1L;
	protected double valueOff;
	protected boolean isTangible = false;

	public ComboBoxSelect() {
		this.randomize();
	}

	public ComboBoxSelect(int valueCount, int valueIdx) {
		this.valueOff = ((double)valueIdx) / valueCount;
		this.isTangible = true;
	}

	@Override
	public boolean randomize() {
		this.valueOff = Randomness.nextDouble();
		super.randomize();
		
		return true;
	}

	@Override
	public void executeOn(AbstractUIEnvironment env, final ComboBox comboBox) {
		this.checkTarget(comboBox);

		this.run(env, new Runnable() {
			@Override
			public void run() {
				// This is optimized not to use org.uispec4j.ComboBox.select(String) by design:
				// In a profile of the TerpWord application 50% of all CPU time was spent
				// extracting and comparing Strings in there.
				JComboBox<?> jCombo = comboBox.getAwtComponent();
				int size = jCombo.getModel().getSize();
				jCombo.setSelectedIndex((int) (ComboBoxSelect.this.valueOff * size));
			}
		});
	}

	public static void addActions(WindowlessUIActionTargetDescriptor targetDescriptor, List<UIAction<? extends UIComponent>> result) {
		int valueCount = targetDescriptor.getCriteria().getComboBoxValueCount();

		if (valueCount < DISTINGUISH_BETWEEN_VALUE_TRESHOLD) {
			for (int i = 0; i < valueCount; i++) {
				result.add(new ComboBoxSelect(valueCount, i));
			}
		} else {
			result.add(new ComboBoxSelect());
		}
	}

	@Override
	public String toString() {
		return String.format("ComboBoxSelect[%s]", this.valueOff);
	}
	
	@Override
	public int hashCode() {
		if (!this.isTangible)
			return super.hashCode();

		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(this.valueOff);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComboBoxSelect other = (ComboBoxSelect) obj;
		if (isTangible != other.isTangible)
			return false;
		if (Double.doubleToLongBits(valueOff) != Double.doubleToLongBits(other.valueOff))
			return false;
		return true;
	}
}
