package com.frank.controller;

import com.frank.dao.ArticleMapper;
import com.frank.dao.DocumentMapper;
import com.frank.dao.TagMapper;
import com.frank.dao.UserMapper;
import com.frank.dto.*;
import com.frank.model.Article;
import com.frank.model.Document;
import com.frank.model.Tag;
import com.frank.model.User;
import com.frank.service.QiniuUpService;
import com.frank.service.TokenService;
import com.frank.service.ValidateService;
import com.frank.shiro.MyShiroRealm;
import com.frank.vcode.Captcha;
import com.frank.vcode.GifCaptcha;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import io.swagger.annotations.*;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by frank on 17/4/19.
 */
@Api(value = "私有接口")
@RestController
@RequestMapping("/admin")
@EnableAutoConfiguration
public class PrivateController {

    /*
     * 登录利用注解获取请求头
     * @RequestHeader("Authorization") String authorization
     */

    private Logger log = Logger.getLogger(PrivateController.class);

    @Resource
    private ValidateService validateService;

    @Resource
    private DocumentMapper documentMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private QiniuUpService qiniuUpService;


    /**
     * 创建文章
     * hasPublished 0:创建文章但不发表 1:创建并发表文章
     */

    @Transactional
    @ApiOperation(notes = "创建文章", value = "创建文章")
    @ApiImplicitParam(paramType = "body", name = "articleForm", value = "hasPublished 0:创建文章但不发表 1:创建并发表文章", required = true, dataType = "ArticleForm")
    @RequestMapping(value = "/p/new",method = RequestMethod.POST)
    public JsonResult<?> createArticle(@RequestBody @Valid ArticleForm articleForm, BindingResult result) {

        if (result.hasErrors()){
            return validateService.validate(result);
        }

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
     * 修改文章
     * hasPublished 0:创建文章，保存但不发表 1:创建并发表文章
     */

    @Transactional
    @ApiOperation(notes = "修改文章", value = "修改文章")
    @ApiImplicitParam(paramType = "body", name = "articleModifyForm", value = "hasPublished 0:修改文章，保存但不发表 1:修改并发表新文章", required = true, dataType = "ArticleModifyForm")
    @RequestMapping(value = "/p/edit",method = RequestMethod.PUT)
    @CacheEvict(cacheNames = "show_articleInfo", key = "'id:'+#articleModifyForm.document_id", condition="#articleModifyForm.hasPublished == 1")
    public JsonResult<?> modifyArticle(@RequestBody @Valid ArticleModifyForm articleModifyForm, BindingResult bindingResult) {

        if (bindingResult.hasErrors()){
            return validateService.validate(bindingResult);
        }

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
     * 删除文章
     */

    @Transactional
    @ApiOperation(notes = "删除文章", value = "删除文章")
    @ApiImplicitParam(paramType = "path", name = "document_id", value = "文章ID", required = true, dataType = "Integer", defaultValue = "1")
    @RequestMapping(value = "/p/d/{document_id}",method = RequestMethod.DELETE)
    public JsonResult<?> deleteDocument(@PathVariable int document_id) {
        Document document = documentMapper.selectByPrimaryKey(document_id);
        if (document.getState() == 1){
            int result = documentMapper.deleteDocument(document_id);
            if (result == 1){
                return new JsonResult<>(200,"success");
            }else {
                return new JsonResult<>(500,"server error");
            }
        }else {
            return new JsonResult<>(404,"文章已经被删除");
        }

    }

    /**
     * 恢复被删除的文章
     */

    @Transactional
    @ApiOperation(notes = "恢复被删除的文章", value = "恢复被删除的文章")
    @ApiImplicitParam(paramType = "path", name = "document_id", value = "文章ID", required = true, dataType = "Integer", defaultValue = "1")
    @RequestMapping(value = "/p/r/{document_id}",method = RequestMethod.PUT)
    public JsonResult<?> recoverDocument(@PathVariable int document_id) {
        Document document = documentMapper.selectByPrimaryKey(document_id);
        if (document.getState() == 0){
            int result = documentMapper.recoverDocument(document_id);
            if (result == 1){
                return new JsonResult<>(200,"success");
            }else {
                return new JsonResult<>(500,"server error");
            }
        }else {
            return new JsonResult<>(404,"不存在此被删文章");
        }
    }

    /**
     * 获取此文章的所有历史版本
     */

    @ApiOperation(notes = "获取此文章的所有历史版本", value = "获取此文章的所有历史版本")
    @ApiImplicitParam(paramType = "path", name = "document_id", value = "文章ID", required = true, dataType = "Integer", defaultValue = "1")
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
     * 获取此文章的草稿内容(每篇文章都有草稿内容，如果你没修改过，那就是发表版的内容，如果你之前修改后保存过但未发表，那就是之前的修改内容)
     */

    @ApiOperation(notes = "获取此文章的草稿内容(每篇文章都有草稿内容，如果你没修改过，那就是发表版的内容，如果你之前修改后保存过但未发表，那就是之前的修改内容)", value = "获取此文章的草稿内容")
    @ApiImplicitParam(paramType = "path", name = "document_id", value = "文章ID", required = true, dataType = "Integer", defaultValue = "1")
    @RequestMapping(value = "/p/e/{document_id}",method = RequestMethod.GET)
    public JsonResult<?> getEditArticles(@PathVariable int document_id) {
        Document document = documentMapper.selectByPrimaryKey(document_id);
        Article article = articleMapper.selectByPrimaryKey(document.getEditId());
        List<String> tags = tagMapper.selectTagsByArticle(article.getId());
        ArticleInfo articleInfo = new ArticleInfo(article,tags,true);

        return new JsonResult<>(200,"success",articleInfo);
    }

    /**
     * 获取已保存但未发表的文章列表
     */

    @ApiOperation(notes = "获取已保存但未发表的文章列表，然后通过/p/e/{document_id}获取详细内容", value = "获取已保存但未发表的文章列表")
    @RequestMapping(value = "/p/saved/u",method = RequestMethod.GET)
    public JsonResult<?> getSavedArticles() {
        List<Article> articleList = articleMapper.selectSavedArticle();
        List<ArticleInfo> articleInfoList = new ArrayList<>();
        for (Article article : articleList){
            List<String> tags = tagMapper.selectTagsByArticle(article.getId());
            ArticleInfo articleInfo = new ArticleInfo(article,tags,false);
            articleInfoList.add(articleInfo);
        }
        return new JsonResult<>(200,"success",articleInfoList);
    }

    /**
     * 通过文章私密ID获取文章内容
     */

    @ApiOperation(notes = "通过文章私密ID获取文章内容", value = "通过文章私密ID获取文章内容")
    @ApiImplicitParam(paramType = "path", name = "article_id", value = "文章私密ID", required = true, dataType = "Integer", defaultValue = "1")
    @RequestMapping(value = "/a/{article_id}",method = RequestMethod.GET)
    public JsonResult<?> getArticleById(@PathVariable int article_id) {
        Article article = articleMapper.selectByPrimaryKey(article_id);
        List<String> tags = tagMapper.selectTagsByArticle(article.getId());
        ArticleInfo articleInfo = new ArticleInfo(article,tags,true);
        return new JsonResult<>(200,"success",articleInfo);
    }

    /**
     * 切换文章版本
     */

    @ApiOperation(notes = "切换文章版本", value = "切换文章版本")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "document_id", value = "文档ID", required = true, dataType = "Integer", defaultValue = "1"),
            @ApiImplicitParam(paramType = "path", name = "version", value = "版本号", required = true, dataType = "String", defaultValue = "v1.0")})
    @RequestMapping(value = "/p/{document_id}/check/{version:[a-zA-Z0-9\\.]+}",method = RequestMethod.PUT)
    @CacheEvict(cacheNames = "show_articleInfo", key = "'id:'+#document_id")
    public JsonResult<?> checkVersion(@PathVariable int document_id, @PathVariable String version) {
        Integer article_id = articleMapper.selectArticleByVersion(document_id,version);
        if (article_id == null){
            return new JsonResult<>(500,"invalid version");
        }
        int result = documentMapper.checkVersion(document_id,article_id);
        if (result == 1){
            return new JsonResult<>(200,"success");
        }else {
            return new JsonResult<>(500,"切换失败");
        }
    }


    /**
     * 修改个人信息
     */

    @ApiOperation(notes = "修改个人信息", value = "修改个人信息")
    @RequestMapping(value = "/user/modify",method = RequestMethod.PUT)
    public JsonResult<?> modifyUserInfo(@RequestBody UserInfo userInfo) {
        User user = new User();
        user.setName(userInfo.getName());
        user.setEmail(userInfo.getEmail());
        user.setDescription(userInfo.getDescription());
        user.setGithub(userInfo.getGithub());
        user.setPhone(userInfo.getPhone());
        user.setUserHead(userInfo.getUserHead());
        user.setWeibo(userInfo.getWeibo());
        int result = userMapper.updateByPrimaryKeySelective(user);
        if (result == 1){
            return new JsonResult<>(200,"success");
        }else {
            return new JsonResult<>(500,"sever error");
        }
    }

    /**
     * 获取七牛云上传token
     */

    @ApiOperation(notes = "获取七牛云上传token", value = "获取七牛云上传token")
    @RequestMapping(value = "/img/upToken",method = RequestMethod.GET)
    public JsonResult<?> getUpToken() {
        return new JsonResult<>(200,"success",qiniuUpService.getUpToken());
    }



}