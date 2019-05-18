package com.wenda.service;

import com.wenda.ZhiFouApplication;
import com.wenda.model.Comment;
import com.wenda.model.EntityType;
import com.wenda.model.Question;
import com.wenda.model.ViewObject;
import com.wenda.util.JsoupUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.util.*;


@Service
public class SearchService {

    @Autowired
    QuestionService questionService;

    @Autowired
    CommentService commentService;

    //问题core
    private static final String SOLR_URL = "http://127.0.0.1:8983/solr/wenda";
    private SolrClient questionClient = new HttpSolrClient.Builder(SOLR_URL).build();

    //评论core
    private static final String SOLR_URL1 = "http://127.0.0.1:8983/solr/comment";
    private SolrClient commentClient = new HttpSolrClient.Builder(SOLR_URL1).build();

    private static final String QUESTION_TITLE_FIELD = "question_title";
    private static final String QUESTION_CONTENT_FIELD = "question_content";
    private static final String COMMENT_CONTENT_FIELD = "comment_content";

    private static final String UNDEFIND_CONDITION = "-1";

    //建索引、查数据
    public ViewObject search(String keyword, String status, String entityId, Integer entityType, Integer pageNum, Integer pageSize) throws Exception {

        QueryResponse response = setCondition(keyword, status, entityId, entityType, pageNum, pageSize);

        SolrDocumentList results = response.getResults();
        // 数量，分页用
        Long total = results.getNumFound();
        //总页数
        Integer totalPage = (total.intValue() + pageSize - 1) / pageSize;

        if (total.intValue() == 0) {
            totalPage = 0;
        }

        if (pageNum > totalPage && totalPage > 0) {
            response = setCondition(keyword, status, entityId, entityType, totalPage, pageSize);
            pageNum = totalPage;
            total = results.getNumFound();
            totalPage = (total.intValue() + pageSize - 1) / pageSize;
        }


        /*
        形如
        "highlighting": {
             String,Map
            "574": {
                "question_title": [" Apple 说虽然 iPhone X 官网不卖了，但是并没有停产，其他渠道还可以买到；再回想这一年来，苹果和高通的<font>关系</font>，"],
                "question_content": [" Apple 说虽然 iPhone X 官网不卖了，但是并没有停产，其他渠道还可以买到；再回想这一年来，苹果和高通的<font>关系</font>，我很害怕新生产的 iPhone X 后来都采用了 Intel 的基带，如果真的是我猜想"]
            },
            "575": {
                "question_title": [" Apple 说虽然 iPhone X 官网不卖了，但是并没有停产，其他渠道还可以买到；再回想这一年来，苹果和高通的<font>关系</font>，"],
                "question_content": [" Apple 说虽然 iPhone X 官网不卖了，但是并没有停产，其他渠道还可以买到；再回想这一年来，苹果和高通的<font>关系</font>，我很害怕新生产的 iPhone X 后来都采用了 Intel 的基带，如果真的是我猜想"]
            },
        }*/
        if (results.isEmpty()) {
            return null;
        }



        ViewObject pageVo = new ViewObject();
        pageVo.set("totalPage", totalPage);
        pageVo.set("pageNumber", pageNum);

        ViewObject vos = new ViewObject();
        if (entityType == EntityType.ENTITY_QUESTION) {
            List<Question> questionList = new ArrayList<>();
            questionList = getQuestionList(keyword, results, response);
            vos.set("questionList", questionList);
        } else if (entityType == EntityType.ENTITY_COMMENT) {
            List<Comment> commentList = new ArrayList<>();
            commentList = getCommentList(keyword, results, response);
            vos.set("commentList", commentList);
        }

        vos.set("pageVo", pageVo);
        return vos;
    }

    private QueryResponse setCondition(String keyword, String status, String entityId, Integer entityType, Integer pageNum, Integer pageSize) throws Exception {
        SolrQuery query = new SolrQuery();
        QueryResponse response = null;

        // 分页
        query.setStart((pageNum - 1) * pageSize);
        query.setRows(pageSize);

        if (StringUtils.isBlank(keyword) || UNDEFIND_CONDITION.equals(keyword)) {
            query.setQuery("*:*");
            //增加排序会影响相关性判断
            query.setSort("updateDate", SolrQuery.ORDER.desc);
        } else {
            query.setQuery(keyword);
        }

        // 开启高亮
        query.setHighlight(true);

        // 设置前缀
        query.setHighlightSimplePre("<span style='color: #c20a0a;'>");

        // 设置后缀
        query.setHighlightSimplePost("</span>");


        List<String> strs = new ArrayList<>();

        //查询问题
        if (entityType == EntityType.ENTITY_QUESTION) {
            //指定域查询
            query.set("df", "question_keywords");
            //设置高亮的字段
            query.set("hl.fl", QUESTION_TITLE_FIELD + "," + QUESTION_CONTENT_FIELD);
            //设置过滤条件，当条件存在才设置
            if (!StringUtils.isBlank(status) && !UNDEFIND_CONDITION.equals(status)) {
                strs.add("question_status:" + status);
            }
            if (!StringUtils.isBlank(entityId) && !UNDEFIND_CONDITION.equals(entityId)) {
                strs.add("id:" + entityId);
            }
            if (!strs.isEmpty()) {
                query.setFilterQueries(strs.toArray(new String[strs.size()]));
            }
            response = questionClient.query(query);
        }


        //查询评论
        else if (entityType == EntityType.ENTITY_COMMENT) {
            //指定域查询
            query.set("df", "comment_keywords");
            //设置高亮的字段
            query.set("hl.fl", COMMENT_CONTENT_FIELD);
            //设置过滤条件，当条件存在才设置
            if (!StringUtils.isBlank(status) && !UNDEFIND_CONDITION.equals(status)) {
                strs.add("comment_status:" + status);
            }
            if (!StringUtils.isBlank(entityId) && !UNDEFIND_CONDITION.equals(entityId)) {
                strs.add("id:" + entityId);
            }
            if (!strs.isEmpty()) {
                query.setFilterQueries(strs.toArray(new String[strs.size()]));
            }
            response = commentClient.query(query);
        }
        return response;
    }

    public <T> void saveOrUpdateIndex(T bean, Integer entityType) throws SolrServerException, IOException {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        SolrInputDocument doc = binder.toSolrInputDocument(bean);
        if (entityType == EntityType.ENTITY_QUESTION) {
            questionClient.add(doc);
            questionClient.commit();
        } else if (entityType == EntityType.ENTITY_COMMENT) {
            commentClient.add(doc);
            commentClient.commit();
        }
    }


    public <T> void batchDeleteIndex(List<T> beans, Integer entityType) throws SolrServerException, IOException {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        if (entityType == EntityType.ENTITY_QUESTION) {
            for (T t : beans) {
                SolrInputDocument doc = binder.toSolrInputDocument(t);
                questionClient.add(doc);
            }
            questionClient.commit();
        } else if (entityType == EntityType.ENTITY_COMMENT) {
            for (T t : beans) {
                SolrInputDocument doc = binder.toSolrInputDocument(t);
                commentClient.add(doc);
            }
            commentClient.commit();
        }
    }

    private List<Question> getQuestionList(String keyword, List<SolrDocument> results, QueryResponse response) {
        List<Question> questionList = new ArrayList<>();
        //没有关键字
        if (StringUtils.isBlank(keyword) || UNDEFIND_CONDITION.equals(keyword)) {
            for (SolrDocument solrDocument : results) {
                Question q = new Question();
                q.setId(Integer.parseInt(solrDocument.get("id").toString()));
                q.setTitle(String.valueOf(solrDocument.get("question_title")));
                q.setContent(String.valueOf(solrDocument.get("question_content")));
                q.setStatus(Integer.parseInt(solrDocument.get("question_status").toString()));
                questionList.add(q);
            }
        } else {
            for (Map.Entry<String, Map<String, List<String>>> entry : response.getHighlighting().entrySet()) {
                int id = Integer.parseInt(entry.getKey());

                Question q = new Question();
                q.setId(id);

                Question question = questionService.getQuestionsByIdWithoutStatus(id);

                if (entry.getValue().containsKey(QUESTION_TITLE_FIELD) && entry.getValue().containsKey(QUESTION_CONTENT_FIELD)) {
                    List<String> titleList = entry.getValue().get(QUESTION_TITLE_FIELD);
                    q.setTitle(titleList.get(0));

                    List<String> contentList = entry.getValue().get(QUESTION_CONTENT_FIELD);
                    q.setContent(contentList.get(0));


                } else if (entry.getValue().containsKey(QUESTION_TITLE_FIELD)) {
                    List<String> titleList = entry.getValue().get(QUESTION_TITLE_FIELD);
                    q.setTitle(titleList.get(0));

                    q.setContent(JsoupUtil.noneClean(question.getContent()));
                    q.setUserId(question.getUserId());
                } else if (entry.getValue().containsKey(QUESTION_CONTENT_FIELD)) {
                    List<String> contentList = entry.getValue().get(QUESTION_CONTENT_FIELD);
                    q.setContent(contentList.get(0));

                    q.setTitle(question.getTitle());
                    q.setUserId(question.getUserId());
                }
                q.setStatus(question.getStatus());
                questionList.add(q);
            }
        }
        return questionList;
    }


    private List<Comment> getCommentList(String keyword, List<SolrDocument> results, QueryResponse response) {
        List<Comment> commentList = new ArrayList<>();
        //没有关键字
        if (StringUtils.isBlank(keyword) || UNDEFIND_CONDITION.equals(keyword)) {
            for (SolrDocument solrDocument : results) {
                Comment comment = new Comment();
                comment.setId(Integer.parseInt(solrDocument.get("id").toString()));
                comment.setContent(String.valueOf(solrDocument.get("comment_content")));
                comment.setStatus(Integer.parseInt(solrDocument.get("comment_status").toString()));
                comment.setEntityId(Integer.parseInt(solrDocument.get("entityId").toString()));
                commentList.add(comment);
            }
        } else {
            for (Map.Entry<String, Map<String, List<String>>> entry : response.getHighlighting().entrySet()) {
                int id = Integer.parseInt(entry.getKey());
                Comment c = new Comment();
                c.setId(id);

                Comment comment = commentService.getCommentByIdWithoutStatus(id);

                if (entry.getValue().containsKey(COMMENT_CONTENT_FIELD)) {
                    List<String> contentList = entry.getValue().get(COMMENT_CONTENT_FIELD);
                    c.setContent(contentList.get(0));
                }
                c.setStatus(comment.getStatus());
                c.setEntityId(comment.getEntityId());
                commentList.add(c);
            }
        }
        return commentList;
    }
}
