package cn.edu.fzu.sm2023.yjr.quizweb.exception;

import cn.edu.fzu.sm2023.yjr.quizweb.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 全局异常拦截
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(value = CustomException.class)
    @ResponseBody
    public ResponseEntity CustomExceptionHandler(CustomException e) {
        return new ResponseEntity<>(ResponseVO.fail(e.getHttpStatus().value(), e.getMessage()), e.getHttpStatus());
    }
}
