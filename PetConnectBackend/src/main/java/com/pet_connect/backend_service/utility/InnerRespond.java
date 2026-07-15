package com.pet_connect.backend_service.utility;

public class InnerRespond<T> {
    private final boolean state;
    private final String message;
    private final T data;

    public InnerRespond(boolean state, String message, T data) {
        this.state = state;
        this.message = message;
        this.data = data;
    }

    public boolean getState() {
        return this.state;
    }

    public String getMessage() {
        return this.message;
    }

    public T getData(){
        return data;
    }
}
