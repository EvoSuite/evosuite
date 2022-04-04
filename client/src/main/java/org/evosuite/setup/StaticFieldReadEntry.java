/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.setup;

/**
 * This class represents GETSTATIC edges in the static usage graph.
 *
 * @author galeotti
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
        if (getClass() != obj.getClass())
            return false;
        if (!super.equals(obj))
            return false;
        StaticFieldReadEntry other = (StaticFieldReadEntry) obj;
        if (targetField == null) {
            return other.targetField == null;
        } else return targetField.equals(other.targetField);
    }

    @Override
    public String toString() {
        return getSourceClass() + "." + getSourceMethod() + " -> "
                + getTargetClass() + "." + targetField;
    }
}
