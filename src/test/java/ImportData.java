import com.wenda.ZhiFouApplication;
import com.wenda.dao.CommentDao;
import com.wenda.dao.QuestionDao;
import com.wenda.model.Comment;
import com.wenda.model.Question;
import com.wenda.service.CommentService;
import com.wenda.service.QuestionService;
import com.wenda.util.JsoupUtil;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ZhiFouApplication.class)
public class ImportData {
    @Autowired
    QuestionDao questionDao;

    @Autowired
    CommentDao commentDao;

    //问题core
    private static final String SOLR_URL = "http://127.0.0.1:8983/solr/wenda";
    private SolrClient questionClient = new HttpSolrClient.Builder(SOLR_URL).build();

    //评论core
    private static final String SOLR_URL1 = "http://127.0.0.1:8983/solr/comment";
    private SolrClient commentClient = new HttpSolrClient.Builder(SOLR_URL1).build();


    @Test
    public <T> void importQuestionData() throws SolrServerException, IOException {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<Question> questions = questionDao.solrQuestions();
        List<Question> cleanQuestions = new ArrayList<>();
        for (Question q : questions) {
            //只保留文字
            q.setTitle(JsoupUtil.noneClean(q.getTitle()));
            q.setContent(JsoupUtil.noneClean(q.getContent()));
            cleanQuestions.add(q);
        }
        for (Question q : cleanQuestions) {
            SolrInputDocument doc = binder.toSolrInputDocument(q);
            questionClient.add(doc);
        }
        questionClient.commit();
    }


    @Test
    public <T> void importCommentData() throws SolrServerException, IOException {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        List<Comment> comments = commentDao.solrComment();
       /* for (Comment c : comments) {
            //只保留文字
            c.setContent(JsoupUtil.noneClean(c.getContent()));
            cleanComments.add(c);
        }*/
        for (Comment c : comments) {
            SolrInputDocument doc = binder.toSolrInputDocument(c);
            commentClient.add(doc);
        }
        commentClient.commit();
    }


    @Test
    public void query() throws IOException, SolrServerException {
        SolrQuery query = new SolrQuery("*:*");
        query.setStart(0);
        query.setRows(10);
        QueryResponse rsp = questionClient.query(query);
        SolrDocumentList documents = rsp.getResults();
        for (SolrDocument solrDocument : documents) {
            System.out.println("title:" + solrDocument.get("question_title") + "\t");
        }
    }
}
