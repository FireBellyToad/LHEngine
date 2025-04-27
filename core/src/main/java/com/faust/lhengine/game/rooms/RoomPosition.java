package com.faust.lhengine.game.rooms;

/**
 * Class for Room position in mainWorld.
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public class RoomPosition {
    private final int x;
    private final int y;

    public RoomPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public RoomPosition(float x, float y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomPosition)) return false;
        RoomPosition that = (RoomPosition) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        final int prime = 11;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public String toString() {
        return "\"x\": " + x + ", \"y\" :" + y;
    }
}
