package cn.edu.fzu.sm2023.yjr.quizweb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = {"cn.edu.fzu.sm2023.yjr.quizweb.mapper"})
public class ProjectManagement {
    public static void main(String[] args) {
        SpringApplication.run(ProjectManagement.class, args);
    }
}
