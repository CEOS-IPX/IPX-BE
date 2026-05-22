package ceos.ipx.global.response;

import ceos.ipx.global.exception.ErrorCode;

public record ErrorResponse(
        int status,
        String code,
        String message
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String customMessage) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                customMessage
        );
    }
}
