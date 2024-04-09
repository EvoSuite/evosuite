package com.examples.with.different.packagename.jdk17;

public sealed class SealedEx permits Circle, Square, Rectangle {
}


// permits 로 선언된 class 들 (Circle, Square, Rectangle) 만이 Shape class 상속
final class Circle extends SealedEx {
    public double radius() {
        return 1.0;
    }
}

// non-sealed 로 선언된 Square 는 어떤 class 든지 상속
non-sealed class Square extends SealedEx {
}

// Shape 를 상속 받을 수 있는 클래스는 FilledRectangle
sealed class Rectangle extends SealedEx permits FilledRectangle {
    public int length() {
        return 3;
    }

    public int width() {
        return 4;
    }
}

final class FilledRectangle extends Rectangle {
}