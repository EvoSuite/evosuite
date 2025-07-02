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
package org.evosuite.runtime.mock.java.audio;

import org.evosuite.runtime.mock.OverrideMock;

import javax.sound.sampled.FloatControl;


/**
 * Mock implementation of Java's BooleanControl for testing purposes.
 *
 * @author Pedro-PFerreira
 */
public class MockFloatControl extends FloatControl implements OverrideMock {
    public MockFloatControl(Type type, float minimum, float maximum, float precision, int updatePeriod, float initialValue, String units) {
        super(type, minimum, maximum, precision, updatePeriod, initialValue, units);
    }

    @Override
    public void setValue(float value) {
        super.setValue(value);
    }

    @Override
    public float getValue() {
        return super.getValue();
    }

    @Override
    public float getMinimum() {
        return super.getMinimum();
    }

    @Override
    public float getMaximum() {
        return super.getMaximum();
    }

    @Override
    public float getPrecision() {
        return super.getPrecision();
    }

    @Override
    public int getUpdatePeriod() {
        return super.getUpdatePeriod();
    }

    @Override
    public String getUnits() {
        return super.getUnits();
    }

    @Override
    public void shift(float delta, float value, int updatePeriod) {
        super.shift(delta, value, updatePeriod);
    }
}
