package com.oa.common;

import lombok.Data;

@Data
public class R<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> R<T> ok(T data) {
        R<T> result = new R<>();
        result.code = 0;
        result.msg = "成功";
        result.data = data;
        return result;
    }

    public static R<Void> ok() {
        return ok(null);
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> result = new R<>();
        result.code = code;
        result.msg = message;
        return result;
    }
}
