package com.smart_park_parking_management_system.exception;

import com.smart_park_parking_management_system.exception.ReturnCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessException extends RuntimeException {
    private int code;
    private String msg;

    public BusinessException() {
    }

    public BusinessException(ReturnCode returnCode) {
        this(returnCode.getCode(),returnCode.getMsg());
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}

