package cn.edu.fzu.sm2023.yjr.quizweb.vo;


import lombok.Data;

import java.util.List;

@Data
public class PageVO<T> {
    List<T> list;
    long total;
}
