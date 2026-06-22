package cn.edu.fzu.sm2023.yjr.quizweb.entity;

import lombok.Data;

/**
 * 题目信息表
 */
@Data
public class Question {
    private Integer questionId;
    private String questionType;
    private String content;
    private String options;
    private String answer;
    private String explanation;
}
