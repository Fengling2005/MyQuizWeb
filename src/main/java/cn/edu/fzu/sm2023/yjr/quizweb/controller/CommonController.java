package cn.edu.fzu.sm2023.yjr.quizweb.controller;

import com.alibaba.fastjson2.JSONObject;
import cn.edu.fzu.sm2023.yjr.quizweb.dto.CurrentUserDTO;
import cn.edu.fzu.sm2023.yjr.quizweb.dto.LoginDTO;
import cn.edu.fzu.sm2023.yjr.quizweb.dto.RetrievePasswordDTO;
import cn.edu.fzu.sm2023.yjr.quizweb.dto.UpdatePasswordDTO;
import cn.edu.fzu.sm2023.yjr.quizweb.service.AdminService;
import cn.edu.fzu.sm2023.yjr.quizweb.utils.CurrentUserThreadLocal;
import cn.edu.fzu.sm2023.yjr.quizweb.utils.JwtUtils;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.ResponseVO;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 通用
 */
@RestController
@RequestMapping("/common")
public class CommonController {

    @Resource
    private AdminService adminService;

    /**
     * 登录
     *
     * @param loginDTO
     * @return
     */
    @PostMapping("login")
    public ResponseVO<String> login(@RequestBody LoginDTO loginDTO) {
        CurrentUserDTO currentUserDTO = adminService.login(loginDTO.getUsername(), loginDTO.getPassword());
        String token = JwtUtils.generateToken(currentUserDTO);
        return ResponseVO.ok(token);
    }

    /**
     * 注册
     *
     * @param data
     */
    @PutMapping("register")
    public ResponseVO register(@RequestBody JSONObject data) {
        adminService.register(data);
        return ResponseVO.ok();
    }

    /**
     * 修改当前用户信息
     *
     * @param currentUserDTO
     */

    @PostMapping("updateCurrentUser")
    public ResponseVO updateCurrentUser(@RequestBody CurrentUserDTO currentUserDTO) {
        adminService.updateCurrentUserInfo(currentUserDTO);
        return ResponseVO.ok();
    }

    /**
     * 修改密码
     *
     * @param updatePassword
     */

    @PostMapping("updatePassword")
    public ResponseVO updatePassword(@RequestBody UpdatePasswordDTO updatePassword) {
        adminService.updateCurrentUserPassword(updatePassword);
        return ResponseVO.ok();
    }

    /**
     * 忘记密码（无需验证码，通过用户名和电话验证）
     * @param retrievePasswordDTO
     * @return
     */

    @PostMapping("retrievePassword")
    public ResponseVO retrievePassword(@RequestBody RetrievePasswordDTO retrievePasswordDTO) {
        adminService.retrievePassword(retrievePasswordDTO);
        return ResponseVO.ok();
    }

    /**
     * 重置密码
     *
     * @param id
     */

    @PostMapping("resetPassword")
    public ResponseVO resetPassword(@RequestParam Integer id) {
        adminService.resetPassword(id);
        return ResponseVO.ok();
    }


    /**
     * 获取当前用户
     *
     * @return
     */
    @GetMapping("currentUser")
    public ResponseVO<CurrentUserDTO> getCurrentUser() {
        Integer userId = CurrentUserThreadLocal.getCurrentUser().getId();
        CurrentUserDTO currentUserDTO = new CurrentUserDTO();
        BeanUtils.copyProperties(adminService.selectById(userId), currentUserDTO);
        return ResponseVO.ok(currentUserDTO);
    }
}