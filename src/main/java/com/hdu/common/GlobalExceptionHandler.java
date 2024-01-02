package com.hdu.common;


import com.hdu.exception.BusinessException;
import com.hdu.exception.ParamException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.json.Json;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = RuntimeException.class)
    @ResponseBody
    public JsonData exceptionHandler(RuntimeException runtimeException){
        log.error("unknown exception",runtimeException);
        if (runtimeException instanceof ParamException || runtimeException instanceof BusinessException){
            return JsonData.fail(runtimeException.getMessage());
        }
        return JsonData.fail("系统异常，稍后再试");
    }

    @ExceptionHandler(value = Error.class)
    @ResponseBody
    public JsonData errorHandler(Error error){
        log.error("unknown error",error);
        return JsonData.fail("系统异常，请联系管理员");
    }
}
