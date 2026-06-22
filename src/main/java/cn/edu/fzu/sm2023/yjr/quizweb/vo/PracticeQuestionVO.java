// PracticeQuestionVO.java
package cn.edu.fzu.sm2023.yjr.quizweb.vo;

import lombok.Data;

@Data
public class PracticeQuestionVO {
    private Integer recordId;
    private Integer questionId;
    private String questionType;
    private String content;
    private String options;
    private String answer;
    private String userAnswer;
    private Integer isCorrect;
    private Integer isMarked;
    private String explanation;
}