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

import org.evosuite.runtime.Randomness;
import org.evosuite.runtime.mock.StaticReplacementMock;

import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of Java's Line for testing purposes.
 * This ensures audio line is generated with random data.
 *
 * @author Pedro-PFerreira
 */
public class MockLine implements Line, StaticReplacementMock {

    private static boolean isOpen = false;

    private static List<LineListener> lineListeners = new ArrayList<>();

    @Override
    public Info getLineInfo() {
        return new Info(Line.class);
    }

    @Override
    public void open() throws LineUnavailableException {

        if (isOpen) {
            throw new LineUnavailableException("Line is already open");
        }

        isOpen = true;
    }

    @Override
    public void close() {

        isOpen = false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public Control[] getControls() {

        int size = Randomness.nextInt(1, Integer.MAX_VALUE);

        Control[] controls = new MockControl[size];

        for (int i = 0; i < size; i++) {
            controls[i] = new MockControl();
        }

        return controls;
    }

    @Override
    public boolean isControlSupported(Control.Type type) {

        if (type == null) {
            throw new NullPointerException();
        }

        return true;
    }

    @Override
    public Control getControl(Control.Type type) {
        if (type == null) {
            throw new NullPointerException();
        }

        if (isControlSupported(type)) {

            Control[] controls = this.getControls();

            for (Control control : controls){

                if (control.getType().equals(type)){
                    return control;
                }
            }
        }

        return null;
    }

    @Override
    public void addLineListener(LineListener lineListener) {

        if (lineListener == null) {
            throw new NullPointerException();
        }

        lineListeners.add(lineListener);
    }

    @Override
    public void removeLineListener(LineListener lineListener) {

        if (lineListener == null) {
            throw new NullPointerException();
        }

        lineListeners.remove(lineListener);
    }

    @Override
    public String getMockedClassName() {
        return MockLine.class.getName();
    }
}
