package cn.edu.fzu.sm2023.yjr.quizweb.mapper;

import cn.edu.fzu.sm2023.yjr.quizweb.entity.Question;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface QuestionMapper {
    @Select("SELECT * FROM question")
    List<Question> list();

    @Select("SELECT * FROM question WHERE question_id = #{questionId}")
    Question selectByQuestionId(Integer questionId);

    @Select("SELECT * FROM question WHERE question_type = #{questionType}")
    Question selectByQuestionType(String questionType);

    @Select("SELECT * FROM question WHERE content = #{content}")
    Question selectByContent(String content);

    List<Question> queryPage(int offset, Integer pageSize, Map<String, Object> query);

    int queryCount(Map<String, Object> query);

    void updateById(Question entity);

    void insert(Question entity);

    void removeByIds(List<Integer> ids);
}
