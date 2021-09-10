package dk.brics.automaton;

/*
 * This class is not in the org.evosuite package,
 * as it has to access package level variables and classes in the
 * dk.brics.automaton package
 *
 */

public abstract class RegExpVisitor<K> {

    public final K visitRegExp(RegExp e) {
        switch (e.kind) {
            case REGEXP_ANYCHAR: {
                return visitAnyChar();
            }
            case REGEXP_ANYSTRING: {
                return visitAnyString();
            }
            case REGEXP_AUTOMATON: {
                return visitAutomaton(e);
            }
            case REGEXP_CHAR: {
                return visitChar(e.c);
            }
            case REGEXP_CHAR_RANGE: {
                return visitCharRange(e.from, e.to);
            }
            case REGEXP_COMPLEMENT: {
                return visitComplement(e.exp1);
            }
            case REGEXP_CONCATENATION: {
                return visitConcatenation(e.exp1, e.exp2);
            }
            case REGEXP_EMPTY: {
                return visitEmpty();
            }
            case REGEXP_INTERSECTION: {
                return visitIntersection(e.exp1, e.exp2);
            }
            case REGEXP_INTERVAL: {
                return visitInterval(e.min, e.max);
            }
            case REGEXP_OPTIONAL: {
                return visitOptional(e.exp1);
            }
            case REGEXP_REPEAT: {
                return visitRepeat(e.exp1);
            }
            case REGEXP_REPEAT_MIN: {
                return visitRepeatMin(e.exp1, e.min);
            }
            case REGEXP_REPEAT_MINMAX: {
                return visitRepeatMinMax(e.exp1, e.min, e.max);
            }
            case REGEXP_STRING: {
                return visitString(e.s);
            }
            case REGEXP_UNION: {
                return visitUnion(e.exp1, e.exp2);
            }
            default:
                throw new IllegalArgumentException(
                        "Unsupported kind ogf regular expression " + e.kind);
        }
    }

    public abstract K visitUnion(RegExp e, RegExp exp2);

    public abstract K visitString(String s);

    public abstract K visitRepeatMinMax(RegExp e, int min, int max);

    public abstract K visitRepeatMin(RegExp e, int min);

    public abstract K visitRepeat(RegExp e);

    public abstract K visitOptional(RegExp e);

    public abstract K visitInterval(int min, int max);

    public abstract K visitIntersection(RegExp left, RegExp right);

    public abstract K visitEmpty();

    public abstract K visitConcatenation(RegExp left, RegExp right);

    public abstract K visitComplement(RegExp e);

    public abstract K visitCharRange(char from, char to);

    public abstract K visitChar(char c);

    public abstract K visitAutomaton(RegExp e);

    public abstract K visitAnyString();

    public abstract K visitAnyChar();

}
