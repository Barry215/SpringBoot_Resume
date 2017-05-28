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
import com.frank.service.BlogService;
import com.frank.service.TokenService;
import com.frank.service.ValidateService;
import com.frank.shiro.MyShiroRealm;
import com.frank.vcode.Captcha;
import com.frank.vcode.GifCaptcha;
import com.github.pagehelper.PageHelper;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import io.swagger.annotations.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * Created by frank on 17/3/9.
 */
@Api(value = "公开接口")
@RestController
@RequestMapping("/")
@EnableAutoConfiguration
public class PublicController {

    /**
     * 在PUT请求里可以既有@RequestBody，也有@PathVariable
     */

    private Logger log = Logger.getLogger(PublicController.class);

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private DocumentMapper documentMapper;

    @Resource
    private TagMapper tagMapper;

    @Resource
    private BlogService blogService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private MyShiroRealm myShiroRealm;

    @Resource
    private Producer captchaProducer;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private TokenService tokenService;

    /**
     * 获取文章
     */

    @ApiOperation(notes = "获取文章", value = "获取文章")
    @ApiImplicitParam(paramType = "path", name = "document_id", value = "文章ID", required = true, dataType = "Integer")
    @RequestMapping(value = "/p/{document_id}",method = RequestMethod.GET)
    public JsonResult<?> getArticle(@PathVariable int document_id) {
        ArticleInfo articleInfo = blogService.getArticleInfo(document_id);
        if (articleInfo != null){
            return new JsonResult<>(200,"success",articleInfo);
        }

        return new JsonResult<>(404,"文章不存在或正在编辑中");
    }

    /**
     * 获取文章列表
     */

    @ApiOperation(value = "获取文章列表",notes = "获取文章列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "offset", value = "页码(偏移量)", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "size", value = "每页数量", required = true, dataType = "Integer")})
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
        return new JsonResult<>(200,"success",articleInfoList);
    }


    /**
     * 获取归档文章列表
     */

    @ApiOperation(value = "获取归档文章列表",notes = "获取归档文章列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "offset", value = "页码(偏移量)", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "size", value = "每页数量", required = true, dataType = "Integer")})
    @RequestMapping(value = "/p/archive/{offset}/{size}",method = RequestMethod.GET)
    public JsonResult<?> getArticleWithTimeList(@PathVariable int offset,@PathVariable int size) {
        PageHelper.startPage(offset, size);
        List<Article> articleList = articleMapper.selectDocuments();
        if (articleList.size() == 0){
            return new JsonResult<>(200,"没有文章");
        }
        List<ArticleInfo> articleInfoList = new ArrayList<>();
        for (Article article : articleList){
            List<String> tags = tagMapper.selectTagsByArticle(article.getId());
            ArticleInfo articleInfo = new ArticleInfo(article,tags,false);
            articleInfoList.add(articleInfo);
        }
        List<ArticleWithTime> articleWithTimeList = new ArrayList<>();
        ArticleWithTime articleWithTime = new ArticleWithTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月");
        String time = simpleDateFormat.format(articleInfoList.get(0).getCreate_time());
        articleWithTime.setTime(time);
        for (ArticleInfo articleInfo : articleInfoList){
            String format_time = simpleDateFormat.format(articleInfo.getCreate_time());
            if (format_time.equals(time)){
                articleWithTime.getArticleInfoList().add(articleInfo);
            }else {
                articleWithTimeList.add(articleWithTime);
                time = format_time;
                articleWithTime = new ArticleWithTime();
                articleWithTime.setTime(format_time);
                articleWithTime.getArticleInfoList().add(articleInfo);
            }
        }
        articleWithTimeList.add(articleWithTime);
        return new JsonResult<>(200,"success",articleWithTimeList);
    }

    /**
     * 获取所有标签
     */

    @ApiOperation(notes = "获取所有标签", value = "获取所有标签")
    @RequestMapping(value = "/p/tag/u",method = RequestMethod.GET)
    public JsonResult<?> getTagList() {
        return new JsonResult<>(200,"success",tagMapper.selectTagList());
    }

    /**
     * 获取所有分类
     */

    @ApiOperation(notes = "获取所有分类", value = "获取所有分类")
    @RequestMapping(value = "/p/category/u",method = RequestMethod.GET)
    public JsonResult<?> getCategoryList() {
        return new JsonResult<>(200,"success",articleMapper.selectArchiveList());
    }


    /**
     * 获取按照分类排序的所有文章列表
     */

    @ApiOperation(notes = "获取按照分类排序的所有文章列表", value = "获取按照分类排序的所有文章列表")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "path", name = "offset", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(paramType = "path", name = "size", value = "每页数量", required = true, dataType = "Integer")})
    @RequestMapping(value = "/p/category/{offset}/{size}",method = RequestMethod.GET)
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
     * 获取按照标签排序的所有文章列表
     */

    @ApiOperation(notes = "获取按照标签排序的所有文章列表", value = "获取按照标签排序的所有文章列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "offset", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "size", value = "每页数量", required = true, dataType = "Integer")})
    @RequestMapping(value = "/p/tag/{offset}/{size}",method = RequestMethod.GET)
    public JsonResult<?> getTagArticles(@PathVariable int offset,@PathVariable int size) {
        PageHelper.startPage(offset, size);
        List<ArticleWithTag> articleWithTagList = articleMapper.selectTagDocuments();
        List<ArticleInfoWithTag> articleInfoWithTagList = new ArrayList<>();
        for (ArticleWithTag article : articleWithTagList){
            List<String> tags = tagMapper.selectTagsByArticle(article.getId());
            ArticleInfoWithTag articleInfoWithTag = new ArticleInfoWithTag(article,tags,false);
            articleInfoWithTagList.add(articleInfoWithTag);
        }
        return new JsonResult<>(200,"success",articleInfoWithTagList);
    }

    /**
     * 获取某个分类下的文章列表
     */

    @ApiOperation(notes = "获取归档内文章", value = "获取归档内文章")
    @ApiImplicitParam(paramType = "path", name = "category", value = "分类名", required = true, dataType = "String")
    @RequestMapping(value = "/p/category/{category}/u",method = RequestMethod.GET)
    public JsonResult<?> getArticleInArchives(@PathVariable String category) {
        List<Article> articleList = articleMapper.selectArticlesInArchive(category);
        List<ArticleInfo> articleInfoList = new ArrayList<>();
        for (Article article : articleList){
            List<String> tags = tagMapper.selectTagsByArticle(article.getId());
            ArticleInfo articleInfo = new ArticleInfo(article,tags,false);
            articleInfoList.add(articleInfo);
        }
        return new JsonResult<>(200,"success",articleInfoList);
    }

    /**
     * 获取某个标签下的文章列表
     */

    @ApiOperation(notes = "获取某个标签下的文章列表", value = "获取某个标签下的文章列表")
    @ApiImplicitParam(paramType = "path", name = "tag", value = "标签名", required = true, dataType = "String")
    @RequestMapping(value = "/p/tag/{tag}/u",method = RequestMethod.GET)
    public JsonResult<?> getArticlesInTag(@PathVariable String tag) {
        List<Article> articleList = articleMapper.selectArticlesInTag(tag);
        List<ArticleInfo> articleInfoList = new ArrayList<>();
        for (Article article : articleList){
            List<String> tags = tagMapper.selectTagsByArticle(article.getId());
            ArticleInfo articleInfo = new ArticleInfo(article,tags,false);
            articleInfoList.add(articleInfo);
        }
        return new JsonResult<>(200,"success",articleInfoList);
    }

    /**
     * 关键字搜索文章
     */

    @ApiOperation(notes = "关键字搜索文章", value = "关键字搜索文章")
    @ApiImplicitParam(paramType = "path", name = "content", value = "关键词", required = true, dataType = "String")
    @RequestMapping(value = "/p/f/{content}",method = RequestMethod.GET)
    public JsonResult<?> searchArticlesByContent(@PathVariable String content) {
        List<Article> articleList = articleMapper.selectArticleLikeContent(content);
        List<ArticleInfo> articleInfoList = new ArrayList<>();
        for (Article article : articleList){
            List<String> tags = tagMapper.selectTagsByArticle(article.getId());
            ArticleInfo articleInfo = new ArticleInfo(article,tags,false);
            articleInfoList.add(articleInfo);
        }
        return new JsonResult<>(200,"success",articleInfoList);
    }

    /**
     * 获取个人信息
     */

    @ApiOperation(notes = "获取个人信息", value = "获取个人信息")
    @RequestMapping(value = "/userInfo",method = RequestMethod.GET)
    public JsonResult<?> getUserInfo() {
        User user = userMapper.selectByPrimaryKey(1);
        UserInfo userInfo = new UserInfo();
        userInfo.setName(user.getName());
        userInfo.setEmail(user.getEmail());
        userInfo.setDescription(user.getDescription());
        userInfo.setGithub(user.getGithub());
        userInfo.setPhone(user.getPhone());
        userInfo.setUserHead(user.getUserHead());
        userInfo.setWeibo(user.getWeibo());
        return new JsonResult<>(200,"success",userInfo);
    }

    /**
     * 获取老的验证码
     * required=false表示不传的话，会给参数赋值为null
     * 但是参数类型为int就会赋值null给int，会报错，所以要设置默认值
     */

    @ApiOperation(notes = "获取老的验证码", value = "获取老的验证码")
    @ApiImplicitParam(paramType = "query", name = "random", value = "随机数，不作任何用", required = false, dataType = "String", defaultValue = "a")
    @RequestMapping(value = "/oldVerifyCode", method = RequestMethod.GET)
    public void getKaptchaImage(@RequestParam(value="random", required = false, defaultValue="a") String random, HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        response.setDateHeader("Expires", 0);
        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");
        // return a jpeg
        response.setContentType("image/jpeg");
        // create the text for the image
        String capText = captchaProducer.createText();
        // store the text in the session
        session.setAttribute(Constants.KAPTCHA_SESSION_KEY, capText.toLowerCase());
        // create the image with the text
        BufferedImage bi = captchaProducer.createImage(capText);
        ServletOutputStream out = response.getOutputStream();
        // write the data out
        ImageIO.write(bi, "jpg", out);
        try {
            out.flush();
        } finally {
            out.close();
        }
    }


    /**
     * 获取新的验证码(Gif版本)
     */

    @ApiOperation(notes = "获取新的验证码(Gif版本)", value = "获取新的验证码(Gif版本)")
    @ApiImplicitParam(paramType = "query", name = "random", value = "随机数，不作任何用", required = false, dataType = "String", defaultValue = "a")
    @RequestMapping(value = "/newVerifyCode", method = RequestMethod.GET)
    public void getGifCode(@RequestParam(value="random", required = false) String random, HttpServletResponse response, HttpServletRequest request) {
        try {
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType("image/gif");
            HttpSession session = request.getSession();
//            String sessionId = session.getId();
//            Cookie cookie = new Cookie("JSESSIONID",sessionId);
//            response.addCookie(cookie);
            //gif格式动画验证码(宽，高，位数)
            Captcha captcha = new GifCaptcha(146, 33, 4);
            //输出
            captcha.out(response.getOutputStream());
            //存入Session
            session.setAttribute(Constants.KAPTCHA_SESSION_KEY, captcha.text().toLowerCase());
        } catch (Exception e) {
            System.err.println("获取验证码异常：" + e.getMessage());
        }
    }

    @ApiOperation(notes = "后台登录", value = "后台登录")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "name", value = "用户名", required = true, dataType = "String", defaultValue = "admin"),
            @ApiImplicitParam(paramType = "query", name = "password", value = "密码", required = true, dataType = "String", defaultValue = "123456"),
            @ApiImplicitParam(paramType = "query", name = "verifyCode", value = "验证码", required = true, dataType = "String", defaultValue = "s2g7")})
    @ApiResponses({
            @ApiResponse(code=200,message="登录成功"),
            @ApiResponse(code=401,message="用户名或密码错误")
    })
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public JsonResult<?> login(HttpServletRequest request, @RequestParam("name") String name, @RequestParam("password") String password, @RequestParam("verifyCode") String verifyCode) {

        HttpSession session = request.getSession();
        String code = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
        if (code == null) {
            return new JsonResult<>(403, "验证码未获取");
        }
        if (!code.equals(verifyCode.toLowerCase())) {
            return new JsonResult<>(400, "验证码错误");
        }

//        if (userMapper.selectByNameAndPwd(name,password) != null){
//            String token = tokenService.createToken(name, password);
//            stringRedisTemplate.opsForValue().set("loginUser:"+name, token, 1L, TimeUnit.DAYS);  //name要唯一
//            return new JsonResult<>(200,"登录成功",token);
//        }
//
//        return new JsonResult<>(401,"用户名或密码错误");

//        shiro
        try {
            UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(name, password);
            SecurityUtils.getSubject().login(usernamePasswordToken);

            String token = tokenService.createToken(name, password);
            stringRedisTemplate.opsForValue().set("loginUser:" + name, token, 1L, TimeUnit.DAYS);  //name要唯一

            return new JsonResult<>(200, "登录成功", token);
        } catch (ShiroException e) {
            return new JsonResult<>(401, e.getMessage());
        }

    }

    /**
     * 获取分类数
     */

    @ApiOperation(notes = "获取分类数", value = "获取分类数")
    @RequestMapping(value = "/p/category/count",method = RequestMethod.GET)
    public JsonResult<?> getCategoryCount() {
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
     * 获取文章总数
     */

    @ApiOperation(notes = "获取文章总数", value = "获取文章总数")
    @RequestMapping(value = "/p/count",method = RequestMethod.GET)
    public JsonResult<?> getArticleCount() {
        return new JsonResult<>(200,"success",documentMapper.countDocument());
    }


    /*
     * 转向登录页面
     */

    @ApiOperation(notes = "转向登录页面", value = "转向登录页面")
    @RequestMapping(value = "/sign_in", method = RequestMethod.GET)
    public JsonResult<?> signIn(HttpServletResponse response, HttpServletRequest request) {
        return new JsonResult<>(401, "登录页面");
    }

    /*
     * 转向未授权页面
     */

    @ApiOperation(notes = "转向未授权页面", value = "转向未授权页面")
    @RequestMapping(value = "/unAuthorization", method = RequestMethod.GET)
    public JsonResult<?> unAuthorization(HttpServletResponse response, HttpServletRequest request) {
        return new JsonResult<>(403, "权限不足");
    }

    /*
     * 退出登录
     */

    @ApiOperation(notes = "退出登录", value = "退出登录")
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public JsonResult<?> logout(HttpServletResponse response, HttpServletRequest request) {
        myShiroRealm.clearCached();
//        SecurityUtils.getSubject().logout();  LogoutFilter帮你实现了
        return new JsonResult<>(200, "退出成功！");
    }
}
