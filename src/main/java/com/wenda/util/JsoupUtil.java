package com.wenda.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

/**
 * 描述: 过滤 HTML 标签中 XSS 代码
 */
public class JsoupUtil {
    /**
     * 使用自带的 basicWithImages 白名单
     * 允许的便签有 a,b,blockquote,br,cite,code,dd,dl,dt,em,i,li,ol,p,pre,q,small,span,strike,strong,sub,sup,u,ul,img
     * 以及 a 标签的 href,img 标签的 src,align,alt,height,width,title 属性
     * a,b,blockquote,br,caption,cite,code,col,colgroup,dd,div,dl,dt,em,h1,h2,h3,h4,h5,h6,i,img,li,ol,p,pre,q,small,span,strike,strong,sub,sup,table,tbody,td,tfoot,th,thead,tr,u,ul
     */
    private static final Whitelist whitelist = Whitelist.relaxed();
    //只保留文本
    private static final Whitelist nonelist = Whitelist.none();

    /**
     * 配置过滤化参数, 不对代码进行格式化
     */
    private static final Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);

    static {
        /*富文本编辑时一些样式是使用 style 来进行实现的
        比如红色字体 style="color:red;"
        所以需要给所有标签添加 style 属性
        :all 表明给白名单中的所有标签添加style属性*/
        whitelist.addAttributes(":all", "href","style");
        //保留元素的URL属性中的相对链接,为false时将会把baseUri和元素的URL属性拼接起来
        whitelist.preserveRelativeLinks(true);

    }

    public static String clean(String content) {
        return Jsoup.clean(content, "http://baseUri", whitelist, outputSettings);
    }

    public static String noneClean(String content) {
        return Jsoup.clean(content, "", nonelist, outputSettings);
    }

    public static void main(String[] args) {
        System.out.println(clean("<a href='/user/1'>zhangsan</a>"));
    }
}
