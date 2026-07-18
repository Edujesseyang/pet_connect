package com.pet_connect.backend_service.dto.respond;

public class InnerRespond<T> {
    private boolean state;
    private String message;
    private  T data;

    public InnerRespond() {
    }

    public InnerRespond(boolean state, String message) {
        this.state = state;
        this.message = message;
        this.data = null;
    }

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

    public T getData() {
        return data;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }
}
