package com.oranges.fileuploaderbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oranges.fileuploaderbackend.model.dto.user.UserQueryRequest;
import com.oranges.fileuploaderbackend.model.dto.user.UserRegisterRequest;
import com.oranges.fileuploaderbackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oranges.fileuploaderbackend.model.vo.LoginUserVO;
import com.oranges.fileuploaderbackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author chen zhi
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-12-24 09:07:44
*/
public interface UserService extends IService<User> {

        //用户注册方法
        Long userRegister(String userAccount , String userPassword , String checkPassword);

        //用户登录方法
        LoginUserVO userLogin(String userAccount , String userPassword, HttpServletRequest request);

        //用户注销
        boolean userLogout(HttpServletRequest request);

        //获取当前登录用户
        User getLoginUser(HttpServletRequest request);

        QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

        //获取脱敏登录用户
        LoginUserVO getLoginUserVO (User user);

        UserVO getUserVO (User user);


        List<UserVO> getUserVOList(List<User> userList);
}
