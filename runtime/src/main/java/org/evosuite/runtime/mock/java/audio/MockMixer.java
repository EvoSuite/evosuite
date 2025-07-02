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

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of Java's Mixer for testing purposes.
 * This ensures audio mixer is generated with random data.
 *
 * @author Pedro-PFerreira
 */
public class MockMixer implements Mixer, StaticReplacementMock {

    private boolean isOpen = false;
    private List<LineListener> lineListeners = new ArrayList<>();

    static {
        String seed = System.getenv("SEED_FOR_MOCKS");

        if (seed != null) {
            Randomness.setSeed(Long.parseLong(seed));
        }
    }

    @Override
    public Info getMixerInfo() {
        return new MockMixerInfo();
    }

    @Override
    public Line.Info[] getSourceLineInfo() {
        int size = Randomness.nextInt();

        Line.Info[] lines = new MockLineInfo[size];

        for (int i = 0; i < size; i++) {
            lines[i] = new MockLineInfo(MockMixer.class);
        }

        return lines;
    }

    @Override
    public Line.Info[] getTargetLineInfo() {
        int size = Randomness.nextInt();

        Line.Info[] lines = new MockLineInfo[size];

        for (int i = 0; i < size; i++) {
            lines[i] = new MockLineInfo(MockMixer.class);
        }

        return lines;
    }

    @Override
    public Line.Info[] getSourceLineInfo(Line.Info info) {

        if (info == null) {
            throw new NullPointerException();
        }

        Line.Info[] lines = getSourceLineInfo();
        ArrayList<Line.Info> matchedInfos = new ArrayList<>();

        for (Line.Info line : lines) {
            if (info.matches(line)){
                matchedInfos.add(line);
            }
        }

        return matchedInfos.toArray(new Line.Info[matchedInfos.size()]);
    }

    @Override
    public Line.Info[] getTargetLineInfo(Line.Info info) {
        if (info == null) {
            throw new NullPointerException();
        }

        Line.Info[] lines = getTargetLineInfo();
        ArrayList<Line.Info> matchedInfos = new ArrayList<>();

        for (Line.Info line : lines) {
            if (info.matches(line)){
                matchedInfos.add(line);
            }
        }

        return matchedInfos.toArray(new Line.Info[matchedInfos.size()]);
    }

    @Override
    public boolean isLineSupported(Line.Info info) {

        for (Line.Info line : getSourceLineInfo()) {
            if (line.matches(info)) {
                return true;
            }
        }

        for (Line.Info line : getTargetLineInfo()) {
            if (line.matches(info)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Line getLine(Line.Info info) throws LineUnavailableException {
        if (info == null) {
            throw new NullPointerException();
        }

        Line.Info lineInfo = this.getLineInfo();

        if (lineInfo != null && lineInfo.matches(info)) {
            return new MockDataLine();
        }

        return null;
    }

    @Override
    public int getMaxLines(Line.Info info) {
        Line.Info lineInfo = this.getLineInfo();

        if (lineInfo != null && lineInfo.matches(info)) {
            return 1;
        }

        return 0;
    }

    @Override
    public Line[] getSourceLines() {

        Line[] localLines = new MockDataLine[getSourceLines().length];

        for (int i = 0; i < localLines.length; i++) {
            localLines[i] = new MockDataLine();
        }

        return localLines;
    }

    @Override
    public Line[] getTargetLines() {
        Line[] localLines = new MockDataLine[getSourceLines().length];

        for (int i = 0; i < localLines.length; i++) {
            localLines[i] = new MockDataLine();
        }

        return localLines;
    }

    @Override
    public void synchronize(Line[] lines, boolean b) {
    }

    @Override
    public void unsynchronize(Line[] lines) {
    }

    @Override
    public boolean isSynchronizationSupported(Line[] lines, boolean b) {
        return false;
    }

    @Override
    public Line.Info getLineInfo() {
        return new MockLineInfo(MockMixer.class);
    }

    @Override
    public void open() throws LineUnavailableException {
        if (isOpen) {
            throw new LineUnavailableException("Mixer is already open");
        }

        isOpen = true;
    }

    @Override
    public void close() {

        if (!isOpen) {
            throw new IllegalStateException("Mixer is not open");
        }
        isOpen = false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public Control[] getControls() {

        int size = Randomness.nextInt(1, 10);

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

        for (Control control : getControls()) {
            if (control.getType().equals(type)) {
                return true;
            }
        }
        return false;
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

        if (lineListener == null || !lineListeners.contains(lineListener)) {
            throw new NullPointerException();
        }
        lineListeners.remove(lineListener);
    }

    @Override
    public String getMockedClassName() {
        return "javax.sound.sampled.Mixer";
    }
}
