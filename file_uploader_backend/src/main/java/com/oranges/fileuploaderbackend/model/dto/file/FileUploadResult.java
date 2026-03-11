package com.oranges.fileuploaderbackend.model.dto.file;

import lombok.Data;

import java.io.Serializable;

@Data
public class FileUploadResult implements Serializable {
    private static final long serialVersionUID = 1L;

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
     * 文件格式
     */
    private String fileFormat;

    /**
     * 文件类型（image/document/video等）
     */
    private String fileType;

}
