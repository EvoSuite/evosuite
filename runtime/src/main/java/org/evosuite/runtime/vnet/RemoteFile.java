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
