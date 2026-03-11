package com.oranges.fileuploaderbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 文件
 * @TableName file
 */
@TableName(value ="file")
@Data
public class FileInfo {
    /**
     * id
     */
//    @TableId(type = IdType.AUTO)
    @TableId(type = IdType.ASSIGN_ID)

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
     * 文件格式
     */
    private String fileFormat;

    /**
     * 文件类型（image/document/video等）
     */
    private String fileType;

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

    /**
     * 文件密码
     */
    private String filePassword;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        FileInfo other = (FileInfo) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUrl() == null ? other.getUrl() == null : this.getUrl().equals(other.getUrl()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getFileSize() == null ? other.getFileSize() == null : this.getFileSize().equals(other.getFileSize()))
            && (this.getFileFormat() == null ? other.getFileFormat() == null : this.getFileFormat().equals(other.getFileFormat()))
            && (this.getFileType() == null ? other.getFileType() == null : this.getFileType().equals(other.getFileType()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getIsEncryption() == null ? other.getIsEncryption() == null : this.getIsEncryption().equals(other.getIsEncryption()))
            && (this.getFilePassword() == null ? other.getFilePassword() == null : this.getFilePassword().equals(other.getFilePassword()))
            && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUrl() == null) ? 0 : getUrl().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getFileSize() == null) ? 0 : getFileSize().hashCode());
        result = prime * result + ((getFileFormat() == null) ? 0 : getFileFormat().hashCode());
        result = prime * result + ((getFileType() == null) ? 0 : getFileType().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getIsEncryption() == null) ? 0 : getIsEncryption().hashCode());
        result = prime * result + ((getFilePassword() == null) ? 0 : getFilePassword().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", url=").append(url);
        sb.append(", name=").append(name);
        sb.append(", fileSize=").append(fileSize);
        sb.append(", fileFormat=").append(fileFormat);
        sb.append(", fileType=").append(fileType);
        sb.append(", userId=").append(userId);
        sb.append(", createTime=").append(createTime);
        sb.append(", isEncryption=").append(isEncryption);
        sb.append(", filePassword=").append(filePassword);
        sb.append(", isDelete=").append(isDelete);
        sb.append("]");
        return sb.toString();
    }
}