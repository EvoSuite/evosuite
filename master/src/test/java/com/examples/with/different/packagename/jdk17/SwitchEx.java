package com.examples.with.different.packagename.jdk17;

public class SwitchEx {

    public enum Day { SUNDAY, MONDAY, TUESDAY,
        WEDNESDAY, THURSDAY, FRIDAY, SATURDAY; }

    public int getSwitchExpr(){
        Day day = Day.WEDNESDAY;
        int numLetters = switch (day) {
            case MONDAY:
            case FRIDAY:
            case SUNDAY:
                yield 6;
            case TUESDAY:
                System.out.println("## test ##");
                yield 7;
            case THURSDAY:
            case SATURDAY:
                yield 8;
            case WEDNESDAY:
                yield 9;
            default:
                throw new IllegalStateException("Invalid day: " + day);
        };
        System.out.println(numLetters);

        return numLetters;
    }


}
