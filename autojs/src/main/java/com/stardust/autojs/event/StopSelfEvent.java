package com.stardust.autojs.event;

public class StopSelfEvent {
    private int type ;
    public StopSelfEvent(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
