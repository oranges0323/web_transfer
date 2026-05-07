package com.oranges.fileuploaderbackend.service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oranges.fileuploaderbackend.model.dto.file.FileDownloadRequest;
import com.oranges.fileuploaderbackend.model.dto.file.FileEncryptionRequest;
import com.oranges.fileuploaderbackend.model.dto.file.FileQueryRequest;
import com.oranges.fileuploaderbackend.model.dto.file.FileUploadResult;
import com.oranges.fileuploaderbackend.model.entity.FileInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oranges.fileuploaderbackend.model.entity.User;
import com.oranges.fileuploaderbackend.model.vo.FileInfoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @author chen zhi
* @description 针对表【file(文件)】的数据库操作Service
* @createDate 2026-02-03 17:44:47
*/
public interface FileService extends IService<FileInfo> {

        FileInfoVO uploadFile(MultipartFile multipartFile, User loginUser);

        FileUploadResult uploadCOSFile(MultipartFile multipartFile, String uploadPathPrefix);

        Boolean downloadFile(FileDownloadRequest fileDownloadRequest, User loginUser);

        Boolean encryptFile(FileEncryptionRequest fileEncryptionRequest, User loginUser);

        Boolean decryptFile(Long fileId, User loginUser);

        FileInfo getLoginUser(FileInfoVO fileInfoVO);

        FileInfoVO getFileInfoVO(FileInfo fileInfo);

        QueryWrapper<FileInfo> getQueryWrapper(FileQueryRequest fileQueryRequest);

        List<FileInfoVO> getFileVOList(List<FileInfo> fileInfoList);

        String getEncryptPassword(String filePassword);
}
