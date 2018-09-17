package com.examples.with.different.packagename.classhandling;

public enum MutableEnum {
    Monday("a"), Tuesday("b");

    private String letter;

    private MutableEnum(String letter){
        this.letter = letter;
    }

    public void changeLetter(){
        this.letter = "X";
    }

    public String getLetter() {
        return letter;
    }
}
