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
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
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
//@Api(value = "示例接口")
@RestController
@RequestMapping("/")
@EnableAutoConfiguration
public class BlogController {

    /**
     * 在PUT请求里可以既有@RequestBody，也有@PathVariable
     */

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
    @ApiOperation(notes = "创建文章", value = "创建文章")
    @ApiImplicitParam(name = "articleForm", value = "示例实体", required = true, dataType = "ArticleForm")
    @RequestMapping(value = "/p/new",method = RequestMethod.POST)
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

    @ApiOperation(notes = "获取草稿列表", value = "获取草稿列表")
    @RequestMapping(value = "/p/edit/u",method = RequestMethod.GET)
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

    @ApiOperation(notes = "获取草稿文章", value = "获取草稿文章")
    @ApiImplicitParam(name = "document_id", value = "文章ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/p/edit/{document_id}",method = RequestMethod.GET)
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
    @ApiOperation(notes = "修改文章", value = "修改文章")
    @ApiImplicitParam(name = "articleModifyForm", value = "表单", required = true, dataType = "ArticleModifyForm")
    @RequestMapping(value = "/p/edit",method = RequestMethod.PUT)
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

    @ApiOperation(value = "获取文章列表",notes = "获取文章列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "offset", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = "每页数量", required = true, dataType = "Integer")})
    @RequestMapping(value = "/p/u/{offset}/{size}",method = RequestMethod.GET)
    public JsonResult<?> getDocumentsList(@PathVariable int offset,@PathVariable int size) {
        PageHelper.startPage(offset, size);
        List<Article> articleList = articleMapper.selectDocuments();
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

    @ApiOperation(notes = "获取文章总数", value = "获取文章总数")
    @RequestMapping(value = "/p/count",method = RequestMethod.GET)
    public JsonResult<?> getArticleCount() {
        return new JsonResult<>(200,"success",documentMapper.countDocument());
    }

    /**
     * 获取文章
     */

    @ApiOperation(notes = "获取文章", value = "获取文章")
    @ApiImplicitParam(name = "document_id", value = "文章ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/p/{document_id}",method = RequestMethod.GET)
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

    @ApiOperation(notes = "获取历史文章", value = "获取历史文章")
    @ApiImplicitParam(name = "document_id", value = "文章ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/p/h/{document_id}",method = RequestMethod.GET)
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

    @ApiOperation(notes = "获取归档文章", value = "获取归档文章")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "offset", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "size", value = "每页数量", required = true, dataType = "Integer")})
    @RequestMapping(value = "/p/archive/{offset}/{size}",method = RequestMethod.GET)
    public JsonResult<?> getArchiveArticles(@PathVariable int offset,@PathVariable int size) {
        PageHelper.startPage(offset, size);
        List<Article> articleList = articleMapper.selectArchiveDocuments();
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

    @ApiOperation(notes = "获取归档数", value = "获取归档数")
    @RequestMapping(value = "/p/archive/count",method = RequestMethod.GET)
    public JsonResult<?> getArchiveArticles() {

        return new JsonResult<>(200,"success",articleMapper.selectArchiveCount());
    }

    /**
     * 获取标签数
     */

    @ApiOperation(notes = "获取标签数", value = "获取标签数")
    @RequestMapping(value = "/p/tag/count",method = RequestMethod.GET)
    public JsonResult<?> getTagsCount() {
        return new JsonResult<>(200,"success",tagMapper.selectTagsCount());
    }

    /**
     * 切换文章版本
     */

    @ApiOperation(notes = "切换文章版本", value = "切换文章版本")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "document_id", value = "文档ID", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "version", value = "版本号", required = true, dataType = "String")})
    @RequestMapping(value = "/p/{document_id}/check/{version}",method = RequestMethod.PUT)
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
