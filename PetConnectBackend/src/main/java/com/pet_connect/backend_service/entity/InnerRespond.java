package com.pet_connect.backend_service.entity;

public class InnerRespond {
    private final boolean state;
    private final String message;

    public InnerRespond(boolean state, String message) {
        this.state = state;
        this.message = message;
    }

    public boolean getState() {
        return this.state;
    }

    public String getMessage() {
        return this.message;
    }
}
