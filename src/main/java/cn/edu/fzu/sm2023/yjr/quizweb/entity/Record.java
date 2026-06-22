// Record.java
package cn.edu.fzu.sm2023.yjr.quizweb.entity;

import lombok.Data;
import java.util.Date;

/**
 * 答题记录表
 */
@Data
public class Record {
    private Integer recordId;
    private Integer id;  // 用户ID
    private Integer questionId;
    private String userAnswer;
    private Integer isCorrect;
    private Integer isMarked;
    private Date createTime;

    private String questionType;
    private String content;
    private String options;
    private String answer;
    private String explanation;
}