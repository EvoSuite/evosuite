package com.examples.with.different.packagename.fm;

/**
 * Created by Andrea Arcuri on 06/10/15.
 */
public class SimpleFM_PackageMethodWithReturn {

    public static class PLM {
        private PLM(){}
        String foo() {
            throw new IllegalStateException("");
        }
    }

    public void bar(PLM plm){
        String foo = plm.foo(); //throw exception if not mocked
        System.out.println("To reach this, PLM should had been mocked: "+foo);
    }
}
