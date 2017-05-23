package com.frank.controller;

import com.frank.dao.UserMapper;
import com.frank.dto.JsonResult;
import com.frank.service.PermissionService;
import com.frank.service.TokenService;
import com.frank.shiro.MyShiroRealm;
import com.frank.vcode.Captcha;
import com.frank.vcode.GifCaptcha;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

/**
 * Created by frank on 17/4/19.
 */
@Api(value = "管理员")
@RestController
@RequestMapping("/admin")
@EnableAutoConfiguration
public class AdminController {

    /*
     * 登录
     * @RequestHeader("Authorization") String authorization
     */

    private Logger log = Logger.getLogger(AdminController.class);

    @Resource
    private Producer captchaProducer;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private TokenService tokenService;

    @Resource
    private MyShiroRealm myShiroRealm;

    @ApiOperation(notes = "获取验证码", value = "获取验证码")
    @RequestMapping(value = "/kaptcha", method = RequestMethod.GET)
    public ModelAndView getKaptchaImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
//        String code = (String)session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
//        log.info("******************验证码是: " + code + "******************");

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
        session.setAttribute(Constants.KAPTCHA_SESSION_KEY, capText);

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
        return null;
    }


    /**
     * 获取验证码（Gif版本）
     */
    @ApiOperation(notes = "获取验证码", value = "获取验证码")
    @RequestMapping(value = "getGifCode/{random}", method = RequestMethod.GET)
    public void getGifCode(@PathVariable int random, HttpServletResponse response, HttpServletRequest request) {
        try {
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType("image/gif");
            /**
             * gif格式动画验证码
             * 宽，高，位数。
             */
            Captcha captcha = new GifCaptcha(146, 33, 4);
            //输出
            captcha.out(response.getOutputStream());
            HttpSession session = request.getSession();
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
//    @ApiResponses({
//       @ApiResponse(code=200,message="登录成功"),
//       @ApiResponse(code=401,message="用户名或密码错误")
//    })
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public JsonResult<?> login(HttpServletRequest request, @RequestParam("name") String name, @RequestParam("password") String password, @RequestParam("verifyCode") String verifyCode) {

        HttpSession session = request.getSession();
        String code = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
        if (code == null) {
            return new JsonResult<>(403, "验证码未获取");
        }
        if (!code.equals(verifyCode)) {
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