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
package org.evosuite.runtime.vnet;

import org.evosuite.runtime.mock.java.io.MockIOException;
import org.evosuite.runtime.util.ByteDataInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class representing a file on a remote host.
 * A remote file can only be created once, and cannot be modified, just read.
 *
 * Created by arcuri on 12/6/14.
 */
public class RemoteFile {

    private final String url;
    private final byte[] data;


    public RemoteFile(String url, byte[] data) throws IllegalArgumentException{
        if(url == null || data == null){
            throw new IllegalArgumentException("Null inputs");
        }
        this.url = url;
        this.data = data;
    }

    public RemoteFile(String url, String data) throws IllegalArgumentException {
        this(url,data==null ? null : data.getBytes());
    }


    /**
     *
     * @return a new {@code InputStream} instance that can be used to read the content of this remote file
     */
    public InputStream getInputStream(){
        return new ByteDataInputStream(data);
    }

}
