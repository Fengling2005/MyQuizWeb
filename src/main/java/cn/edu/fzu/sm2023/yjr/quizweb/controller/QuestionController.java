package cn.edu.fzu.sm2023.yjr.quizweb.controller;

import cn.edu.fzu.sm2023.yjr.quizweb.entity.Question;
import cn.edu.fzu.sm2023.yjr.quizweb.service.QuestionService;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.PageVO;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.ResponseVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/question")
public class QuestionController {
    @Autowired
    private QuestionService questionService;

    @GetMapping("list")
    public ResponseVO<List<Question>> list() {
        List<Question> list = questionService.list();
        return ResponseVO.ok(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("page")
    public ResponseVO<PageVO<Question>> page(@RequestParam Map<String, Object> query,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageVO<Question> page = questionService.page(query, pageNum, pageSize);
        return ResponseVO.ok(page);
    }

    /**
     * 更新
     */
    @PutMapping("update")
    public ResponseVO update(@RequestBody Question entity) {
        questionService.updateById(entity);
        return ResponseVO.ok();
    }

    /**
     * 新增
     */
    @PostMapping("add")
    public ResponseVO add(@RequestBody Question entity) {
        questionService.insert(entity);
        return ResponseVO.ok();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("delBatch")
    public ResponseVO delBatch(@RequestBody List<Integer> ids) {
        questionService.removeByIds(ids);
        return ResponseVO.ok();
    }

    /**
     * 导入Excel数据
     */
    @PostMapping("importExcel")
    public ResponseVO<List<Question>> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            List<Question> questions = questionService.importExcel(file);
            return ResponseVO.ok(questions, "文件解析成功，共" + questions.size() + "条数据");
        } catch (Exception e) {
            return ResponseVO.error(e.getMessage());
        }
    }

    /**
     * 确认导入（保存到数据库）
     */
    @PostMapping("confirmImport")
    public ResponseVO confirmImport(@RequestBody List<Question> questions) {
        try {
            String result = questionService.batchInsert(questions);
            return ResponseVO.ok(result);
        } catch (Exception e) {
            return ResponseVO.error(e.getMessage());
        }
    }
}