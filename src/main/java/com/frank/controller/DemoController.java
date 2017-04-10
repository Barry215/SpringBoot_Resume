package com.frank.controller;

import com.frank.dao.ArticleMapper;
import com.frank.dao.DocumentMapper;
import com.frank.dao.TagMapper;
import com.frank.dto.ArticleForm;
import com.frank.dto.ArticleInfo;
import com.frank.dto.JsonResult;
import com.frank.model.Article;
import com.frank.model.Document;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by frank on 17/3/9.
 */
@RestController
public class DemoController {

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private DocumentMapper documentMapper;

    @Resource
    private TagMapper tagMapper;

    @RequestMapping(value = "/p",method = RequestMethod.POST)
    public JsonResult<?> createArticle(@RequestBody ArticleForm articleForm) {
//增加tag
        Document document = new Document();
        int result_1 = documentMapper.insertSelective(document);
        if (result_1 == 1){
            Article article = new Article();
            article.setTitle(articleForm.getTitle());
            article.setContent(articleForm.getContent());
            article.setVersion(articleForm.getVersion());
            article.setHasPublished(articleForm.getHasPublished());
            article.setDocumentId(document.getId());

            int result_2 = articleMapper.insertSelective(article);
            if (result_2 == 1){
                if (articleForm.getHasPublished() == 1){
                    document.setShowId(article.getId());
                }else if (articleForm.getHasPublished() == 0){
                    document.setEditId(article.getId());
                }
                return new JsonResult<>(200,"success");
            }else {
                return new JsonResult<>(500,"server error");
            }
        }else {
            return new JsonResult<>(500,"server error");
        }
    }

    @RequestMapping(value = "/p/{document_id}/edit",method = RequestMethod.GET)
    public JsonResult<?> getEditDocument(@PathVariable int document_id) {
        Document document = documentMapper.selectByPrimaryKey(document_id);
        Article article = articleMapper.selectByPrimaryKey(document.getEditId());
        List<String> tags = tagMapper.selectTagsByArticle(article.getId());
        ArticleInfo articleInfo = new ArticleInfo(article,tags);
        return new JsonResult<>(200,"success",articleInfo);
    }

    @RequestMapping(value = "/p/u",method = RequestMethod.GET)
    public JsonResult<?> getDocumentsList() {
        return new JsonResult<>(200,"success",articleMapper.selectAllDocuments());
    }

    @RequestMapping(value = "/p/{article_id}",method = RequestMethod.GET)
    public JsonResult<?> getArticle(@PathVariable int article_id) {
        Article article = articleMapper.selectByPrimaryKey(article_id);
        List<String> tags = tagMapper.selectTagsByArticle(article_id);
        ArticleInfo articleInfo = new ArticleInfo(article,tags);
        return new JsonResult<>(200,"success",articleInfo);
    }

    @RequestMapping(value = "/p/h/{document_id}",method = RequestMethod.GET)
    public JsonResult<?> getHistoryArticle(@PathVariable int document_id) {
        List<Article> articleList= articleMapper.selectHistoryArticles(document_id);
        List<ArticleInfo> articleInfoList = new ArrayList<>();
        for (Article article : articleList){
            List<String> tags = tagMapper.selectTagsByArticle(article.getId());
            ArticleInfo articleInfo = new ArticleInfo(article,tags);
            articleInfoList.add(articleInfo);
        }
        return new JsonResult<>(200,"success",articleInfoList);
    }
}
