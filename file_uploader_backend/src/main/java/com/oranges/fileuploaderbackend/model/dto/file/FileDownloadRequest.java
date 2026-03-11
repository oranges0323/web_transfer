package com.oranges.fileuploaderbackend.model.dto.file;

import lombok.Data;

import java.io.Serializable;


@Data
public class FileDownloadRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Long id;

    /**
     * 文件密码
     */
    private String filePassword;

    /**
     * 下载路径
     */
    private String downloadPath;
}

