package cn.edu.fzu.sm2023.yjr.quizweb.controller;

import cn.edu.fzu.sm2023.yjr.quizweb.service.PracticeService;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.PracticeQuestionVO;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.ResponseVO;
import cn.edu.fzu.sm2023.yjr.quizweb.utils.CurrentUserThreadLocal;
import cn.edu.fzu.sm2023.yjr.quizweb.dto.CurrentUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mark")
public class MarkController {

    @Autowired
    private PracticeService practiceService;

    /**
     * 获取收藏列表
     */
    @GetMapping("/list")
    public ResponseVO<List<PracticeQuestionVO>> getMarkedList() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error("用户未登录");
        }

        List<PracticeQuestionVO> list = practiceService.getMarkedQuestions(userId);
        return ResponseVO.ok(list);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/remove/{questionId}")
    public ResponseVO removeMark(@PathVariable Integer questionId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error("用户未登录");
        }

        practiceService.toggleMarkQuestion(userId, questionId);
        return ResponseVO.ok("已取消收藏");
    }

    /**
     * 获取收藏数量
     */
    @GetMapping("/count")
    public ResponseVO<Integer> getMarkCount() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error("用户未登录");
        }

        List<PracticeQuestionVO> list = practiceService.getMarkedQuestions(userId);
        return ResponseVO.ok(list.size());
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