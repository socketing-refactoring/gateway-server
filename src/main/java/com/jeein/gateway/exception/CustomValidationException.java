package com.jeein.gateway.exception;

import lombok.Getter;
import org.springframework.validation.BindingResult;

@Getter
public class CustomValidationException extends RuntimeException {
    private final ErrorCode errorCode;
    private final BindingResult bindingResult;

    public CustomValidationException(ErrorCode errorCode, BindingResult bindingResults) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.bindingResult = bindingResults;
    }
}
