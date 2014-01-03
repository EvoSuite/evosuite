package org.evosuite.setup;

/**
 * This class represents INVOKESTATIC edges in the static usage graph.
 * 
 * @author galeotti
 *
 */
final class StaticMethodCallEntry extends GetStaticGraphEntry {

	private final String targetMethod;

	public StaticMethodCallEntry(String sourceClass, String sourceMethod,
			String targetClass, String targetMethod) {
		super(sourceClass, sourceMethod, targetClass);
		this.targetMethod = targetMethod;
	}

	public String getTargetMethod() {
		return targetMethod;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((targetMethod == null) ? 0 : targetMethod.hashCode());
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
		StaticMethodCallEntry other = (StaticMethodCallEntry) obj;
		if (targetMethod == null) {
			if (other.targetMethod != null)
				return false;
		} else if (!targetMethod.equals(other.targetMethod))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getSourceClass() + "." + getSourceMethod() + " -> "
				+ getTargetClass() + "." + targetMethod;
	}

}
