package com.frank.service.impl;

import com.frank.service.TokenService;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by frank on 17/4/20.
 */
@Service
public class TokenServiceImpl implements TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Override
    public String createToken(String name, String password) {

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject("login")
                .setAudience("admin")
                .claim("name",name)
                .setNotBefore(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    @Override
    public String parseToken(String token) throws JwtException{
        Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        return (String)claims.get("name");
    }
}
