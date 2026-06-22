// RecordMapper.java
package cn.edu.fzu.sm2023.yjr.quizweb.mapper;

import cn.edu.fzu.sm2023.yjr.quizweb.entity.Record;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface RecordMapper {
    @Select("SELECT * FROM record WHERE id = #{userId} AND question_id = #{questionId}")
    Record selectByUserAndQuestion(@Param("userId") Integer userId,
                                   @Param("questionId") Integer questionId);

    @Insert("INSERT INTO record (id, question_id, user_answer, is_correct, is_marked) " +
            "VALUES (#{id}, #{questionId}, #{userAnswer}, #{isCorrect}, #{isMarked})")
    int insert(Record record);

    @Update("UPDATE record SET user_answer = #{userAnswer}, is_correct = #{isCorrect}, " +
            "is_marked = #{isMarked} WHERE record_id = #{recordId}")
    int update(Record record);

    @Select("SELECT is_marked FROM record WHERE id = #{userId} AND question_id = #{questionId}")
    Integer selectMarkStatus(@Param("userId") Integer userId,
                             @Param("questionId") Integer questionId);

    @Select("SELECT r.*, q.question_type, q.content, q.options, q.answer, q.explanation " +
            "FROM record r " +
            "LEFT JOIN question q ON r.question_id = q.question_id " +
            "WHERE r.id = #{userId} AND r.is_marked = 1 " +
            "ORDER BY r.record_id DESC")
    List<Record> selectMarkedQuestions(Integer userId);

    @Select("SELECT COUNT(*) FROM record WHERE id = #{userId} AND is_marked = 1")
    int countMarkedQuestions(Integer userId);

    @Select("SELECT r.*, q.question_type, q.content, q.options, q.answer, q.explanation " +
            "FROM record r " +
            "LEFT JOIN question q ON r.question_id = q.question_id " +
            "WHERE r.id = #{userId} AND r.is_correct = 0 " +
            "ORDER BY r.record_id DESC")
    List<Record> selectWrongQuestions(Integer userId);

    @Select("SELECT COUNT(*) FROM record WHERE id = #{userId} AND is_correct = 0")
    int countWrongQuestions(Integer userId);

    @Select("SELECT COUNT(*) FROM record WHERE id = #{userId} AND is_correct = 1")
    int countCorrectQuestions(Integer userId);

    @Select("SELECT COUNT(*) FROM record WHERE id = #{userId}")
    int countTotalAnswered(Integer userId);

    @Select("SELECT COUNT(DISTINCT question_id) FROM record WHERE id = #{userId} AND is_correct = 0")
    int countDistinctWrongQuestions(Integer userId);

}