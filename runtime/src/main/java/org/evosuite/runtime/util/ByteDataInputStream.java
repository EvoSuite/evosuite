/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.runtime.util;

import org.evosuite.runtime.mock.java.io.MockIOException;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class used to create an input stream for a byte array
 *
 * Created by Andrea Arcuri on 22/05/15.
 */
public class ByteDataInputStream  extends InputStream{

    private final byte[] data;
    private final AtomicInteger pos;
    private volatile boolean closed;

    public ByteDataInputStream(byte[] data) throws IllegalArgumentException{
        if(data==null){
            throw new IllegalArgumentException("Null input");
        }
        this.data = data;
        pos = new AtomicInteger(0);
        closed = false;
    }

    public ByteDataInputStream(String text) throws NullPointerException{
        this(text.getBytes());
    }

    @Override
    public int read() throws IOException {
        throwExceptionIfClosed();

        int available = available();
        if(available <= 0){
            return -1; // never block
        } else {
            int index = pos.getAndIncrement();
            return data[index];
        }
    }


    @Override
    public int available() throws IOException {
        throwExceptionIfClosed();
        int dif = data.length - pos.get();
        return dif;
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

    @Override
    public synchronized void mark(int readlimit) {}

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    private void throwExceptionIfClosed() throws IOException{
        if(closed){
            throw new MockIOException();
        }
    }
}
