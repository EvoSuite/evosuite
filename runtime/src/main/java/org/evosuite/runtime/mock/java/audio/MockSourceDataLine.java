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

import static org.evosuite.runtime.mock.java.audio.MockAudioUtils.generateRandomContent;

/**
 * Mock implementation of Java's SourceDataLine for testing purposes.
 * This ensures audio data is generated in memory without disk access.
 *
 * <p>All objects are created in memory, and no access to disk is ever done.</p>
 *
 * @author Pedro-PFerreira
 */
public class MockSourceDataLine implements SourceDataLine, StaticReplacementMock {
    private boolean isOpen;
    private boolean isRunning;
    private boolean isActive;
    private byte[] dataBuffer;
    private List<LineListener> lineListeners;

    static{
        String seed = System.getenv("SEED_FOR_MOCKS");
        if (seed != null) {
            Randomness.setSeed(Long.parseLong(seed));
        }
    }

    /**
     * Default constructor for MockSourceDataLine.
     * Initializes the line as closed, not running, and active.
     * Generates a random data buffer for audio content.
     */
    public MockSourceDataLine() {

        this.isOpen = false;
        this.isRunning = false;
        this.isActive = true;
        this.dataBuffer = MockAudioUtils.generateRandomContent(MockAudioUtils.generateSampleRate(), Randomness.nextInt(1, 2));
        this.lineListeners = new ArrayList<>();
    }

    /**
     * Opens the line with the specified audio format and buffer size.
     *
     * @param audioFormat the audio format to use
     * @param i           the buffer size
     * @throws LineUnavailableException if the line cannot be opened
     */
    @Override
    public void open(AudioFormat audioFormat, int i) throws LineUnavailableException {

        if (audioFormat == null) {
            throw new NullPointerException("AudioFormat is null");
        }

        if (i < 0) {
            throw new IllegalArgumentException("Buffer size is negative");
        }

        if (!this.isOpen) {
            this.isOpen = true;
            this.isActive = true;
            this.dataBuffer = new byte[i];
        }
    }

    /**
     * Opens the line with the specified audio format.
     *
     * @param audioFormat the audio format to use
     * @throws LineUnavailableException if the line cannot be opened
     */
    @Override
    public void open(AudioFormat audioFormat) throws LineUnavailableException {
        if (audioFormat == null) {
            throw new NullPointerException("AudioFormat is null");
        }

        if (!this.isOpen) {
            this.isOpen = true;
            this.isActive = true;
            this.dataBuffer = generateRandomContent(audioFormat.getSampleRate(), audioFormat.getChannels());
        }

        for(LineListener lineListener : this.lineListeners) {
            lineListener.update(new LineEvent(this, LineEvent.Type.OPEN, 0L));
        }

        this.start();
    }

    /**
     * Reads audio data into the specified byte array.
     * @param bytes the byte array to read data into
     * @param i the starting index in the byte array
     * @param i1 the number of bytes to read
     * @return the number of bytes read
     */
    @Override
    public int write(byte[] bytes, int i, int i1) {
        if (this.isOpen() && this.isActive) {
            if (bytes != null && i >= 0 && i1 > 0 && i + i1 <= bytes.length) {
                if (this.dataBuffer == null) {
                    this.dataBuffer = new byte[i1];
                } else if (this.dataBuffer.length < i1) {
                    this.dataBuffer = new byte[i1];
                } else {
                    System.arraycopy(bytes, i, this.dataBuffer, 0, i1);
                }

                return i1;
            } else {
                throw new IllegalArgumentException("Invalid arguments");
            }
        } else {
            throw new IllegalStateException("Line is not open");
        }
    }

    /**
     * Drains the line, clearing the data buffer.
     * This method should be called when the line is running.
     */
    @Override
    public void drain() {
        if (this.isOpen() && this.isActive) {
            if (!this.isRunning) {
                throw new IllegalStateException("Line is not running");
            } else {
                this.dataBuffer = null;
            }
        }
    }

    /**
     * Flushes the line, clearing the data buffer.
     * This method should be called when the line is running.
     */
    @Override
    public void flush() {
        if (this.isOpen() && this.isActive) {
            if (!this.isRunning) {
                throw new IllegalStateException("Line is not running");
            } else {
                this.dataBuffer = null;
            }
        }
    }

    /**
     * Starts the line, allowing it to process audio data.
     * This method should be called after the line is opened.
     */
    @Override
    public void start() {
        if (this.isOpen() && this.isActive) {
            this.isRunning = true;
            this.dataBuffer = new byte[0];

            for(LineListener lineListener : this.lineListeners) {
                lineListener.update(new LineEvent(this, LineEvent.Type.START, 0L));
            }
        }
    }

    /**
     * Stops the line, halting audio processing.
     * This method should be called when the line is running.
     */
    @Override
    public void stop() {
        if (this.isOpen() && this.isActive) {
            if (!this.isRunning) {
                throw new IllegalStateException("Line is not running");
            } else {
                this.isRunning = false;
            }
        }
    }

    /**
     * Checks if the line is currently running.
     *
     * @return true if the line is running, false otherwise
     */
    @Override
    public boolean isRunning() {
        return this.isRunning;
    }

    /**
     * Checks if the line is currently active.
     *
     * @return true if the line is active, false otherwise
     */
    @Override
    public boolean isActive() {
        return this.isActive;
    }

    /**
     * Gets the audio format of the line.
     *
     * @return the audio format
     */
    @Override
    public AudioFormat getFormat() {
        return new MockAudioFormat();
    }

    /**
     * Gets the size of the data buffer.
     *
     * @return the size of the data buffer
     */
    @Override
    public int getBufferSize() {
        return this.dataBuffer.length;
    }

    /**
     * Gets the number of bytes that can be written to the line.
     *
     * @return the number of bytes available for writing
     */
    @Override
    public int available() {
        return 0;
    }

    /**
     * Gets the current frame position of the line.
     *
     * @return the current frame position
     */
    @Override
    public int getFramePosition() {
        return 0;
    }

    /**
     * Gets the current long frame position of the line.
     *
     * @return the current long frame position
     */
    @Override
    public long getLongFramePosition() {
        return 0L;
    }

    /**
     * Gets the current microsecond position of the line.
     *
     * @return the current microsecond position
     */
    @Override
    public long getMicrosecondPosition() {
        return 0L;
    }

    /**
     * Gets the current level of the line.
     * This method is not implemented in this mock.
     *
     * @return the current level, always 0.0F
     */
    @Override
    public float getLevel() {
        return 0.0F;
    }

    /**
     * Gets the line information for this SourceDataLine.
     *
     * @return the line information
     */
    @Override
    public Line.Info getLineInfo() {
        return new MockLineInfo(SourceDataLine.class);
    }

    /**
     * Opens the line.
     * This method should be called before starting the line.
     *
     * @throws LineUnavailableException if the line cannot be opened
     */
    @Override
    public void open() throws LineUnavailableException {
        if (!this.isOpen() && !this.isActive) {
            this.isOpen = true;
        } else {
            throw new LineUnavailableException("Line is already open");
        }
        this.start();
    }

    /**
     * Closes the line, stopping any audio processing and releasing resources.
     * This method should be called when the line is no longer needed.
     *
     * @throws IllegalStateException if the line is already closed
     */
    @Override
    public void close() {

        if (!this.isOpen() && !this.isActive) {
            throw new IllegalStateException("Line is already closed");
        }

        this.isOpen = false;
        this.isActive = false;
    }

    /**
     * Checks if the line is currently open.
     *
     * @return true if the line is open, false otherwise
     */
    @Override
    public boolean isOpen() {
        return this.isOpen;
    }

    /**
     * Gets the controls available for this SourceDataLine.
     * This method generates random controls for testing purposes.
     *
     * @return an array of Control objects
     */
    @Override
    public Control[] getControls() {
        int maxNumberOfDecimals = Randomness.nextInt(1, 3);
        int max = Randomness.nextInt(Integer.MAX_VALUE);
        int min = -max;

        int initialValue = Randomness.nextInt(0, max);

        // Randomly decide whether to make the initial value negative
        if (Randomness.nextBoolean()){
            initialValue = -initialValue;
        }
        // Ensure initialValue is within the range of min and max
        Control[] controls = new Control[2];
        MockFloatControl mockFloatControl = new MockFloatControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN, (float)min, (float)max, (float)maxNumberOfDecimals, max, (float)initialValue, "dB");
        MockBooleanControl mockBooleanControl = new MockBooleanControl(javax.sound.sampled.BooleanControl.Type.MUTE);
        mockBooleanControl.setValue(Randomness.nextBoolean());
        controls[0] = mockFloatControl;
        controls[1] = mockBooleanControl;
        return controls;
    }

    /**
     * Checks if a specific control type is supported by this SourceDataLine.
     *
     * @param type the control type to check
     * @return true if the control type is supported, false otherwise
     * @throws NullPointerException if the type is null
     */
    @Override
    public boolean isControlSupported(Control.Type type) {
        if (type == null) {
            throw new NullPointerException();
        } else {
            return true;
        }
    }

    /**
     * Gets a specific control by its type.
     *
     * @param type the control type to retrieve
     * @return the Control object if supported, null otherwise
     * @throws NullPointerException if the type is null
     */
    @Override
    public Control getControl(Control.Type type) {
        if (type == null) {
            throw new NullPointerException();
        } else {
            if (this.isControlSupported(type)) {
                Control[] controls = this.getControls();

                for(Control control : controls) {
                    if (control.getType().equals(type)) {
                        return control;
                    }
                }
            }

            return null;
        }
    }

    /**
     * Adds a LineListener to this SourceDataLine.
     *
     * @param lineListener the LineListener to add
     * @throws NullPointerException if the lineListener is null
     */
    @Override
    public void addLineListener(LineListener lineListener) {
        if (lineListener == null) {
            throw new NullPointerException("LineListener is null");
        } else {
            this.lineListeners.add(lineListener);
        }
    }

    /**
     * Removes a LineListener from this SourceDataLine.
     *
     * @param lineListener the LineListener to remove
     * @throws NullPointerException if the lineListener is null
     */
    @Override
    public void removeLineListener(LineListener lineListener) {
        if (lineListener == null) {
            throw new NullPointerException("LineListener is null");
        } else {
            this.lineListeners.remove(lineListener);
        }
    }

    /**
     * Returns the name of the mocked class.
     *
     * @return the name of the mocked class
     */
    public String getMockedClassName() {
        return "javax.sound.sampled.SourceDataLine";
    }
}
