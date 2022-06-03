package com.cnu.goawaycorona;

public class Position {
    double x=0;
    double y=0;

    public Position() {
    }

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void set(double ans_x, double ans_y) {
        x=ans_x;
        y=ans_y;
    }
}
