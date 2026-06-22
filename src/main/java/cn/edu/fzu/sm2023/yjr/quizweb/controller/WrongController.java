// WrongController.java
package cn.edu.fzu.sm2023.yjr.quizweb.controller;

import cn.edu.fzu.sm2023.yjr.quizweb.service.PracticeService;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.PracticeQuestionVO;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.ResponseVO;
import cn.edu.fzu.sm2023.yjr.quizweb.utils.CurrentUserThreadLocal;
import cn.edu.fzu.sm2023.yjr.quizweb.dto.CurrentUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wrong")
public class WrongController {

    @Autowired
    private PracticeService practiceService;

    /**
     * 获取错题列表
     */
    @GetMapping("/list")
    public ResponseVO<List<PracticeQuestionVO>> getWrongList() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error("用户未登录");
        }

        List<PracticeQuestionVO> list = practiceService.getWrongQuestions(userId);
        return ResponseVO.ok(list);
    }

    /**
     * 清除错题（标记为已掌握）
     */
    @DeleteMapping("/clear/{questionId}")
    public ResponseVO clearWrong(@PathVariable Integer questionId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error("用户未登录");
        }

        practiceService.clearWrongQuestion(userId, questionId);
        return ResponseVO.ok("错题已标记为已掌握");
    }

    /**
     * 批量清除错题
     */
    @DeleteMapping("/clearBatch")
    public ResponseVO clearWrongBatch(@RequestBody List<Integer> questionIds) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error("用户未登录");
        }

        for (Integer questionId : questionIds) {
            practiceService.clearWrongQuestion(userId, questionId);
        }
        return ResponseVO.ok("已批量清除" + questionIds.size() + "道错题");
    }

    /**
     * 获取错题统计
     */
    @GetMapping("/statistics")
    public ResponseVO<Map<String, Object>> getWrongStatistics() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error("用户未登录");
        }

        Map<String, Object> stats = practiceService.getWrongStatistics(userId);
        return ResponseVO.ok(stats);
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