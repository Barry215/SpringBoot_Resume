package com.frank.filter;

import com.frank.dto.JsonResult;
import com.frank.service.TokenService;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.alibaba.fastjson.JSON;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * Created by frank on 17/4/20.
 */
public class AdminFilter implements Filter {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private TokenService tokenService;


    public void init(FilterConfig config) throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());
    }


    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest httpRequest = (HttpServletRequest)req;
        String token = httpRequest.getHeader("Authorization");
        try{
            if (token == null){
                throw new JwtException("Unauthorized");
            }
            String name = tokenService.parseToken(token);
            String value = stringRedisTemplate.opsForValue().get(name);
            if (value == null || !value.equals(token)){
                throw new JwtException("invalid token");
            }
            chain.doFilter(req, resp);
        }catch (JwtException jwtException){
            String message = jwtException.getMessage();
            HttpServletResponse httpResponse = (HttpServletResponse) resp;
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.setContentType("application/json; charset=utf-8");
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write(JSON.toJSONString(new JsonResult<>(401,message)));
        }
    }

    public void destroy() {

    }

}
