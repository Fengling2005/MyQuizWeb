// PracticeServiceImpl.java
package cn.edu.fzu.sm2023.yjr.quizweb.service.impl;

import cn.edu.fzu.sm2023.yjr.quizweb.entity.Question;
import cn.edu.fzu.sm2023.yjr.quizweb.entity.Record;
import cn.edu.fzu.sm2023.yjr.quizweb.exception.CustomException;
import cn.edu.fzu.sm2023.yjr.quizweb.mapper.QuestionMapper;
import cn.edu.fzu.sm2023.yjr.quizweb.mapper.RecordMapper;
import cn.edu.fzu.sm2023.yjr.quizweb.service.PracticeService;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.PracticeQuestionVO;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.PracticeResultVO;
import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PracticeServiceImpl implements PracticeService {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private RecordMapper recordMapper;

    // 存储用户已做过的题目，避免重复
    private final Map<Integer, Set<Integer>> userAnsweredQuestions = new HashMap<>();

    @Override
    public Question getNextQuestion(Integer userId, String practiceMode) {
        // 获取用户已做过的题目
        Set<Integer> answeredSet = userAnsweredQuestions.computeIfAbsent(userId, k -> new HashSet<>());

        // 获取所有题目
        List<Question> allQuestions = questionMapper.list();

        // 根据模式过滤题目
        List<Question> filteredQuestions = allQuestions.stream()
                .filter(q -> {
                    if (answeredSet.contains(q.getQuestionId())) {
                        return false; // 跳过已做过的题目
                    }
                    switch (practiceMode) {
                        case "single":
                            return "单选题".equals(q.getQuestionType());
                        case "multiple":
                            return "多选题".equals(q.getQuestionType());
                        case "judgment":
                            return "判断题".equals(q.getQuestionType());
                        case "mixed":
                            return true; // 混合训练包含所有类型
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());

        if (filteredQuestions.isEmpty()) {
            // 如果所有题目都做完了，清空记录重新开始
            answeredSet.clear();
            filteredQuestions = allQuestions.stream()
                    .filter(q -> {
                        switch (practiceMode) {
                            case "single":
                                return "单选题".equals(q.getQuestionType());
                            case "multiple":
                                return "多选题".equals(q.getQuestionType());
                            case "judgment":
                                return "判断题".equals(q.getQuestionType());
                            case "mixed":
                                return true;
                            default:
                                return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        if (filteredQuestions.isEmpty()) {
            throw new CustomException("没有找到符合条件的题目");
        }

        // 随机选择一道题
        Random random = new Random();
        Question selected = filteredQuestions.get(random.nextInt(filteredQuestions.size()));

        // 解析选项JSON为Map
        if (selected.getOptions() != null && selected.getOptions().startsWith("{")) {
            Map<String, String> optionsMap = JSONUtil.toBean(selected.getOptions(), Map.class);
            // 这里可以根据需要格式化选项显示
        }

        return selected;
    }

    @Override
    @Transactional
    public PracticeResultVO submitAnswer(Record record) {
        if (record.getId() == null || record.getQuestionId() == null || record.getUserAnswer() == null) {
            throw new CustomException("参数不完整");
        }

        // 获取题目信息
        Question question = questionMapper.selectByQuestionId(record.getQuestionId());
        if (question == null) {
            throw new CustomException("题目不存在");
        }

        // 检查答案是否正确
        boolean isCorrect = checkAnswer(question, record.getUserAnswer());
        record.setIsCorrect(isCorrect ? 1 : 0);

        // 查找是否已有记录
        Record existingRecord = recordMapper.selectByUserAndQuestion(record.getId(), record.getQuestionId());

        if (existingRecord == null) {
            // 新记录
            record.setIsMarked(0); // 默认未收藏
            recordMapper.insert(record);
        } else {
            // 更新记录，但保留收藏状态
            record.setRecordId(existingRecord.getRecordId());
            record.setIsMarked(existingRecord.getIsMarked());
            recordMapper.update(record);
        }

        // 添加到已答题集合
        userAnsweredQuestions.computeIfAbsent(record.getId(), k -> new HashSet<>())
                .add(record.getQuestionId());

        // 创建返回结果
        PracticeResultVO result = new PracticeResultVO();
        result.setCorrect(isCorrect);
        result.setCorrectAnswer(question.getAnswer());
        result.setExplanation(question.getExplanation());
        result.setQuestionType(question.getQuestionType());
        result.setUserAnswer(record.getUserAnswer());

        // 添加收藏状态
        boolean marked = checkMarkStatus(record.getId(), record.getQuestionId());
        result.setMarked(marked);

        return result;
    }

    @Override
    public void toggleMarkQuestion(Integer userId, Integer questionId) {
        Record record = recordMapper.selectByUserAndQuestion(userId, questionId);
        if (record == null) {
            // 如果还没有记录，创建一条
            record = new Record();
            record.setId(userId);
            record.setQuestionId(questionId);
            record.setUserAnswer("");
            record.setIsCorrect(0);
            record.setIsMarked(1); // 设置为收藏
            recordMapper.insert(record);
        } else {
            // 切换收藏状态
            Integer currentMarkStatus = record.getIsMarked();
            Integer newMarkStatus = (currentMarkStatus == null || currentMarkStatus == 0) ? 1 : 0;
            record.setIsMarked(newMarkStatus);
            recordMapper.update(record);
        }
    }

    @Override
    public List<PracticeQuestionVO> getMarkedQuestions(Integer userId) {
        List<Record> records = recordMapper.selectMarkedQuestions(userId);

        return records.stream().map(record -> {
            PracticeQuestionVO vo = new PracticeQuestionVO();
            vo.setRecordId(record.getRecordId());
            vo.setQuestionId(record.getQuestionId());
            vo.setQuestionType(record.getQuestionType());
            vo.setContent(record.getContent());
            vo.setOptions(record.getOptions());
            vo.setAnswer(record.getAnswer());
            vo.setUserAnswer(record.getUserAnswer());
            vo.setIsCorrect(record.getIsCorrect());
            vo.setIsMarked(record.getIsMarked());
            vo.setExplanation(record.getExplanation());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getStatistics(Integer userId) {
        Map<String, Object> stats = getWrongStatistics(userId);

        // 添加统计信息
        int markedCount = recordMapper.countMarkedQuestions(userId);
        stats.put("markedCount", markedCount);

        return stats;
    }

    private boolean checkAnswer(Question question, String userAnswer) {
        String correctAnswer = question.getAnswer();
        String questionType = question.getQuestionType();

        if (userAnswer == null || correctAnswer == null) {
            return false;
        }

        userAnswer = userAnswer.trim();
        correctAnswer = correctAnswer.trim();

        if ("单选题".equals(questionType) || "判断题".equals(questionType)) {
            return correctAnswer.equalsIgnoreCase(userAnswer);
        } else if ("多选题".equals(questionType)) {
            // 处理多选题，答案可能是"A,C"或"C,A"，顺序不重要
            String[] correctArr = correctAnswer.split(",");
            String[] userArr = userAnswer.split(",");

            // 去除空格并排序
            for (int i = 0; i < correctArr.length; i++) {
                correctArr[i] = correctArr[i].trim().toUpperCase();
            }
            for (int i = 0; i < userArr.length; i++) {
                userArr[i] = userArr[i].trim().toUpperCase();
            }

            // 如果数量不同，直接返回false
            if (correctArr.length != userArr.length) {
                return false;
            }

            Arrays.sort(correctArr);
            Arrays.sort(userArr);

            // 逐个比较
            for (int i = 0; i < correctArr.length; i++) {
                if (!correctArr[i].equals(userArr[i])) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean checkMarkStatus(Integer userId, Integer questionId) {
        Integer markStatus = recordMapper.selectMarkStatus(userId, questionId);
        return markStatus != null && markStatus == 1;
    }

    @Override
    public Map<String, String> getQuestionAnswer(Integer questionId) {
        Question question = questionMapper.selectByQuestionId(questionId);
        if (question == null) {
            throw new CustomException("题目不存在");
        }

        Map<String, String> result = new HashMap<>();
        result.put("answer", question.getAnswer());
        result.put("explanation", question.getExplanation());

        return result;
    }

    @Override
    public List<PracticeQuestionVO> getWrongQuestions(Integer userId) {
        List<Record> records = recordMapper.selectWrongQuestions(userId);

        return records.stream().map(record -> {
            PracticeQuestionVO vo = new PracticeQuestionVO();
            vo.setRecordId(record.getRecordId());
            vo.setQuestionId(record.getQuestionId());
            vo.setQuestionType(record.getQuestionType());
            vo.setContent(record.getContent());
            vo.setOptions(record.getOptions());
            vo.setAnswer(record.getAnswer());
            vo.setUserAnswer(record.getUserAnswer());
            vo.setIsCorrect(record.getIsCorrect());
            vo.setIsMarked(record.getIsMarked());
            vo.setExplanation(record.getExplanation());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void clearWrongQuestion(Integer userId, Integer questionId) {
        // 将错题标记为正确，或者直接删除记录
        Record record = recordMapper.selectByUserAndQuestion(userId, questionId);
        if (record != null) {
            record.setIsCorrect(1); // 标记为正确
            recordMapper.update(record);
        }
    }

    @Override
    public Map<String, Object> getWrongStatistics(Integer userId) {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 获取错题总数
            List<Record> wrongRecords = recordMapper.selectWrongQuestions(userId);
            int wrongCount = wrongRecords.size();

            // 获取答题总数
            int totalAnswered = recordMapper.countTotalAnswered(userId);

            // 获取正确答题数
            int correctCount = recordMapper.countCorrectQuestions(userId);

            stats.put("wrongCount", wrongCount);
            stats.put("totalAnswered", totalAnswered);
            stats.put("correctCount", correctCount);

            // 计算正确率
            double accuracy = totalAnswered > 0 ? (double) correctCount / totalAnswered * 100 : 0;
            stats.put("accuracy", Math.round(accuracy * 100) / 100.0);

        } catch (Exception e) {
            // 如果查询出错，返回默认值
            stats.put("wrongCount", 0);
            stats.put("totalAnswered", 0);
            stats.put("correctCount", 0);
            stats.put("accuracy", 0);
        }

        return stats;
    }

    @Override
    public Question getNextWrongQuestion(Integer userId) {
        // 获取用户的错题列表
        List<Record> wrongRecords = recordMapper.selectWrongQuestions(userId);

        if (wrongRecords.isEmpty()) {
            throw new CustomException("暂无错题可练习");
        }

        // 获取用户已经练习过的错题（避免重复显示）
        Set<Integer> practicedWrongQuestions = userAnsweredQuestions.getOrDefault(userId, new HashSet<>());

        // 过滤掉已经练习过的错题
        List<Record> availableWrongRecords = wrongRecords.stream()
                .filter(record -> !practicedWrongQuestions.contains(record.getQuestionId()))
                .collect(Collectors.toList());

        // 如果所有错题都练习过了，清空记录重新开始
        if (availableWrongRecords.isEmpty()) {
            practicedWrongQuestions.clear();
            availableWrongRecords = wrongRecords;
        }

        // 随机选择一道错题
        Random random = new Random();
        Record selectedRecord = availableWrongRecords.get(random.nextInt(availableWrongRecords.size()));

        // 获取题目详情
        Question question = questionMapper.selectByQuestionId(selectedRecord.getQuestionId());

        if (question == null) {
            throw new CustomException("题目不存在");
        }

        // 标记这道题已经练习过
        practicedWrongQuestions.add(selectedRecord.getQuestionId());
        userAnsweredQuestions.put(userId, practicedWrongQuestions);

        return question;
    }

}