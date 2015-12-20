package com.examples.with.different.packagename.fm;


/**
 * Created by foo on 20/12/15.
 */
public class SimpleFM_GenericsReturnWithExtend_Single {

    public interface B {
    }

    public interface W extends B {
        boolean isW();
    }

    public interface A{
        void setB(B b);
        <C extends B> C getB();
    }


    public static boolean foo(A a){
        W w = a.getB();

        if(w.isW()){
            System.out.println("W");
            return true;
        } else {
            return false;
        }

    }
}
