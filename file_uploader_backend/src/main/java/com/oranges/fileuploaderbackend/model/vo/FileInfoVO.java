package com.oranges.fileuploaderbackend.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 文件
 * @TableName file
 */
@TableName(value ="file")
@Data
public class FileInfoVO {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 文件 url
     */
    private String url;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 文件体积
     */
    private Long fileSize;


    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否加密
     */
    private Integer isEncryption;

}