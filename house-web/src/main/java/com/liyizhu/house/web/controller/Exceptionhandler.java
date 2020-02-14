package com.liyizhu.house.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class Exceptionhandler {
    private static final Logger logger = LoggerFactory.getLogger(Exceptionhandler.class);

    @ExceptionHandler(value={Exception.class})
    public String error500(HttpServletRequest request,Exception e){
        logger.error(e.getMessage(),e);
        logger.error(request.getRequestURL() + " encounter 500");
        return "error/500";
    }
}
