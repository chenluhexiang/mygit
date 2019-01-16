package com.hexu.miniapi.util;

public class RestResponse<T> {

    private Integer status;

    private long timestamp;

    private String error;

    private T data;


    public RestResponse(Integer status, String error) {
        this.status = status;
        this.error = error;
        this.timestamp = System.currentTimeMillis();
    }

    public RestResponse(Integer status, String error, T data) {
        this.status = status;
        this.error = error;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public Integer getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getError() {
        return error;
    }

    public Object getData() {
        return data;
    }


}
