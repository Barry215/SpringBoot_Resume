package com.frank.controller;

import com.frank.dao.ArticleMapper;
import com.frank.dao.DocumentMapper;
import com.frank.dao.TagMapper;
import com.frank.dto.ArticleForm;
import com.frank.dto.ArticleInfo;
import com.frank.dto.ArticleModifyForm;
import com.frank.dto.JsonResult;
import com.frank.model.Article;
import com.frank.model.Document;
import com.frank.model.Tag;
import com.frank.service.BlogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Created by frank on 17/3/9.
 */
@Api(value = "示例接口")
@RestController
@RequestMapping("/")
@EnableAutoConfiguration
public class BlogController {

    private Logger log = Logger.getLogger(BlogController.class);

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private DocumentMapper documentMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private BlogService blogService;

    /**
     * 创建文章
     * hasPublished
     * 0 创建草稿文章
     * 1 创建并发布文章
     */

    @Transactional
    @RequestMapping(value = "/p",method = RequestMethod.POST)
    @ApiOperation(notes = "创建文章", value = "创建文章", httpMethod = "POST")
    @ApiImplicitParam(name = "articleForm", value = "示例实体", required = true, dataType = "ArticleForm")
    public JsonResult<?> createArticle(@RequestBody ArticleForm articleForm) {

        Document document = new Document();
        document.setState(1);
        int result_1 = documentMapper.insertSelective(document);
        if (result_1 == 1){
            Article article = new Article();
            article.setTitle(articleForm.getTitle());
            article.setContent(articleForm.getContent());
            article.setVersion(articleForm.getVersion());
            article.setHasPublished(articleForm.getHasPublished());
            article.setDocumentId(document.getId());
            article.setArchive(articleForm.getArchive());

            int result_2 = articleMapper.insertSelective(article);
            if (result_2 == 1){
                if (articleForm.getHasPublished() == 1){
                    document.setShowId(article.getId());
                    document.setEditId(article.getId());
                }else if (articleForm.getHasPublished() == 0){
                    document.setEditId(article.getId());
                }
                documentMapper.updateByPrimaryKeySelective(document);
                for (String str : articleForm.getTags()){
                    tagMapper.insertSelective(new Tag(str,article.getId()));
                }
                return new JsonResult<>(200,"success");
            }else {
                return new JsonResult<>(500,"server error");
            }
        }else {
            return new JsonResult<>(500,"server error");
        }
    }

    /**
     * 获取草稿列表
     */

    @RequestMapping(value = "/p/edit/u",method = RequestMethod.GET)
    @ApiOperation(notes = "获取草稿列表", value = "获取草稿列表", httpMethod = "GET")
    @ApiImplicitParam(name = "", value = "null", required = false, dataType = "null")
    public JsonResult<?> getEditArticles() {
        List<Article> articleList = articleMapper.selectEditArticle();
        List<ArticleInfo> articleInfoList = new ArrayList<>();
        for (Article article : articleList){
            List<String> tags = tagMapper.selectTagsByArticle(article.getId());
            ArticleInfo articleInfo = new ArticleInfo(article,tags,false);
            articleInfoList.add(articleInfo);
        }
        return new JsonResult<>(200,"success",articleInfoList);
    }

    /**
     * 获取草稿文章
     */

    @RequestMapping(value = "/p/edit/{document_id}",method = RequestMethod.GET)
    @ApiOperation(notes = "获取草稿文章", value = "获取草稿文章", httpMethod = "GET")
    @ApiImplicitParam(name = "document_id", value = "文章ID", required = true, dataType = "Integer")
    public JsonResult<?> getEditArticles(@PathVariable int document_id) {
        Document document = documentMapper.selectByPrimaryKey(document_id);
        Article article = articleMapper.selectByPrimaryKey(document.getEditId());
        List<String> tags = tagMapper.selectTagsByArticle(article.getId());
        ArticleInfo articleInfo = new ArticleInfo(article,tags,true);

        return new JsonResult<>(200,"success",articleInfo);
    }

    /**
     * 修改文章
     * hasPublished
     * 0 继续保存草稿文章
     * 1 发布文章
     */

    @Transactional
    @RequestMapping(value = "/p/edit",method = RequestMethod.PUT)
    @ApiOperation(notes = "修改文章", value = "修改文章", httpMethod = "PUT")
    @ApiImplicitParam(name = "articleModifyForm", value = "表单", required = true, dataType = "ArticleModifyForm")
    @CacheEvict(cacheNames = "show_articleInfo", key = "'id:'+#articleModifyForm.document_id", condition="#articleModifyForm.hasPublished == 1")
    public JsonResult<?> modifyArticle(@RequestBody ArticleModifyForm articleModifyForm) {
        Document document = documentMapper.selectByPrimaryKey(articleModifyForm.getDocument_id());
        int result;
        Article article;
        if (document.getShowId() != null && Objects.equals(document.getShowId(), document.getEditId())){
            article = new Article();
            article.setTitle(articleModifyForm.getTitle());
            article.setContent(articleModifyForm.getContent());
            article.setVersion(articleModifyForm.getVersion());
            article.setHasPublished(articleModifyForm.getHasPublished());
            article.setArchive(articleModifyForm.getArchive());
            article.setDocumentId(articleModifyForm.getDocument_id());

            result = articleMapper.insertSelective(article);
            if (result == 1){
                if (articleModifyForm.getHasPublished() == 1){
                    System.out.println("修改文章的id："+article.getId());

                    for (String str : articleModifyForm.getTags()){
                        tagMapper.insertSelective(new Tag(str,article.getId()));
                    }

                    document.setShowId(article.getId());
                    document.setEditId(article.getId());
                }else {
                    document.setEditId(article.getId());
                }
                documentMapper.updateByPrimaryKeySelective(document);
                return new JsonResult<>(200,"success");
            }
        }else {
            article = articleMapper.selectByPrimaryKey(document.getEditId());
            article.setTitle(articleModifyForm.getTitle());
            article.setContent(articleModifyForm.getContent());
            article.setVersion(articleModifyForm.getVersion());
            article.setHasPublished(articleModifyForm.getHasPublished());
            article.setArchive(articleModifyForm.getArchive());
            tagMapper.deleteTagsByArticle(document.getEditId());
            for (String str : articleModifyForm.getTags()){
                tagMapper.insertSelective(new Tag(str,article.getId()));
            }
            result = articleMapper.updateByPrimaryKeySelective(article);
            if (result == 1){
                if (articleModifyForm.getHasPublished() == 1){
                    System.out.println("修改文章的id："+article.getId());

                    document.setShowId(article.getId());
                    document.setEditId(article.getId());
                }else {
                    document.setEditId(article.getId());
                }
                documentMapper.updateByPrimaryKeySelective(document);
                return new JsonResult<>(200,"success");
            }
        }

        return new JsonResult<>(500,"server error");
    }

    /**
     * 获取文章列表
     */

    @RequestMapping(value = "/p/u/{offset}",method = RequestMethod.GET)
    @ApiOperation(notes = "获取文章列表", value = "获取文章列表", httpMethod = "GET")
    @ApiImplicitParam(name = "offset", value = "页码", required = true, dataType = "Integer")
    public JsonResult<?> getDocumentsList(@PathVariable int offset) {
        List<Article> articleList = articleMapper.selectDocuments((offset-1)*8);
        List<ArticleInfo> articleInfoList = new ArrayList<>();
        for (Article article : articleList){
            List<String> tags = tagMapper.selectTagsByArticle(article.getId());
            ArticleInfo articleInfo = new ArticleInfo(article,tags,false);
            articleInfoList.add(articleInfo);
        }
        //分月份
        return new JsonResult<>(200,"success",articleInfoList);
    }

    /**
     * 获取文章总数
     */

    @RequestMapping(value = "/p/count",method = RequestMethod.GET)
    @ApiOperation(notes = "获取文章总数", value = "获取文章总数", httpMethod = "GET")
    @ApiImplicitParam(name = "", value = "null", required = false, dataType = "null")
    public JsonResult<?> getArticleCount() {
        return new JsonResult<>(200,"success",documentMapper.countDocument());
    }

    /**
     * 获取文章
     */

    @RequestMapping(value = "/p/{document_id}",method = RequestMethod.GET)
    @ApiOperation(notes = "获取文章", value = "获取文章", httpMethod = "GET")
    @ApiImplicitParam(name = "document_id", value = "文章ID", required = true, dataType = "Integer")
    public JsonResult<?> getArticle(@PathVariable int document_id) {

        ArticleInfo articleInfo = blogService.getArticleInfo(document_id);
        if (articleInfo != null){
            return new JsonResult<>(200,"success",articleInfo);
        }

        return new JsonResult<>(404,"The article does not exist or it's on editing");
    }

    /**
     * 获取历史文章
     */

    @RequestMapping(value = "/p/h/{document_id}",method = RequestMethod.GET)
    @ApiOperation(notes = "获取历史文章", value = "获取历史文章", httpMethod = "GET")
    @ApiImplicitParam(name = "document_id", value = "文章ID", required = true, dataType = "Integer")
    public JsonResult<?> getHistoryArticles(@PathVariable int document_id) {
        List<Article> articleList= articleMapper.selectHistoryArticles(document_id);
        List<ArticleInfo> articleInfoList = new ArrayList<>();
        for (Article article : articleList){
            List<String> tags = tagMapper.selectTagsByArticle(article.getId());
            ArticleInfo articleInfo = new ArticleInfo(article,tags,false);
            articleInfoList.add(articleInfo);
        }
        return new JsonResult<>(200,"success",articleInfoList);
    }

    /**
     * 获取归档文章
     */

    @RequestMapping(value = "/p/archive/{offset}",method = RequestMethod.GET)
    @ApiOperation(notes = "获取归档文章", value = "获取归档文章", httpMethod = "GET")
    @ApiImplicitParam(name = "offset", value = "页码", required = true, dataType = "Integer")
    public JsonResult<?> getArchiveArticles(@PathVariable int offset) {
        List<Article> articleList = articleMapper.selectArchiveDocuments((offset-1)*8);
        List<ArticleInfo> articleInfoList = new ArrayList<>();
        for (Article article : articleList){
            List<String> tags = tagMapper.selectTagsByArticle(article.getId());
            ArticleInfo articleInfo = new ArticleInfo(article,tags,false);
            articleInfoList.add(articleInfo);
        }
        return new JsonResult<>(200,"success",articleInfoList);
    }

    /**
     * 获取归档数
     */

    @RequestMapping(value = "/p/archive/count",method = RequestMethod.GET)
    @ApiOperation(notes = "获取归档数", value = "获取归档数", httpMethod = "GET")
    @ApiImplicitParam(name = "", value = "null", required = true, dataType = "null")
    public JsonResult<?> getArchiveArticles() {

        return new JsonResult<>(200,"success",articleMapper.selectArchiveCount());
    }

    /**
     * 获取标签数
     */

    @RequestMapping(value = "/p/tag/count",method = RequestMethod.GET)
    @ApiOperation(notes = "获取标签数", value = "获取标签数", httpMethod = "GET")
    @ApiImplicitParam(name = "", value = "null", required = true, dataType = "null")
    public JsonResult<?> getTagsCount() {

        return new JsonResult<>(200,"success",tagMapper.selectTagsCount());
    }

    /**
     * 切换文章版本
     */

    @RequestMapping(value = "/p/{document_id}/check/{version}",method = RequestMethod.PUT)
    @ApiOperation(notes = "切换文章版本", value = "切换文章版本", httpMethod = "PUT")
    @ApiImplicitParam(name = "document_id&version", value = "文档ID&版本号", required = true, dataType = "Integer")
    @CacheEvict(cacheNames = "show_articleInfo", key = "'id:'+#document_id")
    public JsonResult<?> checkVersion(@PathVariable int document_id, @PathVariable String version) {
        System.out.println("版本号："+version);
        Integer article_id = articleMapper.selectArticleByVersion(document_id,version);
        if (article_id == null){
            return new JsonResult<>(500,"invalid version");
        }
        int result = documentMapper.checkVersion(document_id,article_id);
        if (result == 1){
            return new JsonResult<>(200,"success");
        }else {
            return new JsonResult<>(500,"check fail");
        }
    }
}
