package com.faust.lhengine.game.rooms;

/**
 * Model class for storing Rooom contents
 *
 * @author Jacopo "Faust" Buttiglieri
 */
public interface OnRoomChangeListener {

    void onRoomChangeStart(AbstractRoom previousRoom);
    void onRoomChangeEnd(AbstractRoom newRoom);
}
