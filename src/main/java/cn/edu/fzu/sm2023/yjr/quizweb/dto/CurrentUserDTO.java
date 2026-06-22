package cn.edu.fzu.sm2023.yjr.quizweb.dto;

import lombok.Data;

@Data
public class CurrentUserDTO {
    private Integer id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String tel;
    private String email;
    private String status;
}
