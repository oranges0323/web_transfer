package com.oranges.fileuploaderbackend.model.dto.file;

import lombok.Data;

import java.io.Serializable;


@Data
public class FileEncryptionRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;

    /**
     * 是否加密
     */
    private Integer isEncryption;

    /**
     * 文件密码
     */
    private String filePassword;
}

