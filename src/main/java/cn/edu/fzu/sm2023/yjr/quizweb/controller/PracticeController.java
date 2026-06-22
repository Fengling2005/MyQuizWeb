// PracticeController.java
package cn.edu.fzu.sm2023.yjr.quizweb.controller;

import cn.edu.fzu.sm2023.yjr.quizweb.dto.CurrentUserDTO;
import cn.edu.fzu.sm2023.yjr.quizweb.entity.Question;
import cn.edu.fzu.sm2023.yjr.quizweb.entity.Record;
import cn.edu.fzu.sm2023.yjr.quizweb.exception.CustomException;
import cn.edu.fzu.sm2023.yjr.quizweb.mapper.QuestionMapper;
import cn.edu.fzu.sm2023.yjr.quizweb.service.PracticeService;
import cn.edu.fzu.sm2023.yjr.quizweb.utils.CurrentUserThreadLocal;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.PracticeQuestionVO;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.PracticeResultVO;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/practice")
public class PracticeController {

    @Autowired
    private PracticeService practiceService;

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 获取下一题
     */
    @GetMapping("/next")
    public ResponseVO<Question> getNextQuestion(@RequestParam String mode) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error("用户未登录");
        }

        Question question;
        try {
            if ("wrong".equals(mode)) {
                // 错题专项练习模式
                question = practiceService.getNextWrongQuestion(userId);
            } else {
                // 常规练习模式
                question = practiceService.getNextQuestion(userId, mode);
            }

            if (question == null) {
                return ResponseVO.error("没有找到符合条件的题目");
            }

            return ResponseVO.ok(question);

        } catch (CustomException e) {
            return ResponseVO.error(e.getMessage());
        } catch (Exception e) {
            return ResponseVO.error("获取题目失败");
        }
    }

    // 错题统计接口
    @GetMapping("/wrongStatistics")
    public ResponseVO<Map<String, Object>> getWrongStatistics() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error("用户未登录");
        }

        Map<String, Object> stats = practiceService.getWrongStatistics(userId);
        return ResponseVO.ok(stats);
    }

    /**
     * 提交答案
     */
    @PostMapping("/submit")
    public ResponseVO<PracticeResultVO> submitAnswer(@RequestBody Record record) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error("用户未登录");
        }

        // 设置当前用户ID
        record.setId(userId);
        PracticeResultVO result = practiceService.submitAnswer(record);
        return ResponseVO.ok(result);
    }

    /**
     * 收藏/取消收藏题目
     */
    @PostMapping("/mark")
    public ResponseVO toggleMark(@RequestParam Integer questionId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error("用户未登录");
        }

        practiceService.toggleMarkQuestion(userId, questionId);
        return ResponseVO.ok();
    }

    /**
     * 获取收藏列表
     */
    @GetMapping("/marked")
    public ResponseVO<List<PracticeQuestionVO>> getMarkedQuestions() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error("用户未登录");
        }

        List<PracticeQuestionVO> list = practiceService.getMarkedQuestions(userId);
        return ResponseVO.ok(list);
    }

    /**
     * 检查收藏状态
     */
    @GetMapping("/checkMark/{questionId}")
    public ResponseVO<Boolean> checkMarkStatus(@PathVariable Integer questionId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error("用户未登录");
        }

        boolean isMarked = practiceService.checkMarkStatus(userId, questionId);
        return ResponseVO.ok(isMarked);
    }

    /**
     * 获取收藏题目数量
     */
    @GetMapping("/markCount")
    public ResponseVO<Integer> getMarkCount() {
        Integer userId = getCurrentUserId();
        // 这里需要添加count方法到RecordMapper
        // int count = recordMapper.countMarkedQuestions(userId);
        // return ResponseVO.ok(count);
        return ResponseVO.ok(0); // 暂时返回0
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ResponseVO<Map<String, Object>> getStatistics() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error("用户未登录");
        }

        Map<String, Object> stats = practiceService.getStatistics(userId);
        return ResponseVO.ok(stats);
    }

    // 查看答案接口
    @GetMapping("/answer/{questionId}")
    public ResponseVO<Map<String, String>> getAnswer(@PathVariable Integer questionId) {
        Question question = questionMapper.selectByQuestionId(questionId);
        if (question == null) {
            return ResponseVO.error("题目不存在");
        }

        Map<String, String> result = new HashMap<>();
        result.put("answer", question.getAnswer());
        result.put("explanation", question.getExplanation());

        return ResponseVO.ok(result);
    }

    /**
     * 获取当前登录用户ID
     */
    private Integer getCurrentUserId() {
        CurrentUserDTO currentUser = CurrentUserThreadLocal.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        return currentUser.getId();
    }
}