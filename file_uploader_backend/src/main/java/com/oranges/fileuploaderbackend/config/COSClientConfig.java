package com.oranges.fileuploaderbackend.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;


import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云对象存储(COS)客户端配置类
 * 该类用于配置和初始化腾讯云COS客户端实例
 */
@Configuration  // 标识该类是一个配置类，用于替代XML配置文件
@ConfigurationProperties(prefix = "cos.client")  // 指定配置文件中前缀为cos.client的属性
@Data  // Lombok注解，自动生成getter、setter、toString等方法
public class COSClientConfig {
    private String host;      // COS服务的域名
    private String secretId;  // 腾讯云API密钥ID，用于身份验证
    private String secretKey; // 腾讯云API密钥Key，用于身份验证
    private String region;    // COS存储桶所在地域
    private String bucket;    // COS存储桶名称

    @Bean
    public COSClient cosClient() {
        //初始化用户身份信息
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        ClientConfig  clientConfig = new ClientConfig( new Region(region));
        // 生成cos客户端。
        return new COSClient(cred, clientConfig);
    }

}
