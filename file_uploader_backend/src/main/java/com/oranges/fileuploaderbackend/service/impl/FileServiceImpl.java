package com.oranges.fileuploaderbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oranges.fileuploaderbackend.config.COSClientConfig;
import com.oranges.fileuploaderbackend.exception.BusinessException;
import com.oranges.fileuploaderbackend.exception.ErrorCode;
import com.oranges.fileuploaderbackend.exception.ThrowUtils;
import com.oranges.fileuploaderbackend.manage.CosManage;
import com.oranges.fileuploaderbackend.model.dto.file.FileDownloadRequest;
import com.oranges.fileuploaderbackend.model.dto.file.FileEncryptionRequest;
import com.oranges.fileuploaderbackend.model.dto.file.FileQueryRequest;
import com.oranges.fileuploaderbackend.model.dto.file.FileUploadResult;
import com.oranges.fileuploaderbackend.model.entity.FileInfo;
import com.oranges.fileuploaderbackend.model.entity.User;
import com.oranges.fileuploaderbackend.model.vo.FileInfoVO;
import com.oranges.fileuploaderbackend.service.FileService;
import com.oranges.fileuploaderbackend.mapper.FileMapper;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author chen zhi
 * @description 针对表【file(文件)】的数据库操作Service实现
 * @createDate 2026-02-03 17:44:47
 */
@Slf4j
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, FileInfo>
        implements FileService {

    @Resource
    private FileMapper fileMapper;
    @Resource
    private COSClient cosClient;
    @Resource
    private CosManage cosManage;
    @Resource
    private COSClientConfig cosClientConfig;


    @Override
    public FileInfoVO uploadFile(MultipartFile multipartFile, User loginUser) {
        //校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");


        //上传
        //上传到COS的路径前缀，分id保存
        String uploadPathPrefix = String.format("public/%s", loginUser.getId());

        FileUploadResult fileUploadResult = uploadCOSFile(multipartFile, uploadPathPrefix);

        //存入数据库
        FileInfo fileInfo = new FileInfo();
        fileInfo.setUrl(fileUploadResult.getUrl());
        fileInfo.setName(fileUploadResult.getName());
        fileInfo.setFileSize(fileUploadResult.getFileSize());
        fileInfo.setFileFormat(fileUploadResult.getFileFormat());
        fileInfo.setFileType(fileUploadResult.getFileType());
        fileInfo.setUserId(loginUser.getId());
        fileInfo.setCreateTime(DateUtil.date());

        fileMapper.insert(fileInfo);

        return getFileInfoVO(fileInfo);
    }

    /**
     * 上传到COS
     *
     * @param multipartFile
     * @param uploadPathPrefix
     * @return
     */
    @Override
    public FileUploadResult uploadCOSFile(MultipartFile multipartFile, String uploadPathPrefix) {
        //校验
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        ThrowUtils.throwIf(multipartFile.getSize() > 1024 * 1024 * 1024 * 5L, ErrorCode.PARAMS_ERROR, "文件大小不能超过5GB");

        //设置文件名
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        //文件类型
        String fileTypeCategory = getFileTypeCategory(multipartFile.getContentType(), FileUtil.getSuffix(originalFilename));

        //文件名 = 时间+uuid+后缀
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        //文件中的完整路径加文件名,分用户id存储
        String uploadPath = String.format("/%s/%s/%s", uploadPathPrefix, fileTypeCategory, uploadFileName);

        File tempFile = null;
        FileUploadResult result = new FileUploadResult();

        //上传到COS
        try {
            tempFile = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(tempFile);
            PutObjectResult putObjectResult = cosManage.upload(tempFile, uploadPath);

            //上传需要返回FileUploadResult
            result.setUrl(cosClientConfig.getHost() + '/' + uploadPath);
            result.setName(FileUtil.getName(originalFilename));
            result.setFileFormat(FileUtil.getSuffix(originalFilename));
            result.setFileSize(multipartFile.getSize());
            result.setFileType(fileTypeCategory);

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败");
        } finally {
            if (tempFile != null) {
                boolean del = FileUtil.del(tempFile);
                if (!del) {
                    log.error("临时文件删除失败");
                }
            }
        }

        return result;
    }


    /**
     * 废弃方法
     */
    @Deprecated
    @Override
    public Boolean downloadFile(FileDownloadRequest fileDownloadRequest, User loginUser) {
        //校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        ThrowUtils.throwIf(fileDownloadRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(fileDownloadRequest.getId() == null, ErrorCode.PARAMS_ERROR, "id为空");
        ThrowUtils.throwIf(fileDownloadRequest.getDownloadPath() == null, ErrorCode.PARAMS_ERROR, "下载路径为空");
        //查询文件是否加密，加密则输密码
//        QueryWrapper<FileInfo> queryWrapper = new QueryWrapper<>();
//        QueryWrapper<FileInfo> q = queryWrapper.eq("id", fileDownloadRequest.getId());
//        FileInfo fileInfo = fileMapper.selectOne(q);
        FileInfo fileInfo = fileMapper.selectById(fileDownloadRequest.getId());
        //校验文件是否存在
        ThrowUtils.throwIf(fileInfo == null, ErrorCode.PARAMS_ERROR, "文件不存在");

        // 从完整URL中提取COS对象路径（去掉host部分）
        String cosKey = fileInfo.getUrl().replace(cosClientConfig.getHost() + "/", "");

        if (fileInfo.getIsEncryption() == 0) {
            // 下载文件
            ObjectMetadata downloadResult = cosManage.download(cosKey, fileDownloadRequest.getDownloadPath() + "/" + fileInfo.getName());
            ThrowUtils.throwIf(downloadResult == null, ErrorCode.PARAMS_ERROR, "下载失败");

        } else {
            //校验密码再下载
            ThrowUtils.throwIf(!fileInfo.getFilePassword().equals(fileDownloadRequest.getFilePassword()), ErrorCode.PARAMS_ERROR, "密码错误");

            // 下载文件
            ObjectMetadata downloadResult = cosManage.download(cosKey, fileDownloadRequest.getDownloadPath() + "/" + fileInfo.getName());
            ThrowUtils.throwIf(downloadResult == null, ErrorCode.PARAMS_ERROR, "下载失败");
        }

        return true;
    }


    @Override
    public Boolean encryptFile(FileEncryptionRequest fileEncryptionRequest, User loginUser) {
        //校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        ThrowUtils.throwIf(fileEncryptionRequest == null, ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(fileEncryptionRequest.getIsEncryption() == 0, ErrorCode.PARAMS_ERROR, "不加密");
        ThrowUtils.throwIf(fileEncryptionRequest.getId() == null, ErrorCode.PARAMS_ERROR, "id为空");

        FileInfo fileInfo = fileMapper.selectById(fileEncryptionRequest.getId());
        //校验文件是否存在
        ThrowUtils.throwIf(fileInfo == null, ErrorCode.PARAMS_ERROR, "文件不存在");
        //检查登录用户是否是文件上传用户
        ThrowUtils.throwIf(!fileInfo.getUserId().equals(loginUser.getId()), ErrorCode.PARAMS_ERROR, "文件不属于该用户");
        //加密文件

        // 加盐混淆密码
        String encryptPassword = getEncryptPassword(fileEncryptionRequest.getFilePassword());
        fileInfo.setIsEncryption(1);
        fileInfo.setFilePassword(encryptPassword);

        int updateResult = fileMapper.updateById(fileInfo);

        return updateResult > 0;
    }

    @Override
    public Boolean decryptFile(Long fileId, User loginUser) {
        //校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        ThrowUtils.throwIf(fileId == null, ErrorCode.PARAMS_ERROR, "id为空");
        FileInfo fileInfo = fileMapper.selectById(fileId);
        //校验文件是否存在
        ThrowUtils.throwIf(fileInfo == null, ErrorCode.PARAMS_ERROR, "文件不存在");
        //检查登录用户是否是文件上传用户
        ThrowUtils.throwIf(!fileInfo.getUserId().equals(loginUser.getId()), ErrorCode.PARAMS_ERROR, "文件不属于该用户");
        //解密文件
        fileInfo.setIsEncryption(0);
        //不清空密码也可以，不是很重要
//        fileInfo.setFilePassword(null);
        int updateResult = fileMapper.updateById(fileInfo);

        return updateResult > 0;
    }

    @Override
    public FileInfo getLoginUser(FileInfoVO fileInfoVO) {
        if (fileInfoVO == null) {
            return null;
        }
        FileInfo fileInfo = new FileInfo();
        BeanUtils.copyProperties(fileInfoVO, fileInfo);
        return fileInfo;
    }

    @Override
    public FileInfoVO getFileInfoVO(FileInfo fileInfo) {
        if (fileInfo == null) {
            return null;
        }
        FileInfoVO fileInfoVO = new FileInfoVO();
        BeanUtils.copyProperties(fileInfo, fileInfoVO);
        return fileInfoVO;
    }

    @Override
    public QueryWrapper<FileInfo> getQueryWrapper(FileQueryRequest fileQueryRequest) {
        if (fileQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        QueryWrapper<FileInfo> queryWrapper = new QueryWrapper<>();
        String id = fileQueryRequest.getId();
        String name = fileQueryRequest.getName();
        String fileFormat = fileQueryRequest.getFileFormat();
        String fileType = fileQueryRequest.getFileType();
        Integer isEncryption = fileQueryRequest.getIsEncryption();
        Long userId = fileQueryRequest.getUserId();

        // 添加查询条件
        queryWrapper.eq(StrUtil.isNotBlank(id), "id", id);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);  // 通常文件名搜索使用 like 更合理
        queryWrapper.eq(StrUtil.isNotBlank(fileFormat), "fileFormat", fileFormat);
        queryWrapper.eq(StrUtil.isNotBlank(fileType), "fileType", fileType);
        queryWrapper.eq(ObjUtil.isNotNull(isEncryption), "isEncryption", isEncryption);
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);

        // 添加逻辑删除过滤条件
        queryWrapper.eq("isDelete", 0);

        return queryWrapper;  // 返回构建好地查询条件
    }

    @Override
    public List<FileInfoVO> getFileVOList(List<FileInfo> fileInfoList) {

        if (CollUtil.isEmpty(fileInfoList)) {
            return new ArrayList<>();
        }
//        List<FileInfoVO> result = new ArrayList<>();
//        for (FileInfo fileInfo : fileInfoList) {
//            result.add(getFileInfoVO(fileInfo));
//        }等于这个
        return fileInfoList.stream().map(this::getFileInfoVO).collect(Collectors.toList());

    }


    /**
     * 将MIME类型或后缀转换为文件类型分类
     */
    private String getFileTypeCategory(String mimeType, String suffix) {
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return "image";
            } else if (mimeType.startsWith("video/")) {
                return "video";
            } else if (mimeType.startsWith("audio/")) {
                return "audio";
            } else if (mimeType.contains("pdf") ||
                    mimeType.contains("word") ||
                    mimeType.contains("excel") ||
                    mimeType.contains("text") ||
                    mimeType.contains("presentation")) {
                return "document";
            }
        }
        // 根据后缀判断
        String lowerSuffix = suffix.toLowerCase();
        if (Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico").contains(lowerSuffix)) {
            return "image";
        } else if (Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "mkv", "webm").contains(lowerSuffix)) {
            return "video";
        } else if (Arrays.asList("mp3", "wav", "flac", "aac", "ogg", "m4a").contains(lowerSuffix)) {
            return "audio";
        } else if (Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt",
                "rtf", "csv", "md", "json", "xml", "html").contains(lowerSuffix)) {
            return "document";
        } else if (Arrays.asList("zip", "rar", "7z", "tar", "gz").contains(lowerSuffix)) {
            return "archive";
        }

        return "other";
    }

    //加盐方法
    private String getEncryptPassword(String filePassword) {
        // 加盐混淆密码
        final String SALT = "oranges";
        return DigestUtils.md5DigestAsHex((SALT + filePassword).getBytes());
    }

}




