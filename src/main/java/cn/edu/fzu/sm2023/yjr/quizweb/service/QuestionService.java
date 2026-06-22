package cn.edu.fzu.sm2023.yjr.quizweb.service;

import cn.edu.fzu.sm2023.yjr.quizweb.entity.Question;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.PageVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface QuestionService {
    List<Question> list();

    PageVO<Question> page(Map<String, Object> query, Integer pageNum, Integer pageSize);

    void updateById(Question entity);

    void insert(Question entity);

    void removeByIds(List<Integer> ids);

    List<Question> importExcel(MultipartFile file) throws IOException;

    String batchInsert(List<Question> questions);

}
