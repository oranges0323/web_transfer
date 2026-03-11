package com.oranges.fileuploaderbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oranges.fileuploaderbackend.annotation.AuthCheck;
import com.oranges.fileuploaderbackend.common.BaseResponse;
import com.oranges.fileuploaderbackend.common.DeleteRequest;
import com.oranges.fileuploaderbackend.common.ResultUtils;
import com.oranges.fileuploaderbackend.exception.BusinessException;
import com.oranges.fileuploaderbackend.exception.ErrorCode;
import com.oranges.fileuploaderbackend.exception.ThrowUtils;
import com.oranges.fileuploaderbackend.model.dto.user.UserLoginRequest;
import com.oranges.fileuploaderbackend.model.dto.user.UserQueryRequest;
import com.oranges.fileuploaderbackend.model.dto.user.UserRegisterRequest;
import com.oranges.fileuploaderbackend.model.dto.user.UserUpdateRequest;
import com.oranges.fileuploaderbackend.model.entity.User;
import com.oranges.fileuploaderbackend.model.vo.LoginUserVO;
import com.oranges.fileuploaderbackend.model.vo.UserVO;
import com.oranges.fileuploaderbackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null , ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        Long result = userService.userRegister(userAccount, userPassword, checkPassword);

        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null , ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    @AuthCheck(mustRole = "user")
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null , ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    //管理员
    //经典增删改查，简单就不抽service了

    /**
     * 根据id获取用户（管理员）
     * @param id
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/get")
    public BaseResponse<User> getUserById(Long id) {
        ThrowUtils.throwIf(id <= 0 , ErrorCode.PARAMS_ERROR);
        User result = userService.getById(id);
        ThrowUtils.throwIf(result == null , ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(result);
    }

    /**
     * 根据id获取用户（用户）
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(Long id) {
        ThrowUtils.throwIf(id <= 0 , ErrorCode.PARAMS_ERROR);
        User result = userService.getById(id);
        ThrowUtils.throwIf(result == null , ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(result));
    }

    @AuthCheck(mustRole = "admin")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if(userUpdateRequest == null || userUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result , ErrorCode.NOT_FOUND_ERROR);

        return ResultUtils.success(true);
    }

    @AuthCheck(mustRole = "admin")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(DeleteRequest deleteRequest) {
        if(deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(deleteRequest.getId());

        return ResultUtils.success(result);
    }

    /**
     * 分页获取用户列表（管理员）
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/get/list/vo")
    public BaseResponse<Page<UserVO>> listUserVOById(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null , ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current,pageSize,userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
}
