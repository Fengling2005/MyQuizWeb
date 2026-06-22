package cn.edu.fzu.sm2023.yjr.quizweb.service.impl;

import cn.edu.fzu.sm2023.yjr.quizweb.entity.Question;
import cn.edu.fzu.sm2023.yjr.quizweb.exception.CustomException;
import cn.edu.fzu.sm2023.yjr.quizweb.mapper.QuestionMapper;
import cn.edu.fzu.sm2023.yjr.quizweb.service.QuestionService;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.PageVO;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QuestionServiceImpl implements QuestionService {
    @Resource
    private QuestionMapper questionMapper;

    @Override
    public List<Question> list() {
        return questionMapper.list();
    }

    @Override
    public PageVO<Question> page(Map<String, Object> query, Integer pageNum, Integer pageSize) {
        PageVO<Question> page = new PageVO();
        List<Question> list = questionMapper.queryPage((pageNum - 1) * pageSize, pageSize, query);
        page.setList(list);
        page.setTotal(questionMapper.queryCount(query));
        return page;
    }

    @Override
    public void updateById(Question entity) {
        check(entity);
        // 确保选项是JSON格式
        if (entity.getOptions() != null && !entity.getOptions().trim().startsWith("{")) {
            entity.setOptions(convertOptionsToJson(entity.getOptions()));
        }
        questionMapper.updateById(entity);
    }

    @Override
    public void insert(Question entity) {
        check(entity);
        // 确保选项是JSON格式
        if (entity.getOptions() != null && !entity.getOptions().trim().startsWith("{")) {
            entity.setOptions(convertOptionsToJson(entity.getOptions()));
        }
        questionMapper.insert(entity);
    }

    @Override
    public void removeByIds(List<Integer> ids) {
        questionMapper.removeByIds(ids);
    }

    @Override
    public List<Question> importExcel(MultipartFile file) throws IOException {
        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new CustomException("请选择要上传的文件");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new CustomException("请上传Excel文件（.xlsx或.xls格式）");
        }

        // 检查文件大小（限制10MB）
        long fileSize = file.getSize();
        if (fileSize > 10 * 1024 * 1024) {
            throw new CustomException("文件大小不能超过10MB");
        }

        List<Question> questionList = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream()) {
            // 使用Hutool读取Excel
            ExcelReader reader = ExcelUtil.getReader(inputStream);

            // 验证表头
            List<Object> header = reader.readRow(0);
            if (header == null || header.size() < 5) {
                throw new CustomException("Excel格式错误，请使用正确的模板");
            }

            // 读取所有行（从第二行开始，第一行是表头）
            List<List<Object>> rows = reader.read(1); // 从第2行开始读

            if (rows == null || rows.isEmpty()) {
                throw new CustomException("Excel中没有数据");
            }

            for (int i = 0; i < rows.size(); i++) {
                List<Object> row = rows.get(i);
                if (row == null || row.isEmpty()) {
                    continue; // 跳过空行
                }

                // 检查是否是说明行（跳过）
                if (row.get(0) != null && row.get(0).toString().startsWith("【")) {
                    continue;
                }

                Question question = new Question();

                // 读取各列数据
                question.setContent(row.size() > 0 && row.get(0) != null ? row.get(0).toString().trim() : "");
                question.setQuestionType(row.size() > 1 && row.get(1) != null ? row.get(1).toString().trim() : "");

                // 将选项字符串转换为JSON格式
                String optionsStr = row.size() > 2 && row.get(2) != null ? row.get(2).toString().trim() : "";
                question.setOptions(convertOptionsToJson(optionsStr));  // 使用转换方法

                question.setAnswer(row.size() > 3 && row.get(3) != null ? row.get(3).toString().trim() : "");
                question.setExplanation(row.size() > 4 && row.get(4) != null ? row.get(4).toString().trim() : "");

                // 验证必填字段
                if (StrUtil.isBlank(question.getContent())) {
                    throw new CustomException("第" + (i + 2) + "行：题目内容不能为空");
                }
                if (StrUtil.isBlank(question.getQuestionType())) {
                    throw new CustomException("第" + (i + 2) + "行：题型不能为空");
                }
                if (StrUtil.isBlank(optionsStr)) {
                    throw new CustomException("第" + (i + 2) + "行：选项不能为空");
                }
                if (StrUtil.isBlank(question.getAnswer())) {
                    throw new CustomException("第" + (i + 2) + "行：答案不能为空");
                }

                // 验证题型合法性
                String type = question.getQuestionType();
                if (!"单选题".equals(type) && !"多选题".equals(type) && !"判断题".equals(type)) {
                    throw new CustomException("第" + (i + 2) + "行：题型错误，只能是：单选题、多选题、判断题");
                }

                // 验证答案格式
                if ("单选题".equals(type)) {
                    if (!question.getAnswer().matches("^[A-D]$")) {
                        throw new CustomException("第" + (i + 2) + "行：单选题答案应为A、B、C、D中的单个字母");
                    }
                } else if ("多选题".equals(type)) {
                    if (!question.getAnswer().matches("^[A-D](,[A-D])*$")) {
                        throw new CustomException("第" + (i + 2) + "行：多选题答案应为用逗号分隔的字母，如：A,C");
                    }
                } else if ("判断题".equals(type)) {
                    if (!"正确".equals(question.getAnswer()) && !"错误".equals(question.getAnswer())) {
                        throw new CustomException("第" + (i + 2) + "行：判断题答案应为'正确'或'错误'");
                    }
                }

                questionList.add(question);
            }

            reader.close();
        } catch (IOException e) {
            throw new CustomException("文件读取失败：" + e.getMessage());
        } catch (Exception e) {
            if (e instanceof CustomException) {
                throw e;
            }
            throw new CustomException("文件解析失败：" + e.getMessage());
        }

        if (questionList.isEmpty()) {
            throw new CustomException("没有找到有效的题目数据");
        }

        return questionList;
    }

    // 添加选项转换方法
    private String convertOptionsToJson(String optionsStr) {
        if (StrUtil.isBlank(optionsStr)) {
            return "{}";
        }

        // 处理判断题的特殊情况
        if ("正确,错误".equals(optionsStr) || "错误,正确".equals(optionsStr)) {
            // 判断题的选项可以简单处理
            Map<String, String> optionsMap = new LinkedHashMap<>();
            optionsMap.put("A", "正确");
            optionsMap.put("B", "错误");
            return JSONUtil.toJsonStr(optionsMap);
        }

        // 使用正则表达式匹配选项格式，如：A.选项1,B.选项2,C.选项3
        Pattern pattern = Pattern.compile("([A-D])\\.[^,]*");
        Matcher matcher = pattern.matcher(optionsStr);

        Map<String, String> optionsMap = new LinkedHashMap<>();
        while (matcher.find()) {
            String match = matcher.group();
            String[] parts = match.split("\\.", 2);
            if (parts.length == 2) {
                optionsMap.put(parts[0].trim(), parts[1].trim());
            }
        }

        // 如果没有匹配到标准格式，尝试其他格式
        if (optionsMap.isEmpty()) {
            // 尝试按逗号分割
            String[] items = optionsStr.split(",");
            for (int i = 0; i < Math.min(items.length, 4); i++) {
                String item = items[i].trim();
                if (item.length() > 0) {
                    // 检查是否已经包含字母前缀
                    if (item.matches("^[A-D]\\..*")) {
                        String[] parts = item.split("\\.", 2);
                        if (parts.length == 2) {
                            optionsMap.put(parts[0].trim(), parts[1].trim());
                        }
                    } else {
                        // 自动添加字母前缀
                        char optionChar = (char) ('A' + i);
                        optionsMap.put(String.valueOf(optionChar), item);
                    }
                }
            }
        }

        // 如果还是为空，返回原始字符串包装的JSON
        if (optionsMap.isEmpty()) {
            Map<String, String> simpleMap = new HashMap<>();
            simpleMap.put("raw", optionsStr);
            return JSONUtil.toJsonStr(simpleMap);
        }

        return JSONUtil.toJsonStr(optionsMap);
    }

    @Override
    public String batchInsert(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new CustomException("导入数据不能为空");
        }

        int successCount = 0;
        int skipCount = 0;
        List<String> errorMessages = new ArrayList<>();

        // 批量插入
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            try {
                // 检查题目是否已存在
                Question existing = questionMapper.selectByContent(question.getContent());
                if (existing != null) {
                    skipCount++;
                    errorMessages.add("第" + (i + 1) + "条题目已存在，已跳过：" + question.getContent());
                    continue;
                }

                // 插入新题目
                questionMapper.insert(question);
                successCount++;

            } catch (Exception e) {
                errorMessages.add("第" + (i + 1) + "条题目导入失败：" + e.getMessage());
            }
        }

        StringBuilder result = new StringBuilder();
        result.append("导入完成。成功：").append(successCount)
                .append("条，跳过：").append(skipCount)
                .append("条，失败：").append(questions.size() - successCount - skipCount)
                .append("条");

        if (!errorMessages.isEmpty()) {
            result.append("\n\n详细错误信息：");
            for (String error : errorMessages) {
                result.append("\n").append(error);
            }
        }

        return result.toString();
    }

    private void check(Question entity) {
        if (entity == null) {
            throw new CustomException("题目信息不能为空");
        }

        Question question = questionMapper.selectByContent(entity.getContent());
        if (question != null && !question.getQuestionId().equals(entity.getQuestionId())) {
            throw new CustomException("题目已存在：" + entity.getContent());
        }
    }
}