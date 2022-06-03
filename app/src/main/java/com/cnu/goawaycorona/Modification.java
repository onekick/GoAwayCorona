package com.cnu.goawaycorona;

import android.graphics.Point;

public class Modification {
    Position prev = new Position(0,0);
    Position next = new Position(0,0);

    public Position getNext() {
        return next;
    }

    public void setNext(Position next) {
        this.next = next;
    }

    public Position getPrev() {
        return prev;
    }

    public void setPrev(Position prev) {
        this.prev = prev;
    }
}
