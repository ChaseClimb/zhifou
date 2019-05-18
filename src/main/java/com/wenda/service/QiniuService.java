package com.wenda.service;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.processing.OperationManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.qiniu.util.UrlSafeBase64;
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
                //qiNiuMediaPrtScreen(fileName);
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

    /**
     * 七牛的视频截图
     *
     * @param fileName 要截图文件名称
     * @param format   截图类型
     * @return
     */
    public String qiNiuMediaPrtScreen(String fileName, String format) {
        String persistid = null;
        //新建一个OperationManager对象
        OperationManager operater = new OperationManager(auth, cfg);
        // 设置要截图的空间和key，并且这个key在你空间中存在(key就是文件的名字)
        String key = fileName;
        // 设置截图操作参数
        /*
            <Format>	是	输出的目标截图格式，支持jpg、png等。
            /offset/<Second>	是	指定截取视频的时刻，单位：秒，精确到毫秒。
            /w/<Width>		缩略图宽度，单位：像素（px），取值范围为1-3840。
            /h/<Height>		缩略图高度，单位：像素（px），取值范围为1-2160。
            /rotate/<Degree>		指定顺时针旋转的度数，可取值为90、180、270、auto，默认为不旋转
         */
        String fops = "vframe/" + format + "/offset/1/w/480/h/360/rotate/auto";

        //设置截图的队列
        String pipeline = bucket;
        //可以对转码后的文件进行使用saveas参数自定义命名，当然也可以不指定文件会默认命名并保存在当前空间。
        String pictureName = UUID.randomUUID().toString().replace("-", "") + "." + format;
        String urlbase64 = UrlSafeBase64.encodeToString(pictureName);
        String pfops = fops + "|saveas/" + urlbase64;
        //设置pipeline参数
        StringMap params = new StringMap().putWhen("force", 1, true).putNotEmpty("pipeline", pipeline);
        try {
            persistid = operater.pfop(bucket, key, pfops, params);
            //打印返回的persistid
            System.out.println(persistid);
        } catch (QiniuException e) {
            Response r = e.response;// 捕获异常信息
            logger.info(r.toString());// 请求失败时简单状态信息
            try {
                logger.info(r.bodyString());// 响应的文本信息
            } catch (QiniuException e1) {
                logger.error(e1.getMessage());
            }
        }
        return persistid;
    }
}
