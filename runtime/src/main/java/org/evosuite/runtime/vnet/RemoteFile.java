package org.evosuite.runtime.vnet;

import org.evosuite.runtime.mock.java.io.MockIOException;

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
        return new RemoteFileInputStream();
    }


    private class RemoteFileInputStream extends InputStream{

        private final AtomicInteger pos;
        private volatile boolean closed;

        public RemoteFileInputStream(){
            pos = new AtomicInteger(0);
            closed = false;
        }

        @Override
        public int read() throws IOException{
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
}
