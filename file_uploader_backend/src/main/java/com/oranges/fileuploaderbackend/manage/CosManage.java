package com.oranges.fileuploaderbackend.manage;

import com.oranges.fileuploaderbackend.config.COSClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;


@Component
//COS管理
public class CosManage {
    @Resource
    private  COSClientConfig cosClientConfig;
    @Resource
    private  COSClient cosClient;
    //这里不做异常处理是直接在外面做，看他有没有返回，没有就直接抛
    //上传文件
    public  PutObjectResult upload(File file , String key){
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        return putObjectResult;
    }
    //下载文件
    public ObjectMetadata download(String key , String localPath){
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        File fileLocal = new File(localPath);
        ObjectMetadata object = cosClient.getObject(getObjectRequest,fileLocal);
        return object;
    }


    //删除文件
    public  void delete(String key){
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }
}
