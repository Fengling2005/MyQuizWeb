package cn.edu.fzu.sm2023.yjr.quizweb.vo;

import lombok.Data;

@Data
public class PracticeResultVO {
    private boolean correct;
    private String correctAnswer;
    private String explanation;
    private String questionType;
    private String userAnswer;
    private boolean marked;
}