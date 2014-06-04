package org.evosuite.symbolic;

public class Fraction {

    /** A fraction representing "1/5". */
    public static final Fraction ONE_FIFTH = new Fraction(1, 5);

    /** The denominator. */
    private final int denominator;
    
    /** The numerator. */
    private final int numerator;
    
    /**
     * Create a fraction given the numerator and denominator.  The fraction is
     * reduced to lowest terms.
     * @param num the numerator.
     * @param den the denominator.
     * @throws ArithmeticException if the denominator is <code>zero</code>
     */
    public Fraction(int num, int den) {
        if (den == 0) {
            throw MathRuntimeException.createArithmeticException("zero denominator in fraction {0}/{1}",
                                                                 num, den);
        }
        if (den < 0) {
            if (num == Integer.MIN_VALUE || den == Integer.MIN_VALUE) {
                throw MathRuntimeException.createArithmeticException("overflow in fraction {0}/{1}, cannot negate",
                                                                     num, den);
            }
            num = -num;
            den = -den;
        }
        // reduce numerator and denominator by greatest common denominator.
        final int d = MathUtils.gcd(num, den);
        if (d > 1) {
            num /= d;
            den /= d;
        }
        
        // move sign to numerator.
        if (den < 0) {
            num = -num;
            den = -den;
        }
        this.numerator   = num;
        this.denominator = den;
    }

}
