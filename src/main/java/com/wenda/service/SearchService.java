package com.wenda.service;

import com.wenda.model.Question;
import com.wenda.model.ViewObject;
import com.wenda.util.JsoupUtil;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {
    private static final String SOLR_URL = "http://127.0.0.1:8983/solr/wenda";
    SolrClient client = new HttpSolrClient.Builder(SOLR_URL).build();
    private static final String QUESTION_TITLE_FIELD = "question_title";
    private static final String QUESTION_CONTENT_FIELD = "question_content";


    //建索引、查数据
    public ViewObject searchQuestion(String keyword, Integer pageNum, Integer pageSize) throws Exception {
        List<Question> questionList = new ArrayList<>();
        SolrQuery query = new SolrQuery(keyword);

        QueryResponse response = setCondition(query, pageNum, pageSize);

        SolrDocumentList results = response.getResults();
        // 数量，分页用
        Long total = results.getNumFound();
        Integer totalPage = (total.intValue() + pageSize - 1) / pageSize;

        if (total.intValue() == 0) {
            totalPage = 0;
        }

        if (pageNum > totalPage && totalPage > 0) {
            response = setCondition(query, totalPage, pageSize);
            pageNum = totalPage;
            total = results.getNumFound();
            totalPage = (total.intValue() + pageSize - 1) / pageSize;
        }


        /*
        形如
        "highlighting": {
            "574": {
                "question_title": [" Apple 说虽然 iPhone X 官网不卖了，但是并没有停产，其他渠道还可以买到；再回想这一年来，苹果和高通的<font>关系</font>，"],
                "question_content": [" Apple 说虽然 iPhone X 官网不卖了，但是并没有停产，其他渠道还可以买到；再回想这一年来，苹果和高通的<font>关系</font>，我很害怕新生产的 iPhone X 后来都采用了 Intel 的基带，如果真的是我猜想"]
            },
            "575": {
                "question_title": [" Apple 说虽然 iPhone X 官网不卖了，但是并没有停产，其他渠道还可以买到；再回想这一年来，苹果和高通的<font>关系</font>，"],
                "question_content": [" Apple 说虽然 iPhone X 官网不卖了，但是并没有停产，其他渠道还可以买到；再回想这一年来，苹果和高通的<font>关系</font>，我很害怕新生产的 iPhone X 后来都采用了 Intel 的基带，如果真的是我猜想"]
            },
        }*/
        for (Map.Entry<String, Map<String, List<String>>> entry : response.getHighlighting().entrySet()) {
            Question q = new Question();
            q.setId(Integer.parseInt(entry.getKey()));
            if (entry.getValue().containsKey(QUESTION_TITLE_FIELD)) {
                List<String> titleList = entry.getValue().get(QUESTION_TITLE_FIELD);
                if (titleList.size() > 0) {
                    q.setTitle(titleList.get(0));
                }
            }

            if (entry.getValue().containsKey(QUESTION_CONTENT_FIELD)) {
                List<String> contentList = entry.getValue().get(QUESTION_CONTENT_FIELD);
                if (contentList.size() > 0) {
                    String content = JsoupUtil.noneClean(contentList.get(0));
                    if (content.length() >= 104) {
                        content = content.substring(0, 104) + "...";
                    }
                    q.setContent(content);
                }
            }
            questionList.add(q);
        }
        ViewObject pageVo = new ViewObject();
        pageVo.set("totalPage", totalPage);
        pageVo.set("pageNumber", pageNum);

        ViewObject vos = new ViewObject();
        vos.set("questionList", questionList);
        vos.set("pageVo", pageVo);
        return vos;
    }

    private QueryResponse setCondition(SolrQuery query, Integer pageNum, Integer pageSize) throws Exception {
        // 分页
        query.setStart((pageNum - 1) * pageSize);
        query.setRows(pageSize);

        // 开启高亮
        query.setHighlight(true);

        // 设置前缀
        query.setHighlightSimplePre("<span style='color: #c20a0a;'>");

        // 设置后缀
        query.setHighlightSimplePost("</span>");

        //设置高亮的字段
        query.set("hl.fl", QUESTION_TITLE_FIELD + "," + QUESTION_CONTENT_FIELD);
        QueryResponse response = client.query(query);
        return response;
    }

    public boolean indexQuestion(int qid, String title, String content) throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("id", qid);
        doc.setField(QUESTION_TITLE_FIELD, title);
        doc.setField(QUESTION_CONTENT_FIELD, content);
        UpdateResponse response = client.add(doc, 1000);
        return response != null && response.getStatus() == 0;
    }


}
