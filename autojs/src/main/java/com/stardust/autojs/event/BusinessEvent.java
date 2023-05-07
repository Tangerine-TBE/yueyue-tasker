package com.stardust.autojs.event;

public class BusinessEvent {
    private String jsonValue;
    public BusinessEvent(String jsonValue){
        this.jsonValue = jsonValue;
    }
    public String getJsonValue(){
        return jsonValue;
    }
}
