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

import javax.sound.sampled.BooleanControl;

/**
 * Mock implementation of Java's BooleanControl for testing purposes.
 *
 * @author Pedro-PFerreira
 */
public class MockBooleanControl extends BooleanControl implements OverrideMock {

    /**
     * Default constructor: Initializes the control with a specific type.
     *
     * @param type the type of the control
     */
    public MockBooleanControl(Type type) {
        super(type, false);
    }

    @Override
    public void setValue(boolean value) {
        super.setValue(value);
    }

    @Override
    public boolean getValue() {
        return super.getValue();
    }
}
