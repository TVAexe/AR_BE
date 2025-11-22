package com.example.AR_BE.domain.response;
import lombok.Getter;
import lombok.Setter;

public class RestResponse<T> {
    @Getter
    @Setter
    private int statusCode;

    @Getter
    @Setter
    private String error;

    @Getter
    @Setter
    private Object message;

    @Getter
    @Setter
    private T data;
}
