package com.nsuslab.interfaces.api.common;

import com.nsuslab.supports.error.CoreException;

public record ApiResponse<T>(Metadata meta, T data) {

    public record Metadata(Result result, String errorCode, String message) {
        public enum Result {
            SUCCESS, FAIL
        }

        public static Metadata success() {
            return new Metadata(Result.SUCCESS, null, null);
        }

        public static Metadata fail(String errorCode, String errorMessage) {
            return new Metadata(Result.FAIL, errorCode, errorMessage);
        }
    }
    public static ApiResponse<Object> success() {
        return new ApiResponse<>(Metadata.success(), null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(Metadata.success(), data);
    }

    public static <T> ApiResponse<T> fail(CoreException e) {
        return new ApiResponse<>(Metadata.fail(e.getErrorType().getCode(), e.getMessage()), null);
    }

    public static <T> ApiResponse<T> fail(String errorCode, String message) {
        return new ApiResponse<>(Metadata.fail(errorCode, message), null);
    }

}
