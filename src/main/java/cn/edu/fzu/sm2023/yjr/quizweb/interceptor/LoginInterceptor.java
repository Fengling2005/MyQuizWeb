package cn.edu.fzu.sm2023.yjr.quizweb.interceptor;

import com.alibaba.fastjson2.JSON;
import cn.edu.fzu.sm2023.yjr.quizweb.dto.CurrentUserDTO;
import cn.edu.fzu.sm2023.yjr.quizweb.exception.CustomException;
import cn.edu.fzu.sm2023.yjr.quizweb.utils.CurrentUserThreadLocal;
import cn.edu.fzu.sm2023.yjr.quizweb.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute("requestStartTime", startTime);
        // OPTIONS请求不做校验,
        // 前后端分离的架构, 前端会发一个OPTIONS请求先做预检, 对预检请求不做校验
        if (request.getMethod().toUpperCase().equals("OPTIONS")) {
            return true;
        }
        String path = request.getRequestURL().toString();
        log.info("接口登录拦截：，path：{}", path);
        //获取header的token参数
        String token = request.getHeader("token");
        log.info("登录校验开始，token：{}", token);
        if (token == null || token.isEmpty()) {
            log.info("token为空，请求被拦截");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        Claims claims = JwtUtils.verifyJwt(token);
        //获取用户ID
        if (claims == null) {
            log.warn("token无效，请求被拦截");
            throw new CustomException(HttpStatus.UNAUTHORIZED,"token无效，请求被拦截");
        } else {
            CurrentUserDTO currentUserDTO = JSON.parseObject(claims.get("currentUser").toString(), CurrentUserDTO.class);
            CurrentUserThreadLocal.set(currentUserDTO);
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        long startTime = (Long) request.getAttribute("requestStartTime");
        log.info("------------- LoginInterceptor 结束 耗时：{} ms -------------", System.currentTimeMillis() - startTime);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        CurrentUserThreadLocal.clear();
        log.info("LogInterceptor 结束");
    }
}
