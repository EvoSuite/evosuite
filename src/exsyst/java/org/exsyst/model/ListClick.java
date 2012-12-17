package org.exsyst.model;

import java.util.List;

import org.evosuite.utils.Randomness;
import org.uispec4j.Key;
import org.uispec4j.ListBox;
import org.uispec4j.UIComponent;
import org.uispec4j.Key.Modifier;

import org.exsyst.run.AbstractUIEnvironment;

public class ListClick extends UIAction<ListBox> {
	private static final long serialVersionUID = 1L;

	enum Mode {
		LeftClick,
		RightClick,
		DoubleClick
	}

	private double rowRand;
	private Mode mode;
	private Modifier modifier;

	ListClick(Mode mode) {
		assert(mode != null);
		this.mode = mode;
	}

	@Override
	public void executeOn(AbstractUIEnvironment env, final ListBox list) {
		this.checkTarget(list);

		this.run(env, new Runnable() {
			@Override
			public void run() {
				int rowIdx = (int) (ListClick.this.rowRand * list.getSize());

				switch (ListClick.this.mode) {
				case LeftClick:
					list.click(rowIdx, ListClick.this.modifier);
					break;
				
				case RightClick:
					list.rightClick(rowIdx);
					break;
					
				case DoubleClick:
					list.doubleClick(rowIdx);
					break;
				}
			}
		});
	}
	
	@Override
	public boolean randomize() {
		this.rowRand = Randomness.nextDouble();
		this.modifier = Randomness.nextDouble() > 0.5 ?
				Randomness.choice(Key.Modifier.ALT, Key.Modifier.CONTROL, Key.Modifier.META, Key.Modifier.SHIFT) :
					Key.Modifier.NONE;
		
		super.randomize();
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
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
		ListClick other = (ListClick) obj;
		if (mode != other.mode)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s[%.4f]", this.graphVizString(), rowRand);
	}
	
	@Override
	public String graphVizString() {
		switch (this.mode) {
		case LeftClick: return "ListClick";
		case RightClick: return "ListRightClick";
		case DoubleClick: return "ListDoubleClick";
		default: return "ListUnknownClick";
		}
	}

	public static void addActions(List<UIAction<? extends UIComponent>> toList) {
		toList.add(new ListClick(Mode.LeftClick));
		toList.add(new ListClick(Mode.RightClick));
		toList.add(new ListClick(Mode.DoubleClick));
	}
}
