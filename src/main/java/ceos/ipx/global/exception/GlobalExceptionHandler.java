package ceos.ipx.global.exception;

import ceos.ipx.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handlerBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        if (errorCode.getStatus().is5xxServerError()) {
            log.error("BusinessException: [{}] {}", errorCode.getCode(), e.getMessage(), e);
        } else {
            log.warn("BusinessException: [{}] {}", errorCode.getCode(), e.getMessage());
        }

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handlerValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation Exception: {}", e.getMessage());

        String errorMessage = e.getBindingResult().getAllErrors().getFirst().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode, errorMessage));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handlerMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.warn("MethodArgumentTypeMismatchException: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        String errorMessage = "'%s' 파라미터의 형식이 올바르지 않습니다.".formatted(e.getName());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode, errorMessage));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiResponse<?>> handlerBadRequestException(Exception e) {
        log.warn("Bad Request Exception: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<?>> handlerHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        log.warn("HttpRequestMethodNotSupportedException: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<?>> handlerNoResourceFoundException(NoResourceFoundException e) {
        log.warn("NoResourceFoundException: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.RESOURCE_NOT_FOUND;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handlerException(Exception e) {
        log.error("Unexpected Exception:", e);

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }
}
