package com.examples.with.different.packagename;

/**
 * Created by gordon on 10/05/2017.
 */
public interface AbstractEnumInInnerClass {

    enum AnEnum {

        FOO {
            @Override
            public boolean foo(int x) {
                return false;
            }
        },

        BAR {
            @Override
            public boolean foo(int x) {
                return true;
            }
        };

        public abstract boolean foo(int x);
    }
}
