package com.wenda.service;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.wenda.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class QiniuService {
    private static final Logger logger = LoggerFactory.getLogger(QiniuService.class);
    //图床域名
    private static String QINIU_IMAGE_DOMAIN = "http://zhifou.cjproject.top/";

    //构造一个带指定Zone对象的配置类
    Configuration cfg = new Configuration(Zone.zone2());
    //...其他参数参考类注释
    UploadManager uploadManager = new UploadManager(cfg);
    //...生成上传凭证，然后准备上传
    String accessKey = "cUNSQl83sYI9F3TzuKBPG8bFsEm68YWz5MAj4vJc";
    String secretKey = "09vRgoQNgp8u0gvbrmjbXxYhkCwbRYMLZwq8LWWg";
    String bucket = "zhifou";
    //默认不指定key的情况下，以文件内容的hash值作为文件名
    String fileName = null;
    Auth auth = Auth.create(accessKey, secretKey);
    String upToken = auth.uploadToken(bucket);

    public String saveImage(MultipartFile file) throws IOException {
        try {
            int dotPos = file.getOriginalFilename().lastIndexOf(".");
            if (dotPos < 0) {
                return null;
            }
            // 获取文件的后缀名
            String fileExt = file.getOriginalFilename().substring(dotPos + 1).toLowerCase();
            if (!WendaUtil.isFileAllowed(fileExt)) {
                return null;
            }
            fileName = UUID.randomUUID().toString().replace("-", "") + "." + fileExt;
            //调用方法上传
            Response response = uploadManager.put(file.getBytes(), fileName, upToken);

            //打印返回的信息
            System.out.println(response.toString());
            if (response.isOK() && response.isJson()) {
                //返回七牛云图片路径
                return QINIU_IMAGE_DOMAIN + JSONObject.parseObject(response.bodyString()).get("key");
            } else {
                logger.error("七牛异常:" + response.bodyString());
                return null;
            }
        } catch (QiniuException e) {
            // 请求失败时打印的异常的信息
            logger.error("七牛异常:" + e.getMessage());
            return null;
        }
    }
}
