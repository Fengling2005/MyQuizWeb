package cn.edu.fzu.sm2023.yjr.quizweb.service;

import cn.edu.fzu.sm2023.yjr.quizweb.entity.Admin;
import cn.edu.fzu.sm2023.yjr.quizweb.vo.PageVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 */
public interface AdminService extends CommonService {

    PageVO<Admin> page(Map<String, Object> query, Integer pageNum, Integer pageSize);

    Admin selectById(Integer id);

    List<Admin> list();

    void insert(Admin entity);

    void updateById(Admin entity);

    void removeByIds(List<Integer> id);


}
