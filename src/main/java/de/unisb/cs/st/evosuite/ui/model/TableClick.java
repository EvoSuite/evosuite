package de.unisb.cs.st.evosuite.ui.model;

import java.util.List;

import org.uispec4j.Key.Modifier;
import org.uispec4j.Key;
import org.uispec4j.Table;
import org.uispec4j.UIComponent;

import de.unisb.cs.st.evosuite.ui.run.AbstractUIEnvironment;
import de.unisb.cs.st.evosuite.utils.Randomness;

class TableClick extends UIAction<Table> {
	private static final long serialVersionUID = 1L;

	private static final TableClick TABLE_LEFT_CLICK = new TableClick(Mode.LeftClick);
	private static final TableClick TABLE_RIGHT_CLICK = new TableClick(Mode.RightClick);
	private static final TableClick TABLE_DOUBLE_CLICK = new TableClick(Mode.DoubleClick);

	enum Mode {
		LeftClick,
		RightClick,
		DoubleClick
	}
	
	private double rowRand;
	private double colRand;
	private Mode mode;
	private Modifier modifier;
	
	TableClick(Mode mode) {
		assert(mode != null);
		this.mode = mode;
	}

	@Override
	public void executeOn(AbstractUIEnvironment env, final Table table) {
		this.checkTarget(table);

		this.run(env, new Runnable() {
			@Override
			public void run() {
				int rowIdx = (int) (TableClick.this.rowRand * table.getRowCount());
				int colIdx = (int) (TableClick.this.colRand * table.getColumnCount());

				switch (TableClick.this.mode) {
				case LeftClick:
					table.click(rowIdx, colIdx, TableClick.this.modifier);
					break;
				
				case RightClick:
					table.rightClick(rowIdx, colIdx);
					break;
					
				case DoubleClick:
					table.doubleClick(rowIdx, colIdx);
					break;
				}
			}
		});

	}

	@Override
	public boolean randomize() {
		this.rowRand = Randomness.nextDouble();
		this.colRand = Randomness.nextDouble();
		this.modifier = Randomness.nextDouble() > 0.5 ? Randomness.choice(Key.Modifier.ALT, Key.Modifier.CONTROL, Key.Modifier.META, Key.Modifier.SHIFT) : Key.Modifier.NONE;
		
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
		TableClick other = (TableClick) obj;
		if (mode != other.mode)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s[%.4f, %.4f]", this.graphVizString(), rowRand, colRand);
	}
	
	@Override
	public String graphVizString() {
		switch (this.mode) {
		case LeftClick: return "TableClick";
		case RightClick: return "TableRightClick";
		case DoubleClick: return "TableDoubleClick";
		default: return "TableUnknownClick";
		}
	}

	public static void addActions(List<UIAction<? extends UIComponent>> toList) {
		toList.add(TABLE_LEFT_CLICK);
		toList.add(TABLE_RIGHT_CLICK);
		toList.add(TABLE_DOUBLE_CLICK);
	}
}
