package com.examples.with.different.packagename.fm;

/**
 * Created by foo on 19/12/15.
 */
public class SimpleFM_GenericsReturnWithExtend {

    public interface B {
    }

    private class W implements B {
        public boolean isW(){return true;}
    }

    private class Z implements B{
        public boolean isZ(){return true;}
    }

    public interface A{
        void setB(B b);
        <C extends B> C getB();
    }


    public static boolean foo(A a){
        W w = a.getB();
        Z z = a.getB();

        return w.isW() && z.isZ();
    }
}
