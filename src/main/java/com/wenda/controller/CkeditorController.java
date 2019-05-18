package com.wenda.controller;

import com.wenda.service.QiniuService;
import com.wenda.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/edit")
public class CkeditorController {
    private static final Logger logger = LoggerFactory.getLogger(CkeditorController.class);
    @Autowired
    QiniuService qiniuService;

    /**
     * 编辑器图片上传实现
     * @param file
     * @param CKEditorFuncNum
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("/uploadImage")
    //名字upload是固定的
    public String ckeditorUpload(@RequestParam("upload") MultipartFile file, String CKEditorFuncNum) throws Exception {
        try {
            //上传文件返回七牛云路径
            String fileUrl = qiniuService.saveImage(file);
            if (fileUrl == null) {
                return WendaUtil.getJSONString(1, "上传图片失败");
            }
            //Ckeditor 实现图片回显，固定代码，只需改路径即可
            StringBuffer sb = new StringBuffer();
            sb.append("<script type=\"text/javascript\">");
            sb.append("window.parent.CKEDITOR.tools.callFunction(" + CKEditorFuncNum + ",'" + fileUrl
                    + "','')");
            sb.append("</script>");
            return sb.toString();
        } catch (Exception e) {
            logger.error("上传图片失败" + e.getMessage());
            return WendaUtil.getJSONString(1, "上传失败");
        }
    }

}
