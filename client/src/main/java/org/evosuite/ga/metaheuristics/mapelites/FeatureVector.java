/**
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 * <p>
 * This file is part of EvoSuite.
 * <p>
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 * <p>
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.ga.metaheuristics.mapelites;

import org.apache.commons.lang3.ClassUtils;
import org.evosuite.assertion.Inspector;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Felix Prasse
 */
public final class FeatureVector implements Serializable {

    private static final long serialVersionUID = 1L;

    private final static class Entry implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String name;
        private final Object value;
        private final int group;

        public Entry(Inspector inspector, Object instance) {
            Object value;
            try {
                value = inspector.getValue(instance);
            } catch (Exception e) {
                value = null;
            }

            this.value = value;
            this.name = inspector.getMethodCall();
            this.group = this.calculateGroup();
        }

        public int getGroup() {
            return this.group;
        }

        private int calculateGroup() {
            if (value == null) {
                return 0;
            }

            if (value instanceof Integer) {
                return ((Integer) value).compareTo(0);
            } else if (value instanceof Short) {
                return ((Short) value).compareTo((short) 0);
            } else if (value instanceof Byte) {
                return ((Byte) value).compareTo((byte) 0);
            } else if (value instanceof Long) {
                return ((Long) value).compareTo(0L);
            } else if (value instanceof Float) {
                return ((Float) value).compareTo(0f);
            } else if (value instanceof Double) {
                return ((Double) value).compareTo(0d);
            } else if (value instanceof String) {
                return ((String) value).isEmpty() ? 0 : 1;
            } else if (value instanceof Character) {
                return Character.isLetterOrDigit((Character) value) ? 1 : 0;
            } else if (value instanceof Boolean) {
                return (Boolean) value ? 1 : 0;
            } else if (value instanceof Enum) {
                return ((Enum<?>) value).ordinal();
            } else {
                throw new RuntimeException("Unsupported type: " + value.getClass().getName());
            }
        }

        @Override
        public String toString() {
            return name + "=" + value;
        }

        @Override
        public int hashCode() {
            return getGroup() * name.hashCode();
        }

        public boolean equals(Entry other) {
            return getGroup() == other.getGroup() && name.equals(other.name);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Entry && equals((Entry) obj);
        }
    }

    private final Entry[] features;

    public FeatureVector(final Inspector[] inspectors, final Object instance) {
        this.features = new Entry[inspectors.length];

        for (int i = 0; i < inspectors.length; ++i) {
            this.features[i] = new Entry(inspectors[i], instance);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.features);
    }

    public boolean equals(FeatureVector other) {
        return Arrays.equals(this.features, other.features);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FeatureVector && equals((FeatureVector) obj);
    }

    @Override
    public String toString() {
        return Arrays.toString(features);
    }

    private static int getPossibilityCountForType(Class<?> type) {
        final Class<?> wrappedType = ClassUtils.primitiveToWrapper(type);

        int amount = 0;

        if (Character.class.isAssignableFrom(wrappedType)) {
            amount = 1;
        } else if (Boolean.class.isAssignableFrom(wrappedType)) {
            amount = 2;
        } else if (String.class.isAssignableFrom(wrappedType)) {
            amount = 2;
        } else if (wrappedType.isEnum()) {
            amount = wrappedType.getEnumConstants().length;
        } else if (Number.class.isAssignableFrom(wrappedType)) {
            amount = 3;
        }

        if (!type.isPrimitive()) {
            amount += 1;
        }

        return amount;
    }

    public static int getPossibilityCount(final Inspector[] inspectors) {
        return Arrays
                .stream(inspectors)
                .mapToInt(i -> getPossibilityCountForType(i.getReturnType()))
                .reduce(1, Math::multiplyExact);
    }

}
