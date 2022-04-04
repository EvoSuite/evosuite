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
package org.evosuite.seeding;

import org.evosuite.Properties;
import org.evosuite.utils.LoggingUtils;
import org.objectweb.asm.Type;

/**
 * Created by gordon on 06/01/2017.
 */
public class StaticConstantVariableProbabilityPool implements ConstantPool {


    private final FrequencyBasedPool<String> stringPool = new FrequencyBasedPool<>();

    private final FrequencyBasedPool<Type> typePool = new FrequencyBasedPool<>();

    private final FrequencyBasedPool<Integer> intPool = new FrequencyBasedPool<>();

    private final FrequencyBasedPool<Double> doublePool = new FrequencyBasedPool<>();

    private final FrequencyBasedPool<Long> longPool = new FrequencyBasedPool<>();

    private final FrequencyBasedPool<Float> floatPool = new FrequencyBasedPool<>();

    public StaticConstantVariableProbabilityPool() {
        /*
         * all pools HAVE to be non-empty
         */

        stringPool.addConstant("");

        if (Properties.TARGET_CLASS != null && !Properties.TARGET_CLASS.isEmpty()) {
            typePool.addConstant(Type.getObjectType(Properties.TARGET_CLASS));
        } else {
            typePool.addConstant(Type.getType(Object.class));
        }

        intPool.addConstant(0);
        intPool.addConstant(1);
        intPool.addConstant(-1);

        longPool.addConstant(0L);
        longPool.addConstant(1L);
        longPool.addConstant(-1L);

        floatPool.addConstant(0.0f);
        floatPool.addConstant(1.0f);
        floatPool.addConstant(-1.0f);

        doublePool.addConstant(0.0);
        doublePool.addConstant(1.0);
        doublePool.addConstant(-1.0);
    }

    /**
     * <p>
     * getRandomString
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getRandomString() {
        return stringPool.getRandomConstant();
    }

    @Override
    public Type getRandomType() {
        return typePool.getRandomConstant();
    }

    /**
     * <p>
     * getRandomInt
     * </p>
     *
     * @return a int.
     */
    @Override
    public int getRandomInt() {
        return intPool.getRandomConstant();
    }

    /**
     * <p>
     * getRandomFloat
     * </p>
     *
     * @return a float.
     */
    @Override
    public float getRandomFloat() {
        return floatPool.getRandomConstant();
    }

    /**
     * <p>
     * getRandomDouble
     * </p>
     *
     * @return a double.
     */
    @Override
    public double getRandomDouble() {
        return doublePool.getRandomConstant();
    }

    /**
     * <p>
     * getRandomLong
     * </p>
     *
     * @return a long.
     */
    @Override
    public long getRandomLong() {
        return longPool.getRandomConstant();
    }

    /**
     * <p>
     * add
     * </p>
     *
     * @param object a {@link java.lang.Object} object.
     */
    @Override
    public void add(Object object) {
        // We don't add null because this is explicitly handled in the TestFactory
        if (object == null)
            return;

        if (object instanceof String) {
            String string = (String) object;
            if (string.length() > Properties.MAX_STRING)
                return;
            // String literals are constrained to 65535 bytes
            // as they are stored in the constant pool
            if (string.length() > 65535)
                return;
            stringPool.addConstant(string);
        } else if (object instanceof Type) {
            while (((Type) object).getSort() == Type.ARRAY) {
                object = ((Type) object).getElementType();
            }
            typePool.addConstant((Type) object);
        } else if (object instanceof Integer) {
            if (Properties.RESTRICT_POOL) {
                int val = (Integer) object;
                if (Math.abs(val) < Properties.MAX_INT) {
                    intPool.addConstant((Integer) object);
                }
            } else {
                intPool.addConstant((Integer) object);
            }
        } else if (object instanceof Long) {
            if (Properties.RESTRICT_POOL) {
                long val = (Long) object;
                if (Math.abs(val) < Properties.MAX_INT) {
                    longPool.addConstant((Long) object);
                }
            } else {
                longPool.addConstant((Long) object);
            }
        } else if (object instanceof Float) {
            if (Properties.RESTRICT_POOL) {
                float val = (Float) object;
                if (Math.abs(val) < Properties.MAX_INT) {
                    floatPool.addConstant((Float) object);
                }
            } else {
                floatPool.addConstant((Float) object);
            }
        } else if (object instanceof Double) {
            if (Properties.RESTRICT_POOL) {
                double val = (Double) object;
                if (Math.abs(val) < Properties.MAX_INT) {
                    doublePool.addConstant((Double) object);
                }
            } else {
                doublePool.addConstant((Double) object);
            }
        } else {
            LoggingUtils.getEvoLogger().info("Constant of unknown type: "
                    + object.getClass());
        }
    }

}
