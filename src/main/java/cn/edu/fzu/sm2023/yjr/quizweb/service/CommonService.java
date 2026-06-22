package cn.edu.fzu.sm2023.yjr.quizweb.service;

import com.alibaba.fastjson2.JSONObject;
import cn.edu.fzu.sm2023.yjr.quizweb.dto.CurrentUserDTO;
import cn.edu.fzu.sm2023.yjr.quizweb.dto.RetrievePasswordDTO;
import cn.edu.fzu.sm2023.yjr.quizweb.dto.UpdatePasswordDTO;

public interface CommonService<T> {
    /**
     * 登录
     * @param username
     * @param password
     * @return
     */
    CurrentUserDTO login(String username, String password);

    /**
     * 注册
     * @param data
     */

    void register(JSONObject data);

    /**
     * 更新当前用户信息
     * @param currentUserDTO
     */

    void updateCurrentUserInfo(CurrentUserDTO currentUserDTO);

    /**
     * 修改当前用户密码
     * @param updatePassword
     */

    void updateCurrentUserPassword(UpdatePasswordDTO updatePassword);

    /**
     * 重置密码
     * @param id
     */
    void resetPassword(Integer id);

    /**
     * 忘记密码
     * @param retrievePasswordDTO
     */

    void retrievePassword(RetrievePasswordDTO retrievePasswordDTO);

    /**
     * 查询当前用户信息
     * @param id
     * @return
     */

    T selectById(Integer id);

}