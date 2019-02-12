import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

public class Clean {
    private static final Whitelist whitelist = Whitelist.none();
    /**
     * 配置过滤化参数, 不对代码进行格式化
     */
    private static final Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);


    public static void main(String[] args) {
            String str = "<div class='markdown_body'><p>我的工作环境这边只能用 SSH 连接 linux 服务器（内网）。没有 IPMI，没有物理机接触，如何重装 linux 系统呢？</p>" + "</div>";
        System.out.println(Jsoup.clean(str, "", whitelist, outputSettings));
    }
}
