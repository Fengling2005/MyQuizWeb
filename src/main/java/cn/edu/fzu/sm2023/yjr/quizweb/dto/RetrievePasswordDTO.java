package cn.edu.fzu.sm2023.yjr.quizweb.dto;

import lombok.Data;

@Data
public class RetrievePasswordDTO {
    private String tel;
    private String username;
    private String password;
}
