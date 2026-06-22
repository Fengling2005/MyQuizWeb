package cn.edu.fzu.sm2023.yjr.quizweb.dto;

import lombok.Data;

@Data
public class UpdatePasswordDTO {
    private String oldPassword;
    private String newPassword;
}
