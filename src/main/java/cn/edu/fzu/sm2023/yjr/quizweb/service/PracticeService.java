// PracticeService.java
package cn.edu.fzu.sm2023.yjr.quizweb.service;

import cn.edu.fzu.sm2023.yjr.quizweb.entity.Question;
import cn.edu.fzu.sm2023.yjr.quizweb.entity.Record;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.PracticeQuestionVO;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.PracticeResultVO;

import java.util.List;
import java.util.Map;

public interface PracticeService {
    /**
     * 获取下一题（根据模式）
     */
    Question getNextQuestion(Integer userId, String practiceMode);

    /**
     * 提交答案
     */
    PracticeResultVO submitAnswer(Record record);

    /**
     * 收藏题目
     */
    void toggleMarkQuestion(Integer userId, Integer questionId);

    /**
     * 检查收藏状态
     */
    boolean checkMarkStatus(Integer userId, Integer questionId);

    /**
     * 获取收藏的题目
     */
    List<PracticeQuestionVO> getMarkedQuestions(Integer userId);

    /**
     * 获取答题统计
     */
    Map<String, Object> getStatistics(Integer userId);

    /**
     * 获取题目答案
     */
    Map<String, String> getQuestionAnswer(Integer questionId);

    /**
     * 获取错题列表
     */
    List<PracticeQuestionVO> getWrongQuestions(Integer userId);

    /**
     * 清除错题记录
     */
    void clearWrongQuestion(Integer userId, Integer questionId);

    /**
     * 获取下一题（错题模式）
     */
    Question getNextWrongQuestion(Integer userId);

    /**
     * 获取错题统计
     */
    Map<String, Object> getWrongStatistics(Integer userId);
}