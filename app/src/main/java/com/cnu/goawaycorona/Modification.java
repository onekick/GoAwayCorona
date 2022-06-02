package com.cnu.goawaycorona;

import android.graphics.Point;

public class Modification {
    Point prev = new Point(0,0);
    Point next = new Point(0,0);

    public Point getNext() {
        return next;
    }

    public void setNext(Point next) {
        this.next = next;
    }

    public Point getPrev() {
        return prev;
    }

    public void setPrev(Point prev) {
        this.prev = prev;
    }
}
