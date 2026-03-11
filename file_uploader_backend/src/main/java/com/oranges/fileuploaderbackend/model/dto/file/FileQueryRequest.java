package com.oranges.fileuploaderbackend.model.dto.file;

import com.oranges.fileuploaderbackend.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class FileQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 文件类型（image/document/video等）
     */
    private String fileType;

    /**
     * 文件格式
     */
    private String fileFormat;

    /**
     * 创建用户 id
     */
    private Long userId;


    /**
     * 是否加密
     */
    private Integer isEncryption;

}
