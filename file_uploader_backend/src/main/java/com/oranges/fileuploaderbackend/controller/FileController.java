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


    @PostMapping("/upload")
    public BaseResponse<FileInfoVO> upload(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "未登录");

        // 调用 service 处理上传文件
        FileInfoVO fileInfoVO = fileService.uploadFile(multipartFile, loginUser);
        ThrowUtils.throwIf(fileInfoVO == null, ErrorCode.OPERATION_ERROR, "上传失败");

        log.info("{}上传了{}文件", loginUser.getUserName(), fileInfoVO.getName());

        return ResultUtils.success(fileInfoVO);
    }

    //    @GetMapping("/download")
//    public BaseResponse<Boolean> download(FileDownloadRequest fileDownloadRequest, HttpServletRequest httpServletRequest) {
//        User loginUser = userService.getLoginUser(httpServletRequest);
//        Boolean result = fileService.downloadFile(fileDownloadRequest, loginUser);
//        return ResultUtils.success(result);
//    }
    //todo
    //把加密删掉，要下载直接让前端跳转到解密，先解密在下载
    @GetMapping("/download")
    public ResponseEntity<UrlResource> downloadFile(@RequestParam String id,String filePassword) {

        // 获取文件信息
        FileInfo fileInfo = fileMapper.selectById(id);

        if (fileInfo == null) {
            return ResponseEntity.notFound().build();
        }
        //检查是否加密
        if (fileInfo.getIsEncryption() == 1) {
            String encryptPassword = fileService.getEncryptPassword(filePassword);
            //校验密码
            if (!encryptPassword.equals(fileInfo.getFilePassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }


        // 设置响应头
        HttpHeaders headers = new HttpHeaders();

//        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getName() + "\"");
        //告诉浏览器，这是附件直接触发下载
        try {
            String encodedFileName = java.net.URLEncoder.encode(fileInfo.getName(), "UTF-8");
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"");
        } catch (Exception e) {
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileInfo.getName() + "\"");
        }
        //告知浏览器文件类型
        headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
        //设置断点续传
        headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");

        // 返回文件流
        try {
            UrlResource resource = new UrlResource(fileInfo.getUrl());
            log.info("下载了{}文件", fileInfo.getName());
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
    @GetMapping("/get/list/vo")
    public BaseResponse<Page<FileInfoVO>> listFileVOById(FileQueryRequest fileQueryRequest) {
        ThrowUtils.throwIf(fileQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = fileQueryRequest.getCurrent();
        long pageSize = fileQueryRequest.getPageSize();

        // 强制设置为null，确保查询所有用户的文件，不限制用户ID
        fileQueryRequest.setUserId(null);

        Page<FileInfo> fileInfoPage = fileService.page(new Page<>(current, pageSize),
                fileService.getQueryWrapper(fileQueryRequest));
        Page<FileInfoVO> fileVOPage = new Page<>(current, pageSize, fileInfoPage.getTotal());
        List<FileInfoVO> userVOList = fileService.getFileVOList(fileInfoPage.getRecords());
        fileVOPage.setRecords(userVOList);

//        log.info("获取了文件列表");
        return ResultUtils.success(fileVOPage);
    }

    /**
     * 删除文件
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteFile(Long id, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        FileInfo fileInfo = fileMapper.selectById(id);
        ThrowUtils.throwIf(fileInfo == null, ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        // 检查权限：只有文件所有者或管理员可以删除文件
        if (!fileInfo.getUserId().equals(loginUser.getId()) && !loginUser.getUserRole().equals("admin")) {
            ThrowUtils.throwIf(true, ErrorCode.NO_AUTH_ERROR, "没有权限删除该文件");
        }
        fileInfo.setIsDelete(1);
        int i = fileMapper.updateById(fileInfo);

        ThrowUtils.throwIf(i <= 0, ErrorCode.OPERATION_ERROR, "删除失败");

        log.info("{}删除了{}文件", loginUser.getUserName(), fileInfo.getName());

        return ResultUtils.success(i > 0);
    }


    @PostMapping("/encrypt")
    public BaseResponse<Boolean> encrypt(FileEncryptionRequest fileEncryptionRequest, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        Boolean result = fileService.encryptFile(fileEncryptionRequest, loginUser);

        log.info("{}加密了{}文件", loginUser.getUserName(), fileEncryptionRequest.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/decrypt")
    public BaseResponse<Boolean> decrypt(Long fileId, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        Boolean result = fileService.decryptFile(fileId, loginUser);

        log.info("{}解密了{}文件", loginUser.getUserName(), fileId);

        return ResultUtils.success(result);
    }


}
