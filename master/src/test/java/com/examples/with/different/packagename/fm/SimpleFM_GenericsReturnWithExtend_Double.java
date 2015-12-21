package com.examples.with.different.packagename.fm;

/**
 * Created by foo on 19/12/15.
 */
public class SimpleFM_GenericsReturnWithExtend_Double {

    public interface B {
    }

    public interface W extends B {
        boolean isW();
    }

    public interface Z extends B{
        boolean isZ();
    }

    public interface A{
        void setB(B b);
        <C extends B> C getB();
    }


    public static boolean foo(A a){
        W w = a.getB();
        Z z = a.getB();

        if(w.isW()&& z.isZ()){
            System.out.println("W and Z");
            return true;
        } else {
            return false;
        }
    }
}
