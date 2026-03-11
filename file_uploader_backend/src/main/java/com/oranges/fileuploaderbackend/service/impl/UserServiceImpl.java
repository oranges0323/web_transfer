package com.oranges.fileuploaderbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oranges.fileuploaderbackend.constant.UserConstant;
import com.oranges.fileuploaderbackend.exception.BusinessException;
import com.oranges.fileuploaderbackend.exception.ErrorCode;
import com.oranges.fileuploaderbackend.model.dto.user.UserQueryRequest;
import com.oranges.fileuploaderbackend.model.enums.UserRoleEnum;
import com.oranges.fileuploaderbackend.model.vo.LoginUserVO;
import com.oranges.fileuploaderbackend.model.vo.UserVO;
import com.oranges.fileuploaderbackend.service.UserService;
import com.oranges.fileuploaderbackend.model.entity.User;
import com.oranges.fileuploaderbackend.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author chen zhi
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-12-24 09:07:44
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public Long userRegister(String userAccount,String userPassword,String checkPassword) {
        //  参数校验
        if(StrUtil.hasEmpty(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if(userPassword.length()<8 || checkPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }
        //检查账户是否与数据库中的重复
        QueryWrapper <User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        if(userMapper.selectOne(queryWrapper) != null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号已存在");
        }
        //密码加密
        String encryptPassword = getEncryptPassword(userPassword);
        //操作数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserAvatar("https://img.ixintu.com/download/jpg/20201121/e8a5f006ec63a963c04a2b3116997e89_512_512.jpg!ys");
        user.setUserName("默认用户");
        user.setUserRole(UserRoleEnum.USER.getValue());
        user.setCreateTime(new Date());
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"用户注册失败,数据库错误");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //  参数校验
        if(StrUtil.hasEmpty(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }
        if(userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }
        //加密
        userPassword = getEncryptPassword(userPassword);
        //检查账户密码是否正确
        QueryWrapper <User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount)
                    .eq("userPassword",userPassword);

        User user = this.baseMapper.selectOne(queryWrapper);
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在或密码错误");
        }
        //设置登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE,user);
        return this.getLoginUserVO(user);
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        //先判断是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if(userObj == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //清除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);

        return true;
    }


    @Override
    public User getLoginUser(HttpServletRequest request) {
        //先判断是否登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if(currentUser == null || currentUser.getId() == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        //查询用户是否存在
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        return currentUser;
    }
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    //todo
    /**
     * 用户管理（管理员）
     * 创建用户
     * 更新用户
     * 删除用户
     * 分页查询用户（要脱敏）
     * 根据id获取用户（未脱敏）
     *
     * （用户）
     * 根据id获取用户（脱敏）
     * 用户个人信息修改
     */



    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if(user == null){
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user,loginUserVO);
        return loginUserVO;
    }


    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }


    //加盐方法
    private String getEncryptPassword(String userPassword) {
        // 加盐混淆密码
        final String SALT = "oranges";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }
}




