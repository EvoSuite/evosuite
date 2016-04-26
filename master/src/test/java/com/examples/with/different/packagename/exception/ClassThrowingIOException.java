package com.examples.with.different.packagename.exception;

import java.io.IOException;

/**
 * Created by gordon on 19/03/2016.
 */
public class ClassThrowingIOException {

    private boolean shouldThrow = false;

    public ClassThrowingIOException(boolean shouldThrow) {
        this.shouldThrow = shouldThrow;
    }

    public char readChar() throws IOException {
        if(shouldThrow)
            throw new IOException();
        return (char)0;
    }
}
