package cn.edu.fzu.sm2023.yjr.quizweb.config;

import cn.edu.fzu.sm2023.yjr.quizweb.interceptor.LoginInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class SpringMvcConfig implements WebMvcConfigurer {

    @Resource
    LoginInterceptor loginInterceptor;

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                //允许直接访问的接口
                .excludePathPatterns(
                        "/common/login",
                        "/common/register",
                        "/common/retrievePassword",
                        "/file/**"
                );
    }
}
