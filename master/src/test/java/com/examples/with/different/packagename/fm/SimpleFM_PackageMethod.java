package com.examples.with.different.packagename.fm;

/**
 * Created by Andrea Arcuri on 06/10/15.
 */
public class SimpleFM_PackageMethod {

    public static class PLM {
        void foo() {
            throw new IllegalStateException("");
        }
    }

    public void bar(PLM plm){
        plm.foo(); //throw exception if not mocked
        System.out.println("To reach this, PLM should had been mocked");
    }
}
