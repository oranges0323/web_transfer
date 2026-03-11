package com.oranges.fileuploaderbackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserUpdateInfoRequest implements Serializable {


    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 密码
     */
    private String newPassword;

    /**
     * 确认密码
     */
    private String checkPassword;



    private static final long serialVersionUID = 1L;
}
