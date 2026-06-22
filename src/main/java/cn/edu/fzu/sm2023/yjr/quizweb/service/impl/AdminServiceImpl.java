package cn.edu.fzu.sm2023.yjr.quizweb.service.impl;

import com.alibaba.fastjson2.JSONObject;
import cn.edu.fzu.sm2023.yjr.quizweb.dto.CurrentUserDTO;
import cn.edu.fzu.sm2023.yjr.quizweb.dto.RetrievePasswordDTO;
import cn.edu.fzu.sm2023.yjr.quizweb.dto.UpdatePasswordDTO;
import cn.edu.fzu.sm2023.yjr.quizweb.entity.Admin;
import cn.edu.fzu.sm2023.yjr.quizweb.exception.CustomException;
import cn.edu.fzu.sm2023.yjr.quizweb.mapper.AdminMapper;
import cn.edu.fzu.sm2023.yjr.quizweb.service.AdminService;
import cn.edu.fzu.sm2023.yjr.quizweb.utils.CurrentUserThreadLocal;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.PageVO;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 */
@Service
public class AdminServiceImpl implements AdminService {
    @Resource
    private AdminMapper adminMapper;

    @Value("${resetPassword}")
    private String resetPassword;

    @Override
    public PageVO<Admin> page(Map<String, Object> query, Integer pageNum, Integer pageSize) {
        PageVO<Admin> page = new PageVO();
        List<Admin> list = adminMapper.queryPage((pageNum - 1) * pageSize, pageSize, query);
        page.setList(list);
        page.setTotal(adminMapper.queryCount(query));
        return page;
    }

    @Override
    public Admin selectById(Integer id) {
        Admin admin = adminMapper.selectById(id);
        return admin;
    }

    @Override
    public List<Admin> list() {
        return adminMapper.list();
    }

    @Override
    public void insert(Admin entity) {
        check(entity);
        if (entity.getPassword() == null) {
            entity.setPassword(resetPassword);
        }
        adminMapper.insert(entity);
    }

    @Override
    public void updateById(Admin entity) {
        check(entity);
        adminMapper.updateById(entity);
    }

    private void check(Admin entity) {
        Admin admin = adminMapper.selectByUserName(entity.getUsername());
        if (admin != null && admin.getId() != entity.getId()) {
            throw new CustomException("用户名已存在");
        }
    }

    @Override
    public void removeByIds(List<Integer> ids) {
        adminMapper.removeByIds(ids);
    }

    @Override
    public CurrentUserDTO login(String username, String password) {
        Admin admin = adminMapper.selectByUserName(username);
        if (admin == null || !admin.getPassword().equals(password)) {
            throw new CustomException("用户名或密码错误");
        }
        if (admin.getStatus().equals("禁用")) {
            throw new CustomException("用户已禁用");
        }
        CurrentUserDTO currentUserDTO = new CurrentUserDTO();
        BeanUtils.copyProperties(admin, currentUserDTO);
        return currentUserDTO;
    }

    @Override
    public void register(JSONObject data) {
        Admin admin = new Admin();
        admin.setUsername(data.getString("username"));
        admin.setNickname(data.getString("nickname"));
        admin.setAvatarUrl(data.getString("avatarUrl"));
        admin.setPassword(data.getString("password"));
        admin.setStatus("启用");
        insert(admin);
    }


    @Override
    public void updateCurrentUserInfo(CurrentUserDTO currentUserDTO) {
        Admin admin = adminMapper.selectById(currentUserDTO.getId());
        admin.setId(currentUserDTO.getId());
        admin.setNickname(currentUserDTO.getNickname());
        admin.setAvatarUrl(currentUserDTO.getAvatarUrl());
        admin.setTel(currentUserDTO.getTel());
        admin.setEmail(currentUserDTO.getEmail());
        adminMapper.updateById(admin);
    }

    @Override
    public void updateCurrentUserPassword(UpdatePasswordDTO updatePassword) {
        Admin admin = adminMapper.selectById(CurrentUserThreadLocal.getCurrentUser().getId());
        if (!admin.getPassword().equals(updatePassword.getOldPassword())) {
            throw new CustomException("旧密码不正确");
        }
        admin.setPassword(updatePassword.getNewPassword());
        adminMapper.updateById(admin);
    }

    @Override
    public void resetPassword(Integer id) {
        Admin admin = adminMapper.selectById(id);
        admin.setPassword(resetPassword);
        adminMapper.updateById(admin);
    }

    @Override
    public void retrievePassword(RetrievePasswordDTO retrievePasswordDTO) {
        // 1. 根据用户名查找用户
        Admin admin = adminMapper.selectByUserName(retrievePasswordDTO.getUsername());

        if (admin == null) {
            throw new CustomException("用户名不存在");
        }

        // 2. 检查用户状态
        if ("禁用".equals(admin.getStatus())) {
            throw new CustomException("用户已被禁用，无法重置密码");
        }

        // 3. 验证手机号是否匹配（不进行格式验证，只比较内容）
        String storedTel = admin.getTel();
        String inputTel = retrievePasswordDTO.getTel();

        if (storedTel == null || storedTel.trim().isEmpty()) {
            throw new CustomException("该用户未绑定手机号，请联系管理员");
        }

        // 去除空格后比较
        if (!storedTel.trim().equals(inputTel.trim())) {
            throw new CustomException("手机号与用户名不匹配");
        }

        // 4. 检查新密码是否与旧密码相同
        if (retrievePasswordDTO.getPassword().equals(admin.getPassword())) {
            throw new CustomException("新密码不能与旧密码相同");
        }

        // 5. 更新密码
        admin.setPassword(retrievePasswordDTO.getPassword());
        adminMapper.updateById(admin);
    }
}