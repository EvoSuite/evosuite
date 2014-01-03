package org.evosuite.setup;

/**
 * This class represents GETSTATIC edges in the static usage graph.
 *  
 * @author galeotti
 *
 */
final class StaticFieldReadEntry extends GetStaticGraphEntry {

	public StaticFieldReadEntry(String sourceClass, String sourceMethod,
			String targetClass, String targetField) {
		super(sourceClass, sourceMethod, targetClass);
		this.targetField = targetField;
	}

	private final String targetField;

	public String getTargetField() {
		return targetField;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((targetField == null) ? 0 : targetField.hashCode());
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
		if (!super.equals(obj))
			return false;
		StaticFieldReadEntry other = (StaticFieldReadEntry) obj;
		if (targetField == null) {
			if (other.targetField != null)
				return false;
		} else if (!targetField.equals(other.targetField))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getSourceClass() + "." + getSourceMethod() + " -> "
				+ getTargetClass() + "." + targetField;
	}
}
