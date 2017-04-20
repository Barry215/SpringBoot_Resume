package com.frank.filter;

import com.frank.dto.JsonResult;
import com.frank.service.TokenService;
import io.jsonwebtoken.JwtException;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.alibaba.fastjson.JSON;

/**
 * Created by frank on 17/4/20.
 */
public class AdminFilter implements Filter {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private TokenService tokenService;

    public void init(FilterConfig config) throws ServletException {

    }


    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest)req;
        String token = httpRequest.getHeader("Authorization");
        String name;
        try{
            name = tokenService.parseToken(token);
        }catch (JwtException jwtException){
            String message = jwtException.getMessage();
            HttpServletResponse httpResponse = (HttpServletResponse) resp;
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.setContentType("application/json; charset=utf-8");
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write(JSON.toJSONString(new JsonResult<>(401,message)));
            return;
        }
        String value = stringRedisTemplate.opsForValue().get(name);
        //设置时间
        //重定向
        chain.doFilter(req, resp);
    }

    public void destroy() {

    }

}
