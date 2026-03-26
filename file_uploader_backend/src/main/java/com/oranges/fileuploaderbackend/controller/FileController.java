package com.oranges.fileuploaderbackend.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oranges.fileuploaderbackend.annotation.AuthCheck;
import com.oranges.fileuploaderbackend.common.BaseResponse;
import com.oranges.fileuploaderbackend.common.ResultUtils;
import com.oranges.fileuploaderbackend.constant.UserConstant;
import com.oranges.fileuploaderbackend.exception.BusinessException;
import com.oranges.fileuploaderbackend.exception.ErrorCode;
import com.oranges.fileuploaderbackend.exception.ThrowUtils;
import com.oranges.fileuploaderbackend.manage.CosManage;
import com.oranges.fileuploaderbackend.mapper.FileMapper;

import com.oranges.fileuploaderbackend.model.dto.file.FileEncryptionRequest;
import com.oranges.fileuploaderbackend.model.dto.file.FileQueryRequest;
import com.oranges.fileuploaderbackend.model.dto.user.UserQueryRequest;
import com.oranges.fileuploaderbackend.model.entity.FileInfo;
import com.oranges.fileuploaderbackend.model.entity.User;
import com.oranges.fileuploaderbackend.model.vo.FileInfoVO;
import com.oranges.fileuploaderbackend.model.vo.UserVO;
import com.oranges.fileuploaderbackend.service.FileService;
import com.oranges.fileuploaderbackend.service.UserService;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {
    @Resource
    private FileService fileService;

    @Resource
    private UserService userService;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private CosManage cosManage;


    @AuthCheck(mustRole = "user")
    @PostMapping("/upload")
    public BaseResponse<FileInfoVO> upload(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        FileInfoVO fileInfoVO = fileService.uploadFile(multipartFile, loginUser);
        ThrowUtils.throwIf(fileInfoVO == null, ErrorCode.OPERATION_ERROR, "上传失败");
        return ResultUtils.success(fileInfoVO);
    }

    //    @GetMapping("/download")
//    public BaseResponse<Boolean> download(FileDownloadRequest fileDownloadRequest, HttpServletRequest httpServletRequest) {
//        User loginUser = userService.getLoginUser(httpServletRequest);
//        Boolean result = fileService.downloadFile(fileDownloadRequest, loginUser);
//        return ResultUtils.success(result);
//    }

    @AuthCheck(mustRole = "user")
    @GetMapping("/download")
    public ResponseEntity<UrlResource> downloadFile(@RequestParam String id) {
        // 获取文件信息
        FileInfo fileInfo = fileMapper.selectById(id);
        if (fileInfo == null) {
            return ResponseEntity.notFound().build();
        }

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getName() + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");

        // 返回文件流
        try {
            UrlResource resource = new UrlResource(fileInfo.getUrl());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .body(resource); // 直接使用 resource
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 分页获取文件列表
     */
//    @AuthCheck(mustRole = "admin")
    @GetMapping("/get/list/vo")
    public BaseResponse<Page<FileInfoVO>> listFileVOById(FileQueryRequest fileQueryRequest) {
        ThrowUtils.throwIf(fileQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = fileQueryRequest.getCurrent();
        long pageSize = fileQueryRequest.getPageSize();
        Page<FileInfo> fileInfoPage = fileService.page(new Page<>(current, pageSize),
                fileService.getQueryWrapper(fileQueryRequest));
        Page<FileInfoVO> fileVOPage = new Page<>(current, pageSize, fileInfoPage.getTotal());
        List<FileInfoVO> userVOList = fileService.getFileVOList(fileInfoPage.getRecords());
        fileVOPage.setRecords(userVOList);
        return ResultUtils.success(fileVOPage);
    }

    /**
     * 删除文件
     */
    @AuthCheck(mustRole = "user")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteFile( Long id, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        FileInfo fileInfo = fileMapper.selectById(id);
        ThrowUtils.throwIf(fileInfo == null, ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        // 检查权限：只有文件所有者或管理员可以删除文件
        if (!fileInfo.getUserId().equals(loginUser.getId()) && !loginUser.getUserRole().equals("admin")) {
            ThrowUtils.throwIf(true, ErrorCode.NO_AUTH_ERROR, "没有权限删除该文件");
        }
        fileInfo.setIsDelete(1);
        int i = fileMapper.updateById(fileInfo);

        return ResultUtils.success(i > 0);
    }


    @PostMapping("/encrypt")
    public BaseResponse<Boolean> encrypt(FileEncryptionRequest fileEncryptionRequest, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        Boolean result = fileService.encryptFile(fileEncryptionRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/decrypt")
    public BaseResponse<Boolean> decrypt(Long fileId, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        Boolean result = fileService.decryptFile(fileId, loginUser);

        return ResultUtils.success(result);
    }


}
