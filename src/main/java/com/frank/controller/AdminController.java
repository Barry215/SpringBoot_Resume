package com.frank.controller;

import com.frank.dao.UserMapper;
import com.frank.dto.JsonResult;
import com.frank.service.TokenService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Created by frank on 17/4/19.
 */
@RestController
@RequestMapping("/admin")
@EnableAutoConfiguration
public class AdminController {

    /*
     * 登录
     * @RequestHeader("Authorization") String authorization
     */


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private TokenService tokenService;

    @Resource
    private UserMapper userMapper;

    @ApiOperation(notes = "后台登录", value = "后台登录")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "name", value = "用户名", required = true, dataType = "String"),
        @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String"),
        @ApiImplicitParam(name = "verifyCode", value = "验证码", required = true, dataType = "String")})
//    @ApiResponses({
//       @ApiResponse(code=200,message="登录成功"),
//       @ApiResponse(code=401,message="用户名或密码错误")
//    })
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public JsonResult<?> login(@RequestParam("name") String name,@RequestParam("password") String password,@RequestParam("verifyCode") String verifyCode) {

        if (userMapper.selectByNameAndPwd(name,password) != null){
            String token = tokenService.createToken(name, password);
            stringRedisTemplate.opsForValue().set(name, token, 1L, TimeUnit.DAYS);  //name要唯一
            return new JsonResult<>(200,"登录成功",token);
        }

        return new JsonResult<>(401,"用户名或密码错误");
    }


}
