package com.examples.with.different.packagename.idnaming;

/**
 * @author jmr
 * Created on 19/06/15.
 */
public class SimpleIdNaming {
    private boolean isPositive(int aNumber) {
        if (aNumber >= 0)
            return true;
        else
            return false;
    }

    public void foo(int aNumber) {
        if (isPositive(aNumber)) {
            bar(aNumber);
        } else {
            mist(aNumber);
        }
    }

    private void mist(int x) {};

    private void bar(int x) {};
}
