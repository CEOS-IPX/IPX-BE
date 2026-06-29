package ceos.ipx.global.response;

import ceos.ipx.global.exception.ErrorCode;

public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorResponse error
) {
    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<?> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, ErrorResponse.of(errorCode));
    }

    public static ApiResponse<?> fail(ErrorCode errorCode, String customMessage) {
        return new ApiResponse<>(false, null, ErrorResponse.of(errorCode, customMessage));
    }
}
