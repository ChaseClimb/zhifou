package com.wenda.service;

import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

//前缀树
@Service
public class SensitiveService implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveService.class);


    private TrieNode rootNode = new TrieNode();

    //初始化bean调用
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            //得到当前的classpath的绝对路径的URI表示法。
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("SensitiveWords.txt");
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String lineTxt;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                //初始化字典树
                addWord(lineTxt.trim());
            }
            reader.close();
            is.close();
        } catch (Exception e) {
            logger.error("读取敏感词文件失败" + e.getMessage());
        }
    }

    //abc，敏感词词典
    private void addWord(String lineTxt) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < lineTxt.length(); i++) {
            Character c = lineTxt.charAt(i);//先找到a

            if (isSymbol(c)) {
                continue;
            }
            TrieNode node = tempNode.getSubNode(c);

            //如果不存在就挂载
            if (node == null) {
                node = new TrieNode();
                tempNode.addSubNode(c, node);
            }

            //指向下一节点
            tempNode = node;

            //到达尾节点
            if (i == lineTxt.length() - 1) {
                tempNode.setKeywordEnd(true);//标记
            }
        }
    }

    //字典树
    private class TrieNode {
        //是不是敏感词的结尾
        private boolean end = false;

        //当前节点下所有的子节点 ab ac ad
        private Map<Character, TrieNode> subNodes = new HashMap<Character, TrieNode>();

        public void addSubNode(Character key, TrieNode node) {
            subNodes.put(key, node);
        }

        TrieNode getSubNode(Character key) {
            return subNodes.get(key);
        }

        boolean isKeyWordEnd() {
            return end;
        }

        void setKeywordEnd(boolean end) {
            this.end = end;
        }
    }

    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        String replacement = "***";
        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;
        StringBuilder result = new StringBuilder();
        while (position < text.length()) {
            char c = text.charAt(position);
            if (isSymbol(c)) {
                //保留开头空格
                if (tempNode == rootNode) {
                    result.append(c);
                    ++begin;
                }
                ++position;
                continue;
            }
            tempNode = tempNode.getSubNode(c);

            if (tempNode == null) {
                //对不上
                result.append(text.charAt(begin));
                position = begin + 1;
                begin = position;
                //回到开始
                tempNode = rootNode;
            } else if (tempNode.isKeyWordEnd()) {
                //发现敏感词
                result.append(replacement);
                position = position + 1;
                begin = position;
                tempNode = rootNode;
            } else {
                //下指针继续移动
                position++;
            }
        }
        return result.toString();
    }

    private boolean isSymbol(char c) {
        int ic = (int) c;
        //不是英文东亚文字 0x2e80-0x9fff
        return !CharUtils.isAsciiAlphanumeric(c) && (ic < 0x2E80 || ic > 0x9FFF);
    }

    public static void main(String[] args) {
        SensitiveService s = new SensitiveService();
        s.addWord("色情");
        s.addWord("赌博");
        System.out.println(s.filter("你好 赌博"));
    }
}
