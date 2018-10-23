package com.examples.with.different.packagename.coverage;

public class MethodReturnsWrapper {

    public Boolean testBoolean(int x) {
        return (x > 0);
    }

    public Integer testInt(int x, int y) {
        return x + y;
    }

    public Byte testByte(byte x, byte y) {
        return (byte) (x + y);
    }

    public Long testLong(long x, long y) {
        return (long) (x - y);
    }

    public Character testChar(int x, int y) {
        if (x == y)
            return 'a';
        else if (x > y)
            return '1';
        else
            return ' ';
    }
}
