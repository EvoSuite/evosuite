package de.unisb.cs.st.evosuite.ui.model;

import org.uispec4j.Table;

import de.unisb.cs.st.evosuite.ui.run.AbstractUIEnvironment;
import de.unisb.cs.st.evosuite.utils.Randomness;

class TableClick extends UIAction<Table> {
	private static final long serialVersionUID = 1L;

	public static TableClick newLeftClick() {
		return new TableClick(false);
	}

	public static TableClick newRightClick() {
		return new TableClick(true);
	}

	private double rowRand;
	private double colRand;
	private boolean isRightClick;
	
	TableClick(boolean isRightClick) {
		this.isRightClick = isRightClick;
	}

	@Override
	public void executeOn(AbstractUIEnvironment env, final Table table) {
		this.checkTarget(table);

		this.run(env, new Runnable() {
			@Override
			public void run() {
				int rowIdx = (int) (TableClick.this.rowRand * table.getRowCount());
				int colIdx = (int) (TableClick.this.colRand * table.getColumnCount());

				if (TableClick.this.isRightClick) {
					table.rightClick(rowIdx, colIdx);
				} else {
					table.click(rowIdx, colIdx);
				}
			}
		});

	}

	@Override
	public void randomize() {
		this.rowRand = Randomness.nextDouble();
		this.colRand = Randomness.nextDouble();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isRightClick ? 1231 : 1237);
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
		if (isRightClick != other.isRightClick)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s[%.4f, %.4f]", this.graphVizString(), rowRand, colRand);
	}
	
	@Override
	public String graphVizString() {
		return isRightClick ? "TableRightClick" : "TableClick";
	}
}
